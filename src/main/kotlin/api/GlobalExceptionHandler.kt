package org.example.api

import org.example.service.BankForwardingException
import org.example.service.InvalidSessionException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String?>> {
        val fieldError = ex.bindingResult.fieldError
        val message = fieldError?.defaultMessage ?: "Invalid request"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to message))
    }

    @ExceptionHandler(InvalidSessionException::class)
    fun handleInvalidSession(ex: InvalidSessionException): ResponseEntity<Map<String, String>> {
        logger.warn("Invalid session: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to (ex.message ?: "Invalid session")))
    }

    @ExceptionHandler(BankForwardingException::class)
    fun handleBankForwarding(ex: BankForwardingException): ResponseEntity<Map<String, String>> {
        logger.error("Failed to forward to bank", ex)
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(mapOf("message" to "Bank server communication failed"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<Map<String, String>> {
        logger.error("Unexpected error", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("message" to "Internal server error"))
    }
}
