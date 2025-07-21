package ru.app.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class AppConfig {

    @Bean
    fun webClient(): WebClient =
        WebClient.builder()
            .baseUrl("http://localhost:9200") // URL к OpenSearch
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
            .build()

    @Bean fun txManager(cf: ConnectionFactory): R2dbcTransactionManager =
        R2dbcTransactionManager(cf)

    @Bean fun txOperator(txm: R2dbcTransactionManager): TransactionalOperator =
        TransactionalOperator.create(txm)
}