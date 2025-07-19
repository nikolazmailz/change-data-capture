package ru.app.application

import io.kotest.matchers.comparables.shouldBeGreaterThan
import org.springframework.beans.factory.annotation.Autowired
import ru.app.BaseIntegrationTest
import ru.app.domain.NewsRepository
import java.time.Duration

class NewsGeneratorIntegrationTest: BaseIntegrationTest() {

    @Autowired
    private lateinit var repo: NewsRepository

    init {
        should("start generator and save news to Postgres") {
            // ждём немного дольше 5 интераций
            Thread.sleep(Duration.ofSeconds(10).toMillis())
            val count = repo.count().block()
            count!! shouldBeGreaterThan 0L
        }
    }

}