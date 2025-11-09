package org.example.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class BankClient(
    private val restTemplate: RestTemplate,
    @Value("\${bank.server.url:http://localhost:8081}")
    private val bankServerUrl: String
) {

    private val logger = LoggerFactory.getLogger(BankClient::class.java)

    fun forwardPayload(encryptedHashes: String, keyHashMap: Map<String, String>, keyLength: Int = 2048): String {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val body = mapOf(
            "userInput" to encryptedHashes,
            "keyHashMap" to keyHashMap,
            "keyLength" to keyLength
        )

        logger.info("Forwarding payload to bank server {}", bankServerUrl)
        val response: ResponseEntity<String> = restTemplate.exchange(
            "$bankServerUrl/auth",
            HttpMethod.POST,
            HttpEntity(body, headers),
            String::class.java
        )

        if (!response.statusCode.is2xxSuccessful) {
            logger.warn("Bank server responded with non-success status {}", response.statusCode)
            throw BankForwardingException("Failed to forward data to bank server")
        }
        return response.body ?: "ACK"
    }
}

class BankForwardingException(message: String) : RuntimeException(message)
