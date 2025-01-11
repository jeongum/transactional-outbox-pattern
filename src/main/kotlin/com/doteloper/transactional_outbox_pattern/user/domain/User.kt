package com.doteloper.transactional_outbox_pattern.user.domain

import java.util.*

data class User(
    val id: String,
    val name: String,
) {
    constructor(name: String) : this(
        id = UUID.randomUUID().toString(),
        name = name
    )
}
