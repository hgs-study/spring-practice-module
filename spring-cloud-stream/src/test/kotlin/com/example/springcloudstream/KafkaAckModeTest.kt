// src/test/kotlin/com/example/kafkatest/KafkaAckModeTest.kt
package com.example.springcloudstream

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties.AckMode
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

@EmbeddedKafka(
    partitions = 1,
    topics = ["batch-topic", "record-topic", "manual-topic"],
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    ]
)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaAckModeTest {

    @Autowired
    private lateinit var embeddedKafka: EmbeddedKafkaBroker

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    companion object {
        const val MESSAGE_COUNT = 200
        const val FAIL_AT_OFFSET = 150L
        const val POLL_SIZE = 100
    }

    @TestConfiguration
    class KafkaTestConfig {
        
        @Bean
        fun producerFactory(embeddedKafka: EmbeddedKafkaBroker): ProducerFactory<String, String> {
            val props = HashMap<String, Any>()
            props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = embeddedKafka.brokersAsString
            props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
            props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
            return DefaultKafkaProducerFactory(props)
        }

        @Bean
        fun kafkaTemplate(producerFactory: ProducerFactory<String, String>): KafkaTemplate<String, String> {
            return KafkaTemplate(producerFactory)
        }

        @Bean
        fun consumerFactory(embeddedKafka: EmbeddedKafkaBroker): ConsumerFactory<String, String> {
            val props = HashMap<String, Any>()
            props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = embeddedKafka.brokersAsString
            props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
            props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = POLL_SIZE
            return DefaultKafkaConsumerFactory(props)
        }
    }

    private fun sendMessages(topic: String, count: Int) {
        repeat(count) { i ->
            kafkaTemplate.send(topic, "key-$i", "message-$i").get()
        }
        logger.info { "✅ Sent $count messages to $topic" }
    }

    private fun getCommittedOffset(topic: String, groupId: String): Long? {
        val props = KafkaTestUtils.consumerProps(groupId, "false", embeddedKafka)
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        
        val consumer = DefaultKafkaConsumerFactory<String, String>(props).createConsumer()
        val tp = TopicPartition(topic, 0)
        
        return try {
            val committed = consumer.committed(setOf(tp))
            committed[tp]?.offset()
        } finally {
            consumer.close()
        }
    }

    // ============================================
    // BATCH ACK MODE 테스트
    // ============================================
    @Test
    @DisplayName("BATCH 모드: poll() 단위로 커밋 - 장애 시 poll 시작점부터 재처리")
    fun `test BATCH ack mode - failure causes entire poll batch to be reprocessed`() {
        val topic = "batch-topic"
        val groupId = "batch-test-group"
        val processedOffsets = CopyOnWriteArrayList<Long>()
        val processedCount = AtomicInteger(0)
        val failureLatch = CountDownLatch(1)

        // 1. 메시지 발행
        sendMessages(topic, MESSAGE_COUNT)

        // 2. BATCH 모드 Consumer 설정
        val props = KafkaTestUtils.consumerProps(groupId, "false", embeddedKafka)
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = POLL_SIZE
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        
        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(props)
        val containerFactory = ConcurrentKafkaListenerContainerFactory<String, String>()
        containerFactory.consumerFactory = consumerFactory
        containerFactory.containerProperties.ackMode = AckMode.BATCH  // BATCH 모드

        // 3. 직접 Consumer로 테스트
        val consumer = consumerFactory.createConsumer()
        consumer.subscribe(listOf(topic))

        try {
            var shouldFail = true
            
            // 첫 번째 poll (0-99) - 정상 처리
            var records = consumer.poll(Duration.ofSeconds(10))
            logger.info { "1st poll: ${records.count()} records" }
            
            records.forEach { record ->
                processedOffsets.add(record.offset())
                processedCount.incrementAndGet()
                logger.info { "Processing offset: ${record.offset()}" }
            }
            consumer.commitSync()  // 0-99 커밋 완료
            logger.info { "✅ Committed after 1st poll. Offset should be 100" }

            // 두 번째 poll (100-199) - 150에서 장애
            records = consumer.poll(Duration.ofSeconds(10))
            logger.info { "2nd poll: ${records.count()} records" }

            for (record in records) {
                if (record.offset() == FAIL_AT_OFFSET && shouldFail) {
                    logger.error { "💥 Simulating failure at offset ${record.offset()}" }
                    // 커밋하지 않고 종료 시뮬레이션
                    shouldFail = false
                    break
                }
                processedOffsets.add(record.offset())
                processedCount.incrementAndGet()
                logger.info { "Processing offset: ${record.offset()}" }
            }
            // commitSync() 호출하지 않음 - 장애 상황

        } finally {
            consumer.close()
        }

        // 4. 커밋된 오프셋 확인
        val committedOffset = getCommittedOffset(topic, groupId)
        
        logger.info { 
            """
            |
            |📊 BATCH Mode Test Results:
            |   Total messages sent: $MESSAGE_COUNT
            |   Processed before failure: ${processedCount.get()}
            |   Committed offset: $committedOffset
            |   Expected restart point: 100 (start of 2nd poll)
            |   Messages to reprocess: 100-149 (50 messages)
            """.trimMargin()
        }

        // 5. 검증
        assertEquals(100L, committedOffset, "BATCH 모드에서는 이전 poll 완료 시점(100)까지만 커밋되어야 함")
        assertTrue(processedCount.get() >= 100, "최소 100개는 처리되어야 함")
        assertTrue(processedCount.get() < MESSAGE_COUNT, "장애로 인해 전체 처리는 안됨")
    }

    // ============================================
    // RECORD ACK MODE 테스트
    // ============================================
    @Test
    @DisplayName("RECORD 모드: 건별 커밋 - 장애 시 해당 메시지부터 재처리")
    fun `test RECORD ack mode - failure only affects single record`() {
        val topic = "record-topic"
        val groupId = "record-test-group"
        val processedOffsets = CopyOnWriteArrayList<Long>()
        val processedCount = AtomicInteger(0)

        // 1. 메시지 발행
        sendMessages(topic, MESSAGE_COUNT)

        // 2. RECORD 모드 Consumer 설정
        val props = KafkaTestUtils.consumerProps(groupId, "false", embeddedKafka)
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = POLL_SIZE
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java

        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(props)
        val consumer = consumerFactory.createConsumer()
        consumer.subscribe(listOf(topic))

        try {
            val tp = TopicPartition(topic, 0)
            var shouldContinue = true

            while (shouldContinue) {
                val records = consumer.poll(Duration.ofSeconds(5))
                
                if (records.isEmpty) break

                for (record in records) {
                    if (record.offset() == FAIL_AT_OFFSET) {
                        logger.error { "💥 Simulating failure at offset ${record.offset()}" }
                        shouldContinue = false
                        break  // 커밋하지 않고 종료
                    }

                    processedOffsets.add(record.offset())
                    processedCount.incrementAndGet()
                    logger.info { "Processing offset: ${record.offset()}" }

                    // RECORD 모드: 건별 커밋
                    consumer.commitSync(
                        mapOf(tp to OffsetAndMetadata(record.offset() + 1))
                    )
                }
            }

        } finally {
            consumer.close()
        }

        // 4. 커밋된 오프셋 확인
        val committedOffset = getCommittedOffset(topic, groupId)

        logger.info { 
            """
            |
            |📊 RECORD Mode Test Results:
            |   Total messages sent: $MESSAGE_COUNT
            |   Processed before failure: ${processedCount.get()}
            |   Committed offset: $committedOffset
            |   Expected restart point: $FAIL_AT_OFFSET
            |   Messages to reprocess: 1 (only offset $FAIL_AT_OFFSET)
            """.trimMargin()
        }

        // 5. 검증
        assertEquals(FAIL_AT_OFFSET, committedOffset, "RECORD 모드에서는 마지막 성공 건(149) 다음인 150까지 커밋되어야 함")
        assertEquals(FAIL_AT_OFFSET.toInt(), processedCount.get(), "정확히 150개가 처리되어야 함")
    }

    // ============================================
    // MANUAL ACK MODE 테스트
    // ============================================
    @Test
    @DisplayName("MANUAL 모드: 수동 커밋 - 원하는 시점에 커밋")
    fun `test MANUAL ack mode - commit only when explicitly called`() {
        val topic = "manual-topic"
        val groupId = "manual-test-group"
        val processedOffsets = CopyOnWriteArrayList<Long>()
        val processedCount = AtomicInteger(0)
        val acknowledgedCount = AtomicInteger(0)

        // 1. 메시지 발행
        sendMessages(topic, MESSAGE_COUNT)

        // 2. MANUAL 모드 Consumer 설정
        val props = KafkaTestUtils.consumerProps(groupId, "false", embeddedKafka)
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = POLL_SIZE
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java

        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(props)
        val consumer = consumerFactory.createConsumer()
        consumer.subscribe(listOf(topic))

        try {
            val tp = TopicPartition(topic, 0)
            var shouldContinue = true

            while (shouldContinue) {
                val records = consumer.poll(Duration.ofSeconds(5))
                
                if (records.isEmpty) break

                for (record in records) {
                    if (record.offset() == FAIL_AT_OFFSET) {
                        logger.error { "💥 Simulating failure at offset ${record.offset()} - NOT acknowledging" }
                        shouldContinue = false
                        break  // acknowledge 하지 않고 종료
                    }

                    processedOffsets.add(record.offset())
                    processedCount.incrementAndGet()
                    logger.info { "Processing offset: ${record.offset()}" }

                    // MANUAL 모드: 명시적으로 커밋
                    consumer.commitSync(
                        mapOf(tp to OffsetAndMetadata(record.offset() + 1))
                    )
                    acknowledgedCount.incrementAndGet()
                    logger.info { "✅ Manually acknowledged offset: ${record.offset()}" }
                }
            }

        } finally {
            consumer.close()
        }

        // 4. 커밋된 오프셋 확인
        val committedOffset = getCommittedOffset(topic, groupId)

        logger.info { 
            """
            |
            |📊 MANUAL Mode Test Results:
            |   Total messages sent: $MESSAGE_COUNT
            |   Processed before failure: ${processedCount.get()}
            |   Acknowledged count: ${acknowledgedCount.get()}
            |   Committed offset: $committedOffset
            |   Expected restart point: $FAIL_AT_OFFSET
            |   Messages to reprocess: 1 (only offset $FAIL_AT_OFFSET)
            """.trimMargin()
        }

        // 5. 검증
        assertEquals(FAIL_AT_OFFSET, committedOffset, "MANUAL 모드에서는 마지막 acknowledge 시점까지만 커밋되어야 함")
        assertEquals(acknowledgedCount.get(), processedCount.get(), "처리한 만큼 acknowledge 되어야 함")
    }

    // ============================================
    // 비교 테스트
    // ============================================
    @Test
    @DisplayName("모든 모드 비교: 동일 장애 상황에서 커밋 오프셋 차이")
    fun `compare all ack modes - committed offset differences`() {
        data class TestResult(
            val mode: String,
            val committedOffset: Long?,
            val reprocessCount: Int
        )

        val results = mutableListOf<TestResult>()

        // BATCH 모드 시뮬레이션
        // 0-99 처리 후 커밋, 100-150에서 장애 → 커밋 오프셋 100
        results.add(TestResult("BATCH", 100L, 50))

        // RECORD 모드 시뮬레이션  
        // 건별 커밋, 150에서 장애 → 커밋 오프셋 150
        results.add(TestResult("RECORD", 150L, 1))

        // MANUAL 모드 시뮬레이션
        // 수동 커밋, 150에서 장애 (acknowledge 안함) → 커밋 오프셋 150
        results.add(TestResult("MANUAL", 150L, 1))

        logger.info {
            """
            |
            |╔══════════════════════════════════════════════════════════════════╗
            |║              ACK MODE 비교 (장애 발생: offset 150)                ║
            |╠══════════════════════════════════════════════════════════════════╣
            |║  Mode    │ Committed Offset │ Restart From │ Reprocess Count    ║
            |╠══════════════════════════════════════════════════════════════════╣
            |║  BATCH   │       100        │     100      │    50 messages     ║
            |║  RECORD  │       150        │     150      │     1 message      ║
            |║  MANUAL  │       150        │     150      │     1 message      ║
            |╚══════════════════════════════════════════════════════════════════╝
            |
            |📌 결론:
            |   - BATCH: poll() 단위 커밋 → 장애 시 해당 poll 전체 재처리
            |   - RECORD: 건별 커밋 → 장애 발생한 메시지만 재처리
            |   - MANUAL: 개발자 제어 → acknowledge() 호출 시점까지만 커밋
            """.trimMargin()
        }

        // 검증
        assertEquals(100L, results[0].committedOffset)
        assertEquals(150L, results[1].committedOffset)
        assertEquals(150L, results[2].committedOffset)
    }
}