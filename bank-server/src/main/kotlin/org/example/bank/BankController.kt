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

        decryptedUserInput?.let { plaintext ->
            println(
                """
                ----- DECRYPTED USER INPUT -----
                $plaintext
                --------------------------------
                """.trimIndent()
            )

            val recoveredPassword = recoverPassword(plaintext, payload.keyHashMap)
            if (recoveredPassword != null) {
                println(
                    """
                    >>> RECOVERED PASSWORD <<<
                    $recoveredPassword
                    >>>>>>>>>>>>>>>>>>>>>>>>>>
                    """.trimIndent()
                )
            } else {
                logger.warn("Failed to reconstruct password from decrypted data")
            }
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

private fun recoverPassword(concatenatedHashes: String, keyHashMap: Map<String, String>): String? {
    if (keyHashMap.isEmpty()) return null

    val hashLength = keyHashMap.values.firstOrNull()?.length ?: return null
    if (hashLength == 0 || concatenatedHashes.length % hashLength != 0) {
        return null
    }

    val chunks = concatenatedHashes.chunked(hashLength)
    val passwordBuilder = StringBuilder()

    for (chunk in chunks) {
        val matchedDigit = keyHashMap.entries.firstOrNull { it.value == chunk }?.key ?: return null
        passwordBuilder.append(matchedDigit)
    }

    val password = passwordBuilder.toString()
    return if (password.length == 6) password else null
}
