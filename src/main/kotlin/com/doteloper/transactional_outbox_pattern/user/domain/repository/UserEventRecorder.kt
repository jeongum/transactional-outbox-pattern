package com.doteloper.transactional_outbox_pattern.user.domain.repository

import com.doteloper.transactional_outbox_pattern.user.domain.UserEventRecord

interface UserEventRecorder {
    fun save(event: UserEventRecord): UserEventRecord
}
