package ru.app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.Transient

@Table("news")
data class News(

    private val id: UUID,

    @Column("title")
    var title: String,

    @Column("text")
    var text: String,

    @Column("creator_id")
    val creatorId: UUID,

    @Column("created_at")
    val createdAt: Instant = Instant.now()
): Persistable<UUID> {

    @Transient
    private var isNew: Boolean = false

    @Id
    override fun getId(): UUID = id

    override fun isNew(): Boolean = isNew

    fun setAsNew(): News {
        isNew = true
        return this
    }

}