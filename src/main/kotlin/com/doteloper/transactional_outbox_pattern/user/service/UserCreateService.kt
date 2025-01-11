package com.doteloper.transactional_outbox_pattern.user.service

import com.doteloper.transactional_outbox_pattern.user.domain.User
import com.doteloper.transactional_outbox_pattern.user.domain.UserEventRecord
import com.doteloper.transactional_outbox_pattern.user.domain.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserCreateService(
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun create(name: String) {
        val user = User(name)

        userRepository.save(user)
        eventPublisher.publishEvent(UserEventRecord(user.id, user.name))

        logger.info("Complete User Creation")
    }
}
