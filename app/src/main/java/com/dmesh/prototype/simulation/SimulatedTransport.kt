package com.dmesh.prototype.simulation

import com.dmesh.prototype.mesh.protocol.BeaconPayload
import com.dmesh.prototype.mesh.protocol.PacketSerializer
import com.dmesh.prototype.transport.MeshTransport
import com.dmesh.prototype.transport.TransportListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

data class SimulatedNode(
    val nodeId: String,
    val displayName: String = nodeId,
    val battery: Int = 80,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isAlive: Boolean = true,
    val isGateway: Boolean = false
)

data class SimulatedLink(
    val from: String,
    val to: String,
    val enabled: Boolean = true,
    val rssi: Int = -65
)

class SimulatedTransport(
    private val scope: CoroutineScope,
    private val localNodeId: String,
    private val listener: TransportListener
) : MeshTransport {
    override val name = "SIMULATION"

    private val nodes = ConcurrentHashMap<String, SimulatedNode>()
    private val links = ConcurrentHashMap<Pair<String, String>, SimulatedLink>()
    private var packetDropPercent = 0
    private var highCongestion = false

    fun addNode(node: SimulatedNode) {
        nodes[node.nodeId] = node
        if (node.nodeId != localNodeId) {
            addLink(localNodeId, node.nodeId, rssi = -60)
        }
    }

    fun killNode(nodeId: String) {
        nodes[nodeId]?.let { nodes[nodeId] = it.copy(isAlive = false) }
        links.keys.filter { it.first == nodeId || it.second == nodeId }.forEach { links.remove(it) }
        listener.onPeerDisconnected(nodeId)
    }

    fun reviveNode(nodeId: String) {
        nodes[nodeId]?.let { nodes[nodeId] = it.copy(isAlive = true) }
    }

    fun addLink(from: String, to: String, rssi: Int = -65) {
        links[linkKey(from, to)] = SimulatedLink(from, to, enabled = true, rssi = rssi)
        links[linkKey(to, from)] = SimulatedLink(to, from, enabled = true, rssi = rssi)
    }

    fun disableLink(from: String, to: String) {
        links[linkKey(from, to)]?.let { links[linkKey(from, to)] = it.copy(enabled = false) }
        links[linkKey(to, from)]?.let { links[linkKey(to, from)] = it.copy(enabled = false) }
    }

    fun setPacketDropPercent(percent: Int) {
        packetDropPercent = percent.coerceIn(0, 100)
    }

    fun setHighCongestion(enabled: Boolean) {
        highCongestion = enabled
    }

    fun createPartition(groupA: List<String>, groupB: List<String>) {
        groupA.forEach { a ->
            groupB.forEach { b ->
                disableLink(a, b)
            }
        }
    }

    fun bridgePartition(nodeA: String, nodeB: String) {
        addLink(nodeA, nodeB)
    }

    fun allNodes(): List<SimulatedNode> = nodes.values.toList()
    fun allLinks(): List<SimulatedLink> = links.values.filter { it.enabled }.toList()

    override fun start() {
        scope.launch {
            while (isActive) {
                nodes.values.filter { it.isAlive && it.nodeId != localNodeId }.forEach { node ->
                    val link = links[linkKey(localNodeId, node.nodeId)]
                    if (link?.enabled == true) {
                        val beacon = BeaconPayload(
                            nodeId = node.nodeId,
                            battery = node.battery,
                            role = if (node.isGateway) "GATEWAY" else "RELAY",
                            displayName = node.displayName
                        )
                        listener.onBeaconReceived(beacon, link.rssi, node.nodeId)
                    }
                }
                delay(2000)
            }
        }
    }

    override fun stop() = Unit

    override suspend fun sendToPeer(nodeId: String, data: ByteArray): Boolean {
        if (shouldDrop()) return false
        val node = nodes[nodeId]
        if (node == null || !node.isAlive) return false
        val link = links[linkKey(localNodeId, nodeId)]
        if (link == null || !link.enabled) return false
        if (highCongestion) delay(500)
        listener.onPacketReceived(localNodeId, data, name)
        // Simulate multi-hop: deliver to target node's virtual perspective via broadcast to connected
        propagateToNeighbors(nodeId, data, localNodeId)
        return true
    }

    override suspend fun broadcast(data: ByteArray) {
        if (shouldDrop()) return
        neighborsOf(localNodeId).forEach { neighbor ->
            if (nodes[neighbor]?.isAlive == true) {
                listener.onPacketReceived(localNodeId, data, name)
                propagateToNeighbors(neighbor, data, localNodeId)
            }
        }
    }

    override fun isPeerConnected(nodeId: String): Boolean {
        val link = links[linkKey(localNodeId, nodeId)]
        return link?.enabled == true && nodes[nodeId]?.isAlive == true
    }

    private suspend fun propagateToNeighbors(targetNodeId: String, data: ByteArray, fromNodeId: String) {
        // Relay simulation: if packet is destined elsewhere, forward through graph
        val packet = PacketSerializer.deserializePacket(data)
        val dest = packet?.destinationId
        if (dest == null || dest == localNodeId || dest == targetNodeId) return

        val path = findPath(fromNodeId, dest)
        if (path.size >= 2) {
            val nextHop = path[1]
            if (nextHop != localNodeId && links[linkKey(localNodeId, nextHop)]?.enabled == true) {
                delay(100)
                listener.onPacketReceived(fromNodeId, data, name)
            }
        }
    }

    private fun neighborsOf(nodeId: String): List<String> =
        links.values.filter { it.enabled && it.from == nodeId }.map { it.to }

    private fun findPath(from: String, to: String): List<String> {
        if (from == to) return listOf(from)
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<List<String>>()
        queue.add(listOf(from))
        while (queue.isNotEmpty()) {
            val path = queue.removeFirst()
            val current = path.last()
            if (current == to) return path
            if (visited.contains(current)) continue
            visited.add(current)
            neighborsOf(current).forEach { neighbor ->
                if (!visited.contains(neighbor)) {
                    queue.add(path + neighbor)
                }
            }
        }
        return emptyList()
    }

    private fun shouldDrop(): Boolean =
        packetDropPercent > 0 && Random.nextInt(100) < packetDropPercent

    private fun linkKey(from: String, to: String): Pair<String, String> = from to to
}
