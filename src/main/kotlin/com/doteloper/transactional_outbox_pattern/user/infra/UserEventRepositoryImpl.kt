package com.doteloper.transactional_outbox_pattern.user.infra

import com.doteloper.transactional_outbox_pattern.user.domain.UserCreateEvent
import com.doteloper.transactional_outbox_pattern.user.domain.repository.UserEventRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Repository

@Repository
class UserEventRepositoryImpl(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${doteloper.event.topic}") private val topic: String,
) : UserEventRepository {
    override fun publishCreateEvent(userCreateEvent: UserCreateEvent) {
        kafkaTemplate.send(topic, userCreateEvent.userName)
    }
}
