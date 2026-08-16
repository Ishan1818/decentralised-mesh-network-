package com.dmesh.prototype.mesh.routing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RouteEntry(
    val destinationId: String,
    val nextHop: String,
    val path: List<String>,
    val hopCount: Int,
    val sequenceNumber: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 120_000,
    val avgRssi: Int = -70,
    val minBattery: Int = 50
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt

    fun score(): Double = RouteScorer.RouteCandidate(
        path = path,
        avgRssi = avgRssi,
        minBattery = minBattery,
        routeAgeSeconds = (System.currentTimeMillis() - createdAt) / 1000
    ).score()
}

class RouteTable {
    private val routes = MutableStateFlow<Map<String, RouteEntry>>(emptyMap())
    val routesFlow: StateFlow<Map<String, RouteEntry>> = routes.asStateFlow()

    private val reverseRoutes = mutableMapOf<String, String>()
    private val seenRreq = mutableMapOf<String, Long>()
    private var sequenceCounter = 1L

    fun nextSequence(): Long = sequenceCounter++

    fun addRoute(entry: RouteEntry) {
        routes.update { current ->
            val existing = current[entry.destinationId]
            if (existing == null || entry.sequenceNumber >= existing.sequenceNumber) {
                current + (entry.destinationId to entry)
            } else current
        }
    }

    fun getRoute(destinationId: String): RouteEntry? =
        routes.value[destinationId]?.takeUnless { it.isExpired() }

    fun allRoutes(): List<RouteEntry> = routes.value.values.filter { !it.isExpired() }

    fun removeRoute(destinationId: String) {
        routes.update { it - destinationId }
    }

    fun addReverseRoute(requestId: String, fromNode: String) {
        reverseRoutes[requestId] = fromNode
    }

    fun getReverseRoute(requestId: String): String? = reverseRoutes[requestId]

    fun hasSeenRreq(requestId: String, ttlMs: Long = 30_000): Boolean {
        val seenAt = seenRreq[requestId]
        if (seenAt == null) return false
        return System.currentTimeMillis() - seenAt < ttlMs
    }

    fun markRreqSeen(requestId: String) {
        seenRreq[requestId] = System.currentTimeMillis()
    }

    fun expireRoutes() {
        routes.update { current ->
            current.filterValues { !it.isExpired() }
        }
    }
}
