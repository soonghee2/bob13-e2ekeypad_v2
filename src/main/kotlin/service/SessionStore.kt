package org.example.service

import org.example.domain.dto.SessionData
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

interface SessionStore {
    fun save(uuid: String, data: SessionData)
    fun find(uuid: String): SessionData?
    fun remove(uuid: String)
}

@Component
class InMemorySessionStore(
    @Value("\${keypad.session-ttl-seconds:300}")
    private val ttlSeconds: Long = 300
) : SessionStore {

    private val store = ConcurrentHashMap<String, SessionData>()

    override fun save(uuid: String, data: SessionData) {
        cleanupExpired()
        store[uuid] = data
    }

    override fun find(uuid: String): SessionData? {
        val data = store[uuid] ?: return null
        return if (isExpired(data)) {
            store.remove(uuid)
            null
        } else {
            data
        }
    }

    override fun remove(uuid: String) {
        store.remove(uuid)
    }

    private fun isExpired(data: SessionData): Boolean {
        val elapsed = Duration.between(data.createdAt, Instant.now())
        return elapsed.seconds >= ttlSeconds
    }

    private fun cleanupExpired() {
        val now = Instant.now()
        store.entries.removeIf { Duration.between(it.value.createdAt, now).seconds >= ttlSeconds }
    }
}
