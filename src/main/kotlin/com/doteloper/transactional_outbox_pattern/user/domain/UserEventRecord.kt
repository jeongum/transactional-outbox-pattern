package com.doteloper.transactional_outbox_pattern.user.domain

data class UserEventRecord(
    val id: String,
    val name: String,
    val status: EventRecordStatus,
) {
    constructor(id: String, name: String) : this(
        id = id,
        name = name,
        status = EventRecordStatus.PROCESSING
    )
}

enum class EventRecordStatus {
    PROCESSING, SUCCESS, FAIL
}
