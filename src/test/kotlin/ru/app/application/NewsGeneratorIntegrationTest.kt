package ru.app.application

import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import org.springframework.beans.factory.annotation.Autowired
import ru.app.BaseIntegrationTest
import ru.app.TestWebClientConfig.Companion.mockServer
import ru.app.domain.NewsRepository
import ru.app.domain.OutboxEventRepository
import ru.app.domain.StatusType
import java.time.Duration

class NewsGeneratorIntegrationTest: BaseIntegrationTest() {

    @Autowired
    private lateinit var newsRepository: NewsRepository

    @Autowired
    private lateinit var outboxEventRepository: OutboxEventRepository


    init {

        beforeTest {
            newsRepository.deleteAll().block()
            outboxEventRepository.deleteAll().block()
        }

        should("start generator and save news to Postgres") {
            mockServer.enqueue(MockResponse().setResponseCode(200))
            mockServer.enqueue(MockResponse().setResponseCode(404))
            // ждём немного дольше 1 интераций
            Thread.sleep(Duration.ofSeconds(5).toMillis())
            val count = newsRepository.count().block()
            count!! shouldBeGreaterThan 0L

            val blockFirst = newsRepository.findAll().blockFirst()!!

            blockFirst.title = "new title"
            blockFirst.text = "new text"
            newsRepository.save(blockFirst).block()

            Thread.sleep(Duration.ofSeconds(3).toMillis())

            val outboxCount = outboxEventRepository.count().block()
            outboxCount!! shouldBeGreaterThan 0L

            val outboxFirst = outboxEventRepository.findAll().blockFirst()!!
            outboxFirst.status shouldBe StatusType.SENT

            val idFirst = blockFirst.id
            val outboxLast = outboxEventRepository.findAll().blockLast()!!
            outboxLast.aggregateId shouldBe idFirst
            outboxLast.status shouldBe StatusType.FAILED
        }
    }

}