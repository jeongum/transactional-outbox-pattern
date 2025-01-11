package com.doteloper.transactional_outbox_pattern.user.infra

import com.doteloper.transactional_outbox_pattern.user.domain.UserEventRecord
import com.doteloper.transactional_outbox_pattern.user.domain.repository.UserEventRecorder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class UserEventRecorderImpl : UserEventRecorder {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun save(event: UserEventRecord): UserEventRecord {
        logger.info("Save Event Record: ${event}")
        return event
    }
}
