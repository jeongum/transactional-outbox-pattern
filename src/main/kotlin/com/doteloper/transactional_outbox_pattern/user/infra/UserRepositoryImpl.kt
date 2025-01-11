package com.doteloper.transactional_outbox_pattern.user.infra

import com.doteloper.transactional_outbox_pattern.user.domain.User
import com.doteloper.transactional_outbox_pattern.user.domain.repository.UserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl : UserRepository {
    override fun save(user: User): User {
        return user
    }
}
