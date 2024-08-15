package org.example.domain

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.example.service.HashGenerator
import org.example.service.Images_patcher
import org.springframework.http.ResponseEntity

@RestController
class TestController {
    private val hashGenerator = HashGenerator()
    private val hashesMap = hashGenerator.getHashes()

    @GetMapping("/api/hashes")
    fun getHashesAndShuffledItems(): Map<String, Any> {
        // Generate the shuffled items
        val shuffledItems = getShuffledItems()

        // Map shuffled items to their corresponding hashes or an empty string
        val reorderedHashes = mapShuffledItemsToHashes(shuffledItems)

        // Return the result in JSON format
        return mapOf("keys" to reorderedHashes)
    }

    @GetMapping("/api/combined-image")
    fun getCombinedImage(): ResponseEntity<CombinedResponse> {
        // Generate shuffled items
        val shuffledItems = getShuffledItems()

        // Instantiate Images_patcher with the new shuffledItems
        val imagesPatcher = Images_patcher(shuffledItems)
        print(shuffledItems)
        // Generate the base64-encoded image string using imagesPatcher
        val base64Image = imagesPatcher.combineImages()

        // Map shuffled items to their corresponding hashes or an empty string
        val reorderedHashes = mapShuffledItemsToHashes(shuffledItems)
        print(shuffledItems)

        // Return the combined response with both image and reordered hashes
        return ResponseEntity.ok(CombinedResponse(base64Image, reorderedHashes))
    }

    // Define a data class to represent the JSON response for combined image
    data class CombinedResponse(val imageBase64: String, val keys: List<String>)

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
                is Int -> hashesMap[item] ?: "" // Retrieve the hash value if the item is an integer
                "EMPTY" -> ""                   // Assign an empty string if the item is "EMPTY"
                else -> ""                      // Handle any unexpected cases
            }
        }
    }
}

//package org.example.domain
//
//import jakarta.servlet.http.HttpServletResponse
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RestController
//import org.example.service.HashGenerator
//import org.example.service.Images_patcher
//import org.springframework.http.ResponseEntity
//import java.util.Base64
//
//@RestController
//class TestController {
//    private val hashGenerator = HashGenerator()
//    private val hashesMap = hashGenerator.getHashes()
//    private val shuffledItems = getShuffledItems()
//    @GetMapping("/api/hashes")
//    fun getHashesAndShuffledItems(): Map<String, Any> {
//        // Retrieve the hash values
//        //val hashesMap = hashGenerator.getHashes()
//
//        // Generate the shuffled items
//        //val shuffledItems = getShuffledItems()
//
//        // Map shuffled items to their corresponding hashes or an empty string
//        val reorderedHashes = shuffledItems.map { item ->
//            when (item) {
//                is Int -> hashesMap[item] ?: "" // Retrieve the hash value if the item is an integer
//                "EMPTY" -> ""                   // Assign an empty string if the item is "EMPTY"
//                else -> ""                      // Handle any unexpected cases
//            }
//        }
//
//        // Return the result in JSON format
//        return mapOf("keys" to reorderedHashes)
//    }
//
//    @GetMapping("/api/combined-image")
//    fun getCombinedImage(): ResponseEntity<ImageResponse> {
//        // Generate shuffled items each time this endpoint is called
//        val shuffledItems = getShuffledItems()
//
//        // Instantiate Images_patcher with the new shuffledItems
//        val imagesPatcher = Images_patcher(shuffledItems)
//
//        // Generate the base64-encoded image string using imagesPatcher
//        val base64Image = imagesPatcher.combineImages()
//
//        val imgresponse = ImageResponse(base64Image)
//
//        // Return the response as JSON
//        return ResponseEntity.ok(imgresponse)
//    }
//    // Define a data class to represent the JSON response
//    data class ImageResponse(val imageBase64: String)
//
//    // Generates a list of shuffled items
//    fun getShuffledItems(): List<Any> {
//        // 0부터 9까지의 숫자와 빈 문자열 2개를 포함한 리스트 생성
//        val numbers = (0..9).toList()
//        val items = numbers + List(2) { "EMPTY" }
//
//        // 리스트를 랜덤하게 셔플
//        return items.shuffled()
//    }
//}
