/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.springcloudstream

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.UncheckedIOException
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.IntStream

private val logger = KotlinLogging.logger {}

@Profile("batch-produce")
@SpringBootApplication
class KafkaBatchSampleApplication {
    @KafkaListener(id = "batch-out", topics = ["batch-out"])
    fun listen(`in`: String?) {
        println(`in`)
    }
    @KafkaListener(id = "batch-out-group", topics = ["batch-out-topic"])
    fun bathOutGrouplistenA(`in`: String?) {
        logger.info { "bathOutGrouiplisten : $`in`" }
    }

    @Bean
    fun runner(template: KafkaTemplate<ByteArray?, ByteArray?>, environment: Environment): ApplicationRunner {
        return ApplicationRunner { args: ApplicationArguments? -> IntStream.range(0, 10).forEach { i: Int ->
            template.send("batch-in-topic",
                if (environment.activeProfiles.contains("batch-produce"))
                    "\"batch-test$i\"".toByteArray()
                else
                    "\"test$i\"".toByteArray()) }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(KafkaBatchSampleApplication::class.java)
//                    .profiles("default")
                    .profiles("batch-produce")
                    .run(*args)
        }
    }
}

open class Base {
    @Autowired
    val bridge: StreamBridge? = null
}

@Component
@Profile("default")
class NoTransactions : Base() {
    @Bean
    fun consumer(): Consumer<List<String>> {
        return Consumer { list: List<String> -> list.forEach(Consumer { str: String ->
            logger.info { "default consumer : $str"}
            super.bridge?.send("output-out-0", str.uppercase(Locale.getDefault()))
        }) }
    }
}

@Component
@Profile("transactional")
class Transactions : Base() {
    @Bean
    fun consumer(): Consumer<List<String>> {
        return Consumer { list: List<String> ->
            list.forEach(Consumer { str: String -> super.bridge?.send("output-out-0", str.uppercase(Locale.getDefault())) })
            println("Hit Enter to exit the listener and commit transaction")
            try {
                System.`in`.read()
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        }
    }
}

@Component
@Profile("batch-produce")
class BatchProduce : Base() {
    @Bean
    fun consumer(): Function<List<String>, List<Message<String>>> {
        return Function { list: List<String> ->
            logger.info { "batch-produce consumer : $list"}
            list.stream()
                    .map { obj: String -> obj.uppercase(Locale.getDefault()) }
                    .map { uppercasedString: String -> MessageBuilder.withPayload(uppercasedString).build() }
                    .collect(Collectors.toList())
        }
    }
}