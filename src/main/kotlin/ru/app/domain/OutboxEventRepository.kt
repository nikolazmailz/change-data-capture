package ru.app.domain

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

interface OutboxEventRepository: R2dbcRepository<OutboxEvent, UUID>  {

    @Query("""
      SELECT * 
        FROM outbox_event 
       WHERE status = 'PENDING' 
       FOR UPDATE SKIP LOCKED
       LIMIT :limit
    """)
    fun lockPending(limit: Int): Flux<OutboxEvent>


    @Query("""
      UPDATE outbox_event
         SET status = :status,
             sent_at = :sentAt
       WHERE id = ANY(:ids::uuid[])
    """)
    fun updateStatusBulk(ids: Array<UUID>, status: StatusType, sentAt: Instant?): Mono<Int>

}