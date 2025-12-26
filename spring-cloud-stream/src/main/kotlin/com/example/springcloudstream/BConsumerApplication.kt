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
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener

private val logger = KotlinLogging.logger {}

@Profile("b")
@SpringBootApplication
class BConsumerApplication {

    @KafkaListener(id = "batch-out-group", topics = ["batch-out-topic"])
    fun bathOutGrouplistenA(`in`: String?) {
        logger.info { "bathOutGrouiplisten [B]: $`in`" }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(BConsumerApplication::class.java)
                    .profiles("b")
                    .run(*args)
        }
    }
}
