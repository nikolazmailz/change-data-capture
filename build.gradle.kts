import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.5"

//    id("org.jetbrains.kotlin.jvm") version "1.9.22" // Or your current Kotlin version
//    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.23" // Match Kotlin version
}

group = "ru.app"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

val kotestVersion = "5.9.1"
//val kotestVersion = "6.0.0.M5"
val testcontainersVersion = "1.19.8"

dependencies {
    /* --- Core & WebFlux --- */
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.projectreactor:reactor-core")

    /* --- Persistence --- */
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.postgresql:r2dbc-postgresql:1.0.7.RELEASE")

    /* --- Миграции Liquibase --- */
//    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    runtimeOnly("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")

    // debezium
//    implementation("io.debezium:debezium-embedded:3.2.0.Final")
//    implementation("io.debezium:debezium-connector-postgres:3.2.0.Final")

    // jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    /* --- Observability (optional) --- */
    implementation("io.micrometer:micrometer-observation")
    implementation("io.micrometer:micrometer-registry-prometheus")

    /* --- Tests --- */
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
//    testImplementation("io.kotest.extensions:kotest-extensions-spring:$$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

//    testImplementation(platform("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    // MockWebServer
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}

tasks.test {
    useJUnitPlatform()
}

/* Optional: explicit main class for `bootRun`/native images */
//springBoot {
//    mainClass.set("com.example.news.NewsServiceApplicationKt")
//}
