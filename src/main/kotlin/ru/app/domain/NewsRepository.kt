package ru.app.domain

import org.springframework.data.r2dbc.repository.R2dbcRepository
import java.util.UUID

interface NewsRepository : R2dbcRepository<News, UUID> {
}