package com.doteloper.transactional_outbox_pattern.user.infra

import com.doteloper.transactional_outbox_pattern.user.domain.User
import com.doteloper.transactional_outbox_pattern.user.domain.repository.UserRepository
import com.doteloper.transactional_outbox_pattern.user.infra.mysql.UserEntity
import com.doteloper.transactional_outbox_pattern.user.infra.mysql.UserJpaRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository,
) : UserRepository {
    override fun save(user: User) {
        userJpaRepository.save(UserEntity(user))
    }
}
