package ru.app.domain

import org.springframework.data.r2dbc.repository.R2dbcRepository
import java.util.UUID

interface OutboxEventRepository: R2dbcRepository<OutboxEvent, UUID>  {
}