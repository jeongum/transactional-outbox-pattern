package com.doteloper.transactional_outbox_pattern.user.infra.mysql

import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserEntity, String>
