plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.5.7"
  kotlin("plugin.spring") version "2.4.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

ext["jackson-2-bom.version"] = "2.21.5"
ext["jackson-bom.version"] = "3.1.5"
ext["log4j2.version"] = "2.25.5"
ext["tomcat.version"] = "11.0.24"

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:2.5.0")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.5.0")
  testImplementation("org.springframework.boot:spring-boot-webtestclient")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.45") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("org.awaitility:awaitility-kotlin")
}

kotlin {
  jvmToolchain(25)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
  }
}
