package com.doteloper.transactional_outbox_pattern.user.domain

data class UserCreateEvent(
    val userId: String,
    val userName: String,
)
