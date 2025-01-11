package com.doteloper.transactional_outbox_pattern.user.domain.repository

import com.doteloper.transactional_outbox_pattern.user.domain.UserCreateEvent

interface UserEventRepository {
    fun publishCreateEvent(userCreateEvent: UserCreateEvent)
}
