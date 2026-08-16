package com.dmesh.prototype.mesh.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

enum class PacketType {
    BEACON, RREQ, RREP, RERR, DATA, SOS, ACK
}

enum class MessagePriority {
    CRITICAL, HIGH, NORMAL, LOW;

    fun weight(): Int = when (this) {
        CRITICAL -> 0
        HIGH -> 1
        NORMAL -> 2
        LOW -> 3
    }
}

enum class MessageType {
    TEXT, SOS, LOCATION, ACK
}

enum class DeliveryState {
    CREATED, QUEUED, DISCOVERING_ROUTE, ROUTE_FOUND, FORWARDING,
    STORED, RETRYING, DELIVERED, EXPIRED, FAILED
}

enum class NodeStatus {
    ONLINE, WEAK, OFFLINE, RELAY, GATEWAY, SOS
}

enum class RelayMode {
    AGGRESSIVE, NORMAL, CONSERVE, MINIMAL
}

@Serializable
data class BeaconPayload(
    val protocol: String = "DMESH",
    val version: Int = 1,
    val nodeId: String,
    val battery: Int,
    val role: String = "RELAY",
    val displayName: String = ""
)

@Serializable
data class MeshPacket(
    val packetId: String,
    val type: String,
    val sourceId: String,
    val destinationId: String? = null,
    val hopCount: Int = 0,
    val ttl: Int = 12,
    val timestamp: Long = System.currentTimeMillis(),
    val payload: String = "",
    val signature: String = "",
    val route: List<String> = emptyList(),
    val requestId: String? = null,
    val sequenceNumber: Long = 0
)

@Serializable
data class MessageEnvelope(
    val messageId: String,
    val sourceId: String,
    val destinationId: String,
    val timestamp: Long,
    val ttl: Int,
    val priority: String,
    val type: String,
    val payload: String,
    val signature: String = "",
    val route: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val battery: Int? = null
)

@Serializable
data class SosPayload(
    val type: String = "SOS",
    val priority: String = "CRITICAL",
    val source: String,
    val timestamp: Long,
    val location: LocationPayload? = null,
    val message: String = "",
    val battery: Int = 0
)

@Serializable
data class LocationPayload(
    val lat: Double,
    val lon: Double,
    val accuracy: Float? = null
)

object PacketJson {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}
