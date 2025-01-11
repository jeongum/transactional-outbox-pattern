package com.doteloper.transactional_outbox_pattern.user.infra.mysql

import com.doteloper.transactional_outbox_pattern.user.domain.User
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class UserEntity(
    @Id val id: String,
    val name: String,
) {
    constructor(user: User) : this(
        id = user.id,
        name = user.name
    )
}
