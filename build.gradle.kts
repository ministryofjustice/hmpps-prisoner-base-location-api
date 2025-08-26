import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.3.7"
  kotlin("plugin.spring") version "2.2.10"
  id("org.owasp.dependencycheck") version "12.1.3"
  id("org.openapi.generator") version "7.13.0"
  id("jacoco")
}

jacoco {
  toolVersion = "0.8.13"
}

apply(plugin = "jacoco")
apply(plugin = "org.openapi.generator")

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.5.0") {
    implementation("org.apache.commons:commons-lang3:3.18.0")
  }
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
  implementation("io.github.microutils:kotlin-logging:3.0.5")

  // Test dependencies
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.5.0")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.kotest:kotest-assertions-json-jvm:5.9.1")
  testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
  testImplementation("io.kotest:kotest-assertions-core-jvm:5.9.1")
  testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
  testImplementation("org.wiremock:wiremock-standalone:3.13.1")
  testImplementation("org.mockito:mockito-core:5.18.0")
  testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.2")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
  testImplementation("com.atlassian.oai:swagger-request-validator-wiremock:2.45.1") {
    // Exclude WireMock artifacts
    exclude(group = "com.github.tomakehurst", module = "wiremock")
    exclude(group = "com.github.tomakehurst", module = "wiremock-jre8")
    exclude(group = "com.github.tomakehurst", module = "wiremock-standalone")

    // Exclude Jetty components to prevent the validator from pulling in conflicting versions
    exclude(group = "org.eclipse.jetty")
    exclude(group = "javax.servlet")
  }
  // Explicitly add all necessary Jetty and Servlet dependencies
  testImplementation("javax.servlet:javax.servlet-api:4.0.1")
  testImplementation("org.eclipse.jetty:jetty-util:12.0.24")
  testImplementation("org.eclipse.jetty:jetty-server:12.0.24")
  testImplementation("org.eclipse.jetty:jetty-http:12.0.24")
  testImplementation("org.eclipse.jetty:jetty-io:12.0.24")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation(kotlin("test"))
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

tasks {
  withType<KotlinCompile> {
    compilerOptions {
      jvmTarget = JvmTarget.JVM_21
      freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
  }
}

// Jacoco code coverage
tasks.named("test") {
  finalizedBy("jacocoTestReport")
}
tasks.named<JacocoReport>("jacocoTestReport") {
  reports {
    html.required.set(true)
  }
}

kotlin {
  jvmToolchain(21)
}

testlogger {
  theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
}

// this is to address JLLeitschuh/ktlint-gradle#809
ktlint {
  version = "1.5.0"
}

dependencyCheck {
  nvd.datafeedUrl = "file:///opt/vulnz/cache"
}
