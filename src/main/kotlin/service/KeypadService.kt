package org.example.service

import org.example.domain.dto.CombinedResponse
import org.example.domain.dto.SessionData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.UUID

@Service
class KeypadService(
    private val hashGenerator: HashGenerator,
    private val keypadImageComposer: KeypadImageComposer,
    private val sessionStore: SessionStore
) {

    private val logger = LoggerFactory.getLogger(KeypadService::class.java)

    fun generateKeypad(): CombinedResponse {
        val shuffledItems = createShuffledItems()
        val hashesMap = hashGenerator.getHashes()
        val imageBase64 = keypadImageComposer.compose(shuffledItems)
        val keys = mapShuffledItemsToHashes(shuffledItems, hashesMap)

        val uuid = UUID.randomUUID().toString()
        val hashedTimestamp = generateHashedTimestamp()

        sessionStore.save(uuid, SessionData(hashedTimestamp = hashedTimestamp, keyHashMap = hashesMap))
        logger.debug("Generated keypad session {}", uuid)

        return CombinedResponse(
            imageBase64 = imageBase64,
            keys = keys,
            hashedTimestamp = hashedTimestamp,
            uuid = uuid
        )
    }

    private fun createShuffledItems(): List<Any> {
        val numbers = (0..9).toList()
        val items = numbers + List(2) { "EMPTY" }
        return items.shuffled()
    }

    private fun mapShuffledItemsToHashes(shuffledItems: List<Any>, hashesMap: Map<String, String>): List<String> {
        return shuffledItems.map { item ->
            when (item) {
                is Int -> hashesMap[item.toString()] ?: ""
                "EMPTY" -> ""
                else -> ""
            }
        }
    }

    private fun generateHashedTimestamp(): String {
        val timestamp = Instant.now().toString()
        val digest = MessageDigest.getInstance("SHA-256").digest(timestamp.toByteArray())
        return Base64.getEncoder().encodeToString(digest)
    }
}
