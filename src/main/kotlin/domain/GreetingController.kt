package org.example.domain
import java.util.Base64

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.example.service.HashGenerator
import org.example.service.Images_patcher
import org.springframework.http.ResponseEntity

import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap

import java.time.Instant
import java.util.UUID
import java.security.MessageDigest

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate


@RestController
class TestController {
    private val hashGenerator = HashGenerator()
    private val hashesMap = hashGenerator.getHashes()
    var algorithm = "SHA-256";
    var md = MessageDigest.getInstance(algorithm);

    // Store the generated UUIDs and hashed timestamps for comparison
    private val storedData = ConcurrentHashMap<String, Pair<String, Map<String, String>>>()

    @GetMapping("/api/combined-image")
    fun getCombinedImage(): ResponseEntity<CombinedResponse> {
        val shuffledItems = getShuffledItems()
        val imagesPatcher = Images_patcher(shuffledItems)
        print(shuffledItems)
        val base64Image = imagesPatcher.combineImages()

        // Map shuffled items to their corresponding hashes or an empty string
        val reorderedHashes = mapShuffledItemsToHashes(shuffledItems)

        print(shuffledItems)

        // Generate the timestamp and UUID
        val timestamp = Instant.now().toString()
        val uuid = UUID.randomUUID().toString()
        val hashedTimestamp = md.digest(timestamp.toByteArray());
// Convert hashedTimestamp to Base64 string
        val hashedTimestampStr = Base64.getEncoder().encodeToString(hashedTimestamp)
        // Store the hashed timestamp and hashesMap in the map with the uuid as the key
        storedData[uuid] = Pair(hashedTimestampStr, hashesMap)
        // Convert hashedTimestamp to Base64 string
        //val hashedTimestampStr = Base64.getEncoder().encodeToString(hashedTimestamp)
        return ResponseEntity.ok(CombinedResponse(base64Image, reorderedHashes, hashedTimestampStr, uuid))
    }

    @PostMapping("/api/submit-hashes")
    fun submitHashes(
        @RequestBody request: SubmitHashesRequest
    ): ResponseEntity<String> {
        // 프론트엔드에서 받은 값들을 출력
        //val hashedTimestampStr = Base64.getEncoder().encodeToString(request.hashedTimestamp)
        val hashedTimestampStr =request.hashedTimestamp

        println("Received Hashes from frontend: ${request.hashes}")
        println("Received UUID from frontend: ${request.uuid}")
        println("Received Hashed Timestamp from frontend: $hashedTimestampStr")

        val storedEntry = storedData[request.uuid]
        val receivedHashedTimestamp = Base64.getDecoder().decode(request.hashedTimestamp)
        if (storedEntry != null && storedEntry.first == request.hashedTimestamp) {
            println("Stored Hashed Timestamp: ${storedEntry.first}")
            println("Stored KeyHashMap: ${storedEntry.second}")
            // If the uuid and hashed timestamp match, proceed with the POST request
            val restTemplate = RestTemplate()

            val body = mapOf(
                "userInput" to request.hashes,
                "keyHashMap" to storedEntry.second,
                "keyLength" to 2048
            )

            val headers = HttpHeaders()
            headers.set("Content-Type", "application/json")
            val entity = HttpEntity(body, headers)
            println(request.hashes)
            println(storedEntry.second)
            println("Forwarding Body: $body")

            val response = restTemplate.exchange(
                "http://146.56.119.112:8081/auth",
                HttpMethod.POST,
                entity,
                String::class.java
            )
            val statusCode = response.statusCode
            println("Response Status Code: $statusCode")

// 응답 본문 확인
            val responseBody = response.body
            println("Response Body: $responseBody")

// 응답 헤더 확인
            val responseHeaders = response.headers
            println("Response Headers: $responseHeaders")
            return if (response.statusCode == HttpStatus.OK) {
//                ResponseEntity.ok("Hash validation and forwarding successful!")
                println("Response was successful!")
                ResponseEntity.ok(responseBody)
            } else {
                ResponseEntity.status(response.statusCode).body("Failed to forward data to the external server.")
            }
        }
        else {
            // If there is no match
            return ResponseEntity.status(400).body("Invalid hash or UUID.")
        }
    }


    // Define a data class to represent the JSON response for combined image
    data class CombinedResponse(
        val imageBase64: String,
        val keys: List<String>,
        val hashedTimestamp: String,
        val uuid: String
    )

    data class SubmitHashesRequest(
        val hashes: String,
        val uuid: String,
        val hashedTimestamp: String
    )

    // Generates a list of shuffled items
    private fun getShuffledItems(): List<Any> {
        // 0부터 9까지의 숫자와 빈 문자열 2개를 포함한 리스트 생성
        val numbers = (0..9).toList()
        val items = numbers + List(2) { "EMPTY" }

        // 리스트를 랜덤하게 셔플
        return items.shuffled()
    }

    // Maps shuffled items to their corresponding hashes or an empty string
    private fun mapShuffledItemsToHashes(shuffledItems: List<Any>): List<String> {
        return shuffledItems.map { item ->
            when (item) {
                is Int -> hashesMap[item.toString()] ?: "" // Retrieve the hash value if the item is an integer
                "EMPTY" -> ""                   // Assign an empty string if the item is "EMPTY"
                else -> ""                      // Handle any unexpected cases
            }
        }
    }
}