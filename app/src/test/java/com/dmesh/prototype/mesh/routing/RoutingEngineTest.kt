package com.dmesh.prototype.mesh.routing

import com.dmesh.prototype.mesh.events.MeshEventLogger
import com.dmesh.prototype.mesh.neighbor.NeighborTable
import com.dmesh.prototype.mesh.protocol.MeshPacket
import com.dmesh.prototype.mesh.protocol.PacketSerializer
import com.dmesh.prototype.mesh.protocol.PacketType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutingEngineTest {
    @Test
    fun routeRequestIsForwarded() = runBlocking {
        val routeTable = RouteTable()
        val neighborTable = NeighborTable()
        neighborTable.updateNeighbor(
            com.dmesh.prototype.mesh.neighbor.NeighborEntry(nodeId = "NODE-B")
        )
        val sent = mutableListOf<MeshPacket>()
        val engine = RoutingEngine(
            localNodeId = "NODE-A",
            routeTable = routeTable,
            neighborTable = neighborTable,
            eventLogger = MeshEventLogger(),
            onSendPacket = { packet, _ -> sent.add(packet) }
        )
        engine.discoverRoute("NODE-D", "req-1", 12)
        assertTrue(sent.any { it.type == PacketType.RREQ.name })
    }

    @Test
    fun routeReplyEstablishesRoute() = runBlocking {
        val routeTable = RouteTable()
        val neighborTable = NeighborTable()
        val logger = MeshEventLogger()
        val engine = RoutingEngine(
            localNodeId = "NODE-A",
            routeTable = routeTable,
            neighborTable = neighborTable,
            eventLogger = logger,
            onSendPacket = { _, _ -> }
        )
        val requestId = "req-42"
        val rreq = MeshPacket(
            packetId = PacketSerializer.newPacketId(),
            type = PacketType.RREQ.name,
            sourceId = "NODE-X",
            destinationId = "NODE-A",
            requestId = requestId,
            ttl = 8
        )
        engine.handlePacket(rreq, "NODE-B")
        val rrep = MeshPacket(
            packetId = PacketSerializer.newPacketId(),
            type = PacketType.RREP.name,
            sourceId = "NODE-X",
            destinationId = "NODE-A",
            requestId = requestId,
            ttl = 8,
            route = listOf("NODE-X", "NODE-B")
        )
        engine.handlePacket(rrep, "NODE-B")
        assertNotNull(routeTable.getRoute("NODE-X"))
    }
}
