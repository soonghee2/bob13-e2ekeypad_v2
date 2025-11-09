package org.example.service

import org.example.domain.dto.SubmitHashesRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class HashSubmissionService(
    private val sessionStore: SessionStore,
    private val bankClient: BankClient
) {

    private val logger = LoggerFactory.getLogger(HashSubmissionService::class.java)

    fun submit(request: SubmitHashesRequest): String {
        val session = sessionStore.find(request.uuid)
            ?: throw InvalidSessionException("Session not found for uuid ${request.uuid}")

        if (session.hashedTimestamp != request.hashedTimestamp) {
            sessionStore.remove(request.uuid)
            throw InvalidSessionException("Hashed timestamp mismatch for uuid ${request.uuid}")
        }

        return try {
            val response = bankClient.forwardPayload(request.hashes, session.keyHashMap)
            sessionStore.remove(request.uuid)
            response
        } catch (ex: BankForwardingException) {
            logger.error("Bank forwarding failed", ex)
            throw ex
        }
    }
}

class InvalidSessionException(message: String) : RuntimeException(message)
