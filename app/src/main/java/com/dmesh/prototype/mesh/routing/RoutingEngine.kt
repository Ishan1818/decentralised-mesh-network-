package com.dmesh.prototype.mesh.routing

import com.dmesh.prototype.mesh.events.LogCategory
import com.dmesh.prototype.mesh.events.MeshEventLogger
import com.dmesh.prototype.mesh.neighbor.NeighborTable
import com.dmesh.prototype.mesh.protocol.MeshPacket
import com.dmesh.prototype.mesh.protocol.PacketSerializer
import com.dmesh.prototype.mesh.protocol.PacketType

class RoutingEngine(
    private val localNodeId: String,
    private val routeTable: RouteTable,
    private val neighborTable: NeighborTable,
    private val eventLogger: MeshEventLogger,
    private val onSendPacket: suspend (MeshPacket, String?) -> Unit
) {
    suspend fun handlePacket(packet: MeshPacket, fromNodeId: String?) {
        when (packet.type) {
            PacketType.RREQ.name -> handleRreq(packet, fromNodeId)
            PacketType.RREP.name -> handleRrep(packet, fromNodeId)
            PacketType.RERR.name -> handleRerr(packet)
            else -> Unit
        }
    }

    suspend fun discoverRoute(destinationId: String, requestId: String, ttl: Int): Boolean {
        if (destinationId == localNodeId) return true
        val existing = routeTable.getRoute(destinationId)
        if (existing != null) return true

        val rreq = MeshPacket(
            packetId = PacketSerializer.newPacketId(),
            type = PacketType.RREQ.name,
            sourceId = localNodeId,
            destinationId = destinationId,
            hopCount = 0,
            ttl = ttl,
            requestId = requestId,
            sequenceNumber = routeTable.nextSequence()
        )
        routeTable.markRreqSeen(requestId)
        eventLogger.log(LogCategory.ROUTING, "Route request to $destinationId", requestId)
        onSendPacket(rreq, null)
        return false
    }

    private suspend fun handleRreq(packet: MeshPacket, fromNodeId: String?) {
        val requestId = packet.requestId ?: return
        if (routeTable.hasSeenRreq(requestId)) return
        routeTable.markRreqSeen(requestId)

        if (fromNodeId != null) {
            routeTable.addReverseRoute(requestId, fromNodeId)
        }

        if (packet.destinationId == localNodeId) {
            val reverse = routeTable.getReverseRoute(requestId)
            if (reverse != null) {
                val path = listOf(localNodeId, reverse)
                val rrep = MeshPacket(
                    packetId = PacketSerializer.newPacketId(),
                    type = PacketType.RREP.name,
                    sourceId = localNodeId,
                    destinationId = packet.sourceId,
                    hopCount = 0,
                    ttl = packet.ttl,
                    requestId = requestId,
                    route = path,
                    sequenceNumber = routeTable.nextSequence()
                )
                eventLogger.log(LogCategory.ROUTING, "Route reply to ${packet.sourceId}")
                onSendPacket(rrep, reverse)
            }
            return
        }

        if (packet.ttl <= 0) return
        val neighbors = neighborTable.allNeighbors().map { it.nodeId }
        val forwarded = packet.copy(
            hopCount = packet.hopCount + 1,
            ttl = packet.ttl - 1
        )
        neighbors.forEach { neighbor ->
            if (neighbor != fromNodeId && neighbor != packet.sourceId) {
                onSendPacket(forwarded, neighbor)
            }
        }
    }

    private suspend fun handleRrep(packet: MeshPacket, fromNodeId: String?) {
        if (packet.destinationId != localNodeId) {
            if (packet.ttl <= 0) return
            val nextHop = packet.route.firstOrNull { it != localNodeId }
            if (nextHop != null) {
                onSendPacket(packet.copy(ttl = packet.ttl - 1), nextHop)
            }
            return
        }

        val fullPath = buildList {
            add(packet.sourceId)
            if (fromNodeId != null) add(fromNodeId)
            add(localNodeId)
        }.distinct()

        val entry = RouteEntry(
            destinationId = packet.sourceId,
            nextHop = fromNodeId ?: packet.sourceId,
            path = fullPath,
            hopCount = fullPath.size - 1,
            sequenceNumber = packet.sequenceNumber,
            avgRssi = neighborTable.getNeighbor(fromNodeId ?: "")?.rssi ?: -70,
            minBattery = neighborTable.getNeighbor(fromNodeId ?: "")?.battery ?: 50
        )
        routeTable.addRoute(entry)
        eventLogger.log(
            LogCategory.ROUTING,
            "Route established ${fullPath.joinToString(" → ")}"
        )
    }

    suspend fun sendRerr(destinationId: String, brokenNode: String) {
        val route = routeTable.getRoute(destinationId)
        if (route == null) return
        routeTable.removeRoute(destinationId)
        eventLogger.log(LogCategory.ROUTING, "Route failed via $brokenNode", destinationId)
        val rerr = MeshPacket(
            packetId = PacketSerializer.newPacketId(),
            type = PacketType.RERR.name,
            sourceId = localNodeId,
            destinationId = destinationId,
            payload = brokenNode,
            ttl = 8
        )
        onSendPacket(rerr, route.nextHop)
    }

    private suspend fun handleRerr(packet: MeshPacket) {
        routeTable.removeRoute(packet.destinationId ?: "")
        eventLogger.log(LogCategory.ROUTING, "Route error for ${packet.destinationId}")
        if (packet.destinationId != null) {
            discoverRoute(packet.destinationId, PacketSerializer.newPacketId(), 12)
        }
    }

    fun getNextHop(destinationId: String): String? =
        routeTable.getRoute(destinationId)?.nextHop
}
