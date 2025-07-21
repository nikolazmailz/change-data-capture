package ru.app

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.extensions.spring.SpringExtension
import okhttp3.mockwebserver.MockWebServer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = [TestWebClientConfig::class],
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "logging.level.liquibase=TRACE",
        "logging.level.org.springframework.boot.autoconfigure.liquibase=TRACE"
    ]
)
@ActiveProfiles("test")
@Testcontainers
abstract class BaseIntegrationTest(body: ShouldSpec.() -> Unit = {}) : ShouldSpec(body) {

    override fun extensions(): List<Extension> = listOf(SpringExtension)

//    @Bean
//    @Primary
//    fun webClient(): WebClient =
//        WebClient.builder()
//            .baseUrl(mockServer.url("/").toString().removeSuffix("/"))
//            .build()

    companion object {

//        @JvmStatic
//        val mockServer = MockWebServer().apply { start() }

        @Container
        private val postgres = PostgreSQLContainer<Nothing>("postgres:15").apply {
            withDatabaseName("testdb")
            withUsername("postgresql")
            withPassword("postgresql")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // R2DBC
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"
            }
            registry.add("spring.r2dbc.username", postgres::getUsername)
            registry.add("spring.r2dbc.password", postgres::getPassword)

            // JDBC-datasource (для Liquibase)
            registry.add("spring.liquibase.enabled") {
                "true"
            }
            registry.add("spring.liquibase.url") {
                "jdbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"
            }
            registry.add("spring.liquibase.user", postgres::getUsername)
            registry.add("spring.liquibase.password", postgres::getPassword)

            // Мастер-чейндж-лог
            registry.add("spring.liquibase.change-log") {
                "classpath:db/changelog/db.changelog-master.yaml"
            }

        }
    }
}