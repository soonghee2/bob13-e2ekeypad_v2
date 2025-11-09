package org.example.bank

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BankAuthService(
    private val rsaDecryptor: RsaDecryptor
) {

    private val logger = LoggerFactory.getLogger(BankAuthService::class.java)

    fun processPayload(payload: ForwardPayload): BankProcessingResult {
        val decryptedUserInput = decryptUserInput(payload.userInput)
        val recoveredPassword = decryptedUserInput?.let { recoverPassword(it, payload.keyHashMap) }
        return BankProcessingResult(
            decryptedUserInput = decryptedUserInput,
            recoveredPassword = recoveredPassword
        )
    }

    private fun decryptUserInput(cipherText: String): String? {
        return runCatching { rsaDecryptor.decrypt(cipherText) }
            .onFailure { ex -> logger.error("Failed to decrypt user input", ex) }
            .getOrNull()
    }

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
}

data class BankProcessingResult(
    val decryptedUserInput: String?,
    val recoveredPassword: String?
)
