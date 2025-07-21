package ru.app.application


import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import ru.app.domain.OutboxEvent
import ru.app.domain.OutboxEventRepository
import ru.app.domain.StatusType
import java.time.Duration
import java.time.Instant


@Component
class OutboxService(
    private val repo: OutboxEventRepository,
    @Qualifier("webClient")
    private val webClient: WebClient,
    private val txOp: TransactionalOperator
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val batchSize = 100

    init {
        Flux.interval(Duration.ofSeconds(1))
            .flatMap {
                log.info("Flux.interval(Duration.ofSeconds(1))")
                fetchAndBuffer() }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    private fun fetchAndBuffer(): Flux<Void> =
        txOp.execute { _ ->
            repo.lockPending(batchSize)
        }
            .bufferTimeout(batchSize, Duration.ofMillis(500))
            .filter { it.isNotEmpty() }
            .flatMap { events -> sendBulk(events) }
            .thenMany(Flux.empty())

    private fun sendBulk(events: List<OutboxEvent>) =
        webClient.post()
            .uri("/api/v1/bulk-events")
            .bodyValue(events.map { it.payload })
            .retrieve()
            .onStatus(
                { status ->
                    !status.is2xxSuccessful },
                { resp ->
                    Mono.error(RuntimeException("Unexpected status ${resp.statusCode()}")) }
            )
            .toBodilessEntity()
            .flatMap {
                // все успех — статус SENT
                repo.updateStatusBulk(
                    events.map { it.id }.toTypedArray(),
                    StatusType.SENT,
                    Instant.now()
                )
            }
            .onErrorResume { ex ->
                log.error("Bulk send failed", ex)
                // при любом провале — ставим FAILED
                repo.updateStatusBulk(
                    events.map { it.id }.toTypedArray(),
                    StatusType.FAILED,
                    Instant.now()
                )
            }
            .then()

}