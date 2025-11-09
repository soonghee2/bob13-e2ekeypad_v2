package org.example.domain.dto

import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class CombinedResponse(
    val imageBase64: String,
    val keys: List<String>,
    val hashedTimestamp: String,
    val uuid: String
)

data class SubmitHashesRequest(
    @field:NotBlank
    val hashes: String,
    @field:NotBlank
    val uuid: String,
    @field:NotBlank
    val hashedTimestamp: String
)

data class SessionData(
    val hashedTimestamp: String,
    val keyHashMap: Map<String, String>,
    val createdAt: Instant = Instant.now()
)
