package com.doteloper.transactional_outbox_pattern.user.domain.repository

import com.doteloper.transactional_outbox_pattern.user.domain.User

interface UserRepository {
    fun save(user: User): User
}
