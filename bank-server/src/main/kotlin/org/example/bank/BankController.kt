package org.example.bank

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class BankController(
    private val rsaDecryptor: RsaDecryptor
) {
    private val logger = LoggerFactory.getLogger(BankController::class.java)

    @PostMapping("/auth")
    fun receivePayload(@RequestBody payload: ForwardPayload): ResponseEntity<AckResponse> {
        logger.info(
            "bank server received payload | userInput: {}, keyLength: {}, keyHashMap: {}",
            payload.userInput,
            payload.keyLength,
            payload.keyHashMap
        )

        println(
            """
            ===== BANK SERVER RECEIVED =====
            userInput : ${payload.userInput}
            keyLength : ${payload.keyLength}
            keyHashMap: ${payload.keyHashMap}
            timestamp : ${Instant.now()}
            =================================
            """.trimIndent()
        )

        val decryptedUserInput = runCatching { rsaDecryptor.decrypt(payload.userInput) }
            .onFailure { ex -> logger.error("Failed to decrypt userInput", ex) }
            .getOrNull()

        decryptedUserInput?.let {
            println(
                """
                ----- DECRYPTED USER INPUT -----
                $it
                --------------------------------
                """.trimIndent()
            )
        }

        return ResponseEntity.ok(
            AckResponse(
                status = "RECEIVED",
                receivedAt = Instant.now().toString()
            )
        )
    }
}

data class ForwardPayload(
    val userInput: String,
    val keyHashMap: Map<String, String>,
    val keyLength: Int
)

data class AckResponse(
    val status: String,
    val receivedAt: String
)
