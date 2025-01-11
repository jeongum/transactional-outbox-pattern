package com.doteloper.transactional_outbox_pattern

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TransactionalOutboxPatternApplication

fun main(args: Array<String>) {
	runApplication<TransactionalOutboxPatternApplication>(*args)
}
