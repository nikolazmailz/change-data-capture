package ru.app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("outbox_event")
data class OutboxEvent(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column("aggregate_id")
    val aggregateId: UUID,

    @Column("aggregate_type")
    val aggregateType: String,

    @Column("event_type")
    val eventType: EventType,

    @Column("payload")
    val payload: String,

    @Column("status")
    val status: StatusType,

    @Column("created_at")
    val createdAt: Instant = Instant.now(),

    @Column("sent_at")
    val sentAt: Instant? = null
)