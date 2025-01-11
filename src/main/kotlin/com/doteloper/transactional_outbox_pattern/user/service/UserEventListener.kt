package com.doteloper.transactional_outbox_pattern.user.service

import com.doteloper.transactional_outbox_pattern.user.domain.EventRecordStatus
import com.doteloper.transactional_outbox_pattern.user.domain.UserCreateEvent
import com.doteloper.transactional_outbox_pattern.user.domain.UserEventRecord
import com.doteloper.transactional_outbox_pattern.user.domain.repository.UserEventRecorder
import com.doteloper.transactional_outbox_pattern.user.domain.repository.UserEventRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserEventListener(
    private val userEventRecorder: UserEventRecorder,
    private val userEventRepository: UserEventRepository,
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun recordMessageHandler(eventRecord: UserEventRecord) {
        userEventRecorder.save(eventRecord)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendMessageHandler(eventRecord: UserEventRecord) {
        val event = UserCreateEvent(eventRecord.id, eventRecord.name)

        val status: EventRecordStatus = runCatching {
            userEventRepository.publishCreateEvent(event)
        }.fold(
            onSuccess = {
                logger.info("Success to Publish Event")
                EventRecordStatus.SUCCESS
            }, onFailure = {
                logger.info("Fail to Publish Event")
                EventRecordStatus.FAIL
            }
        )

        userEventRecorder.save(eventRecord.copy(status = status))
    }
}
