package com.doteloper.transactional_outbox_pattern.user.service

import com.doteloper.transactional_outbox_pattern.user.domain.User
import com.doteloper.transactional_outbox_pattern.user.domain.UserCreateEvent
import com.doteloper.transactional_outbox_pattern.user.domain.repository.UserEventRepository
import com.doteloper.transactional_outbox_pattern.user.domain.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserCreateService(
    private val userRepository: UserRepository,
    private val userEventRepository: UserEventRepository,
) {
    fun create(name: String) {
        val user = User(name)

        userRepository.save(user)
        userEventRepository.publishCreateEvent(UserCreateEvent(user.id, user.name))
    }
}
