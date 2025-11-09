package org.example.api

import jakarta.validation.Valid
import org.example.domain.dto.CombinedResponse
import org.example.domain.dto.SubmitHashesRequest
import org.example.service.HashSubmissionService
import org.example.service.KeypadService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class KeypadController(
    private val keypadService: KeypadService,
    private val hashSubmissionService: HashSubmissionService
) {

    private val logger = LoggerFactory.getLogger(KeypadController::class.java)

    @GetMapping("/api/combined-image")
    fun getCombinedImage(): ResponseEntity<CombinedResponse> {
        val response = keypadService.generateKeypad()
        logger.info("Issued keypad for session {}", response.uuid)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/api/submit-hashes")
    fun submitHashes(
        @Valid @RequestBody request: SubmitHashesRequest
    ): ResponseEntity<String> {
        val resultMessage = hashSubmissionService.submit(request)
        logger.info("Successfully processed keypad submission for {}", request.uuid)
        return ResponseEntity.ok(resultMessage)
    }
}
