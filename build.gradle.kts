plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.0.0"
  kotlin("plugin.spring") version "2.2.10"
  id("io.kotest") version "6.0.2"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.5.0") {
    implementation("org.apache.commons:commons-lang3:3.18.0")
  }
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.5.0")
  testImplementation("org.wiremock:wiremock-standalone:3.13.1")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.31") {
    exclude(group = "io.swagger.core.v3")
  }

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.kotest:kotest-framework-engine:6.0.2")
  testImplementation("io.kotest:kotest-runner-junit5:6.0.2")
  testImplementation("io.kotest:kotest-extensions-spring:6.0.2")
}

kotlin {
  jvmToolchain(21)
}

tasks.test.configure {
  systemProperty("kotest.framework.config.fqn", "uk.gov.justice.digital.hmpps.prisonerbaselocationapi.KoTestConfig")
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
