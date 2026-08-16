package com.dmesh.prototype.mesh.neighbor

import com.dmesh.prototype.mesh.protocol.NodeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NeighborEntry(
    val nodeId: String,
    val displayName: String = "",
    val lastSeen: Long = System.currentTimeMillis(),
    val rssi: Int = -100,
    val battery: Int = 0,
    val connectionState: String = "DISCOVERED",
    val hopDistanceEstimate: Int = 1,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val statuses: Set<NodeStatus> = setOf(NodeStatus.ONLINE),
    val transportAddress: String? = null
) {
    fun signalLabel(): String = when {
        rssi >= -60 -> "Strong"
        rssi >= -80 -> "Medium"
        else -> "Weak"
    }
}

class NeighborTable {
    private val neighbors = MutableStateFlow<Map<String, NeighborEntry>>(emptyMap())
    val neighborsFlow: StateFlow<Map<String, NeighborEntry>> = neighbors.asStateFlow()

    fun updateNeighbor(entry: NeighborEntry) {
        neighbors.update { current ->
            current + (entry.nodeId to entry)
        }
    }

    fun getNeighbor(nodeId: String): NeighborEntry? = neighbors.value[nodeId]

    fun allNeighbors(): List<NeighborEntry> = neighbors.value.values.toList()

    fun onlineCount(): Int = neighbors.value.values.count {
        it.statuses.contains(NodeStatus.ONLINE) || it.statuses.contains(NodeStatus.RELAY)
    }

    fun markStale(nodeId: String, staleTimeoutMs: Long, offlineTimeoutMs: Long) {
        val now = System.currentTimeMillis()
        neighbors.update { current ->
            val entry = current[nodeId] ?: return@update current
            val age = now - entry.lastSeen
            val newStatuses = when {
                age > offlineTimeoutMs -> setOf(NodeStatus.OFFLINE)
                age > staleTimeoutMs -> entry.statuses + NodeStatus.WEAK
                else -> entry.statuses
            }
            current + (nodeId to entry.copy(statuses = newStatuses))
        }
    }

    fun refreshStaleStates(staleTimeoutMs: Long, offlineTimeoutMs: Long) {
        neighbors.value.keys.forEach { markStale(it, staleTimeoutMs, offlineTimeoutMs) }
    }

    fun removeNeighbor(nodeId: String) {
        neighbors.update { it - nodeId }
    }
}
