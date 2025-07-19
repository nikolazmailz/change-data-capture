package ru.app.application

import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import ru.app.domain.News
import ru.app.domain.NewsRepository
import java.time.Duration
import java.util.UUID

@Component
class NewsGeneratorService (
    private val newsRepository: NewsRepository
) {

    private val lines: List<String> = loadLines()

    @PostConstruct
    fun startGeneration() {
        Flux.interval(Duration.ofSeconds(5))
            // ограничим общую длительность генерации 3 минутами
            .take(Duration.ofMinutes(3))
            .flatMap { _ ->
                val text = lines.random()
                val title = text.take(20).let { if (it.length < text.length) "$it…" else it }
                val creatorId = UUID.randomUUID()
                val news = News(
                    id = UUID.randomUUID(),
                    title = title,
                    text = text,
                    creatorId = creatorId,
                ).setAsNew()
                newsRepository.save(news)
            }
            .doOnNext { saved -> println("Generated news with id=${saved.id}") }
            .doOnError { e -> println("Error during news generation: ${e.message}") }
            .doOnComplete { println("News generation finished after 3 minutes.") }
            .subscribe()
    }

    private fun loadLines(): List<String> =
        ClassPathResource("news.txt").inputStream
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }
}