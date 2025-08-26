package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.controllers

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_GATEWAY
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exception.HmppsAuthFailedException
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.exception.LimitedAccessException
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestControllerAdvice
class HmppsBaseLocationApiExceptionHandler {

  // Custom exceptions
  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<uk.gov.justice.hmpps.kotlin.common.ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Validation exception: {}", e.message) }

  @ExceptionHandler(EntityNotFoundException::class)
  fun handle(e: EntityNotFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        developerMessage = "404 Not found error: ${e.message}",
        userMessage = e.message,
      ),
    ).also { log.info("Not found (404) returned with message {}", e.message) }

  @ExceptionHandler(HmppsAuthFailedException::class)
  fun handleAuthenticationFailedException(e: HmppsAuthFailedException): ResponseEntity<ErrorResponse?>? = ResponseEntity
    .status(BAD_GATEWAY)
    .body(
      ErrorResponse(
        status = BAD_GATEWAY,
        developerMessage = "Authentication error: ${e.message}",
        userMessage = e.message,
      ),
    ).also { log.info("Authentication error in HMPPS Auth: {}", e.message) }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? = ResponseEntity
    .status(INTERNAL_SERVER_ERROR)
    .body(
      ErrorResponse(
        status = INTERNAL_SERVER_ERROR,
        developerMessage = "Unexpected error: ${e.message}",
        userMessage = e.message,
      ),
    ).also { log.info("Unexpected exception: {}", e.message) }

  @ExceptionHandler(WebClientResponseException::class)
  fun handleWebClientResponseException(e: WebClientResponseException): ResponseEntity<ErrorResponse?>? = ResponseEntity
    .status(INTERNAL_SERVER_ERROR)
    .body(
      ErrorResponse(
        status = INTERNAL_SERVER_ERROR,
        developerMessage = "Unable to complete request as an upstream service is not responding",
        userMessage = e.message,
      ),
    ).also { log.info("Upstream service down: {}", e.message) }

  @ExceptionHandler(LimitedAccessException::class)
  fun handleLimitedAccessFailedException(e: LimitedAccessException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(INTERNAL_SERVER_ERROR)
    .body(
      ErrorResponse(
        status = INTERNAL_SERVER_ERROR,
        developerMessage = e.message,
        userMessage = e.message,
      ),
    ).also { log.info("Limited access failure exception: {}", e.message) }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
