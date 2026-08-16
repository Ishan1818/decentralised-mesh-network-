package com.dmesh.prototype.mesh

import com.dmesh.prototype.battery.BatteryRelayPolicy
import com.dmesh.prototype.database.DMeshDatabase
import com.dmesh.prototype.database.EventEntity
import com.dmesh.prototype.database.MessageEntity
import com.dmesh.prototype.database.NodeEntity
import com.dmesh.prototype.database.RouteEntity
import com.dmesh.prototype.database.SeenMessageEntity
import com.dmesh.prototype.mesh.events.LogCategory
import com.dmesh.prototype.mesh.events.MeshEventLogger
import com.dmesh.prototype.mesh.forwarding.PriorityForwardingQueue
import com.dmesh.prototype.mesh.forwarding.QueuedPacket
import com.dmesh.prototype.mesh.forwarding.SeenMessageCache
import com.dmesh.prototype.mesh.metrics.MetricsCollector
import com.dmesh.prototype.mesh.neighbor.NeighborEntry
import com.dmesh.prototype.mesh.neighbor.NeighborTable
import com.dmesh.prototype.mesh.protocol.BeaconPayload
import com.dmesh.prototype.mesh.protocol.DeliveryState
import com.dmesh.prototype.mesh.protocol.MessageEnvelope
import com.dmesh.prototype.mesh.protocol.MessagePriority
import com.dmesh.prototype.mesh.protocol.MessageType
import com.dmesh.prototype.mesh.protocol.MeshPacket
import com.dmesh.prototype.mesh.protocol.NodeStatus
import com.dmesh.prototype.mesh.protocol.PacketSerializer
import com.dmesh.prototype.mesh.protocol.PacketType
import com.dmesh.prototype.mesh.protocol.SosPayload
import com.dmesh.prototype.mesh.routing.RouteEntry
import com.dmesh.prototype.mesh.routing.RoutingEngine
import com.dmesh.prototype.mesh.routing.RouteTable
import com.dmesh.prototype.mesh.storeforward.PendingMessage
import com.dmesh.prototype.mesh.storeforward.StoreForwardManager
import com.dmesh.prototype.security.NodeIdentityManager
import com.dmesh.prototype.transport.MeshTransport
import com.dmesh.prototype.transport.TransportListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeshNetworkEngine(
    private val scope: CoroutineScope,
    private val identityManager: NodeIdentityManager,
    private val batteryRelayPolicy: BatteryRelayPolicy,
    private val database: DMeshDatabase,
    private val batteryPercentProvider: () -> Int
) : TransportListener {
    val nodeId: String = identityManager.getOrCreateNodeId()

    val neighborTable = NeighborTable()
    val routeTable = RouteTable()
    val eventLogger = MeshEventLogger()
    val metricsCollector = MetricsCollector()
    val storeForwardManager = StoreForwardManager()
    val seenMessageCache = SeenMessageCache()
    private val forwardingQueue = PriorityForwardingQueue()

    private val transports = mutableListOf<MeshTransport>()
    private var routingEngine: RoutingEngine? = null
    private var maintenanceJob: Job? = null

    private val activeRouteHighlight = MutableStateFlow<List<String>>(emptyList())
    val activeRouteFlow: StateFlow<List<String>> = activeRouteHighlight.asStateFlow()

    var defaultTtl = 12
    var messageExpirationMs = 3600_000L
    var staleTimeoutMs = 15_000L
    var offlineTimeoutMs = 45_000L
    var discoveryIntervalMs = 5_000L
    var isGateway = false
    var internetOnline = false
    var cellularAvailable = false

    fun registerTransport(transport: MeshTransport) {
        transports.add(transport)
    }

    fun start() {
        identityManager.ensureKeyPair()
        routingEngine = RoutingEngine(
            localNodeId = nodeId,
            routeTable = routeTable,
            neighborTable = neighborTable,
            eventLogger = eventLogger,
            onSendPacket = { packet, nextHop -> sendMeshPacket(packet, nextHop) }
        )
        transports.forEach { it.start() }
        scope.launch { loadPersistedState() }
        maintenanceJob = scope.launch {
            while (isActive) {
                maintainNetwork()
                delay(discoveryIntervalMs)
            }
        }
        eventLogger.log(LogCategory.DISCOVERY, "Mesh engine started", nodeId)
    }

    fun stop() {
        maintenanceJob?.cancel()
        transports.forEach { it.stop() }
    }

    suspend fun sendText(destinationId: String, text: String): String {
        val envelope = MessageEnvelope(
            messageId = PacketSerializer.newMessageId(),
            sourceId = nodeId,
            destinationId = destinationId,
            timestamp = System.currentTimeMillis(),
            ttl = defaultTtl,
            priority = MessagePriority.NORMAL.name,
            type = MessageType.TEXT.name,
            payload = text
        )
        return sendEnvelope(envelope)
    }

    suspend fun sendSos(message: String, lat: Double?, lon: Double?, battery: Int): String {
        val envelope = MessageEnvelope(
            messageId = PacketSerializer.newMessageId(),
            sourceId = nodeId,
            destinationId = "BROADCAST",
            timestamp = System.currentTimeMillis(),
            ttl = defaultTtl,
            priority = MessagePriority.CRITICAL.name,
            type = MessageType.SOS.name,
            payload = message,
            latitude = lat,
            longitude = lon,
            battery = battery
        )
        eventLogger.log(LogCategory.MESSAGE, "SOS sent", message)
        metricsCollector.updateCounts(
            discovered = neighborTable.allNeighbors().size,
            active = neighborTable.onlineCount(),
            offline = neighborTable.allNeighbors().count { it.statuses.contains(NodeStatus.OFFLINE) },
            routes = routeTable.allRoutes().size,
            queued = storeForwardManager.queuedCount(),
            sos = storeForwardManager.allMessages().count { it.envelope.type == MessageType.SOS.name } + 1,
            gateways = if (isGateway) 1 else 0,
            stored = storeForwardManager.storedCount(),
            meshActive = transports.isNotEmpty(),
            internet = internetOnline,
            cellular = cellularAvailable
        )
        return sendEnvelope(envelope)
    }

    private suspend fun sendEnvelope(envelope: MessageEnvelope): String {
        val unsigned = envelope.copy(signature = "")
        val signedPayload = PacketSerializer.serializeEnvelope(unsigned)
        val signature = identityManager.signPayload(signedPayload)
        val signed = envelope.copy(signature = signature)
        storeForwardManager.add(signed, DeliveryState.CREATED)
        persistMessage(signed, DeliveryState.QUEUED)
        metricsCollector.recordAttempt()
        return deliverOrQueue(signed)
    }

    private suspend fun deliverOrQueue(envelope: MessageEnvelope): String {
        if (envelope.destinationId == nodeId) {
            storeForwardManager.updateState(envelope.messageId, DeliveryState.DELIVERED)
            persistMessage(envelope, DeliveryState.DELIVERED)
            return envelope.messageId
        }

        if (envelope.destinationId == "BROADCAST") {
            storeForwardManager.updateState(envelope.messageId, DeliveryState.FORWARDING)
            val packet = buildDataPacket(envelope)
            broadcastPacket(packet)
            storeForwardManager.updateState(envelope.messageId, DeliveryState.DELIVERED)
            persistMessage(envelope, DeliveryState.DELIVERED)
            return envelope.messageId
        }

        val route = routeTable.getRoute(envelope.destinationId)
        if (route != null) {
            storeForwardManager.updateState(envelope.messageId, DeliveryState.ROUTE_FOUND, route.path)
            activeRouteHighlight.value = route.path
            forwardEnvelope(envelope, route.nextHop, route.path)
            return envelope.messageId
        }

        storeForwardManager.updateState(envelope.messageId, DeliveryState.DISCOVERING_ROUTE)
        persistMessage(envelope, DeliveryState.DISCOVERING_ROUTE)
        routingEngine?.discoverRoute(envelope.destinationId, envelope.messageId, envelope.ttl)
        storeForwardManager.updateState(envelope.messageId, DeliveryState.STORED)
        persistMessage(envelope, DeliveryState.STORED)
        eventLogger.log(LogCategory.MESSAGE, "Message stored locally — waiting for network", envelope.messageId)
        return envelope.messageId
    }

    private suspend fun forwardEnvelope(envelope: MessageEnvelope, nextHop: String, path: List<String>) {
        storeForwardManager.updateState(envelope.messageId, DeliveryState.FORWARDING, path)
        persistMessage(envelope, DeliveryState.FORWARDING)
        val packet = buildDataPacket(envelope.copy(route = path))
        enqueuePacket(packet, nextHop, MessagePriority.valueOf(envelope.priority))
        eventLogger.log(LogCategory.FORWARDING, "Message forwarded through $nextHop", path.joinToString(" → "))
    }

    private fun buildDataPacket(envelope: MessageEnvelope): MeshPacket {
        val type = if (envelope.type == MessageType.SOS.name) PacketType.SOS.name else PacketType.DATA.name
        return MeshPacket(
            packetId = envelope.messageId,
            type = type,
            sourceId = envelope.sourceId,
            destinationId = envelope.destinationId,
            ttl = envelope.ttl,
            payload = PacketSerializer.serializeEnvelope(envelope),
            signature = envelope.signature,
            route = envelope.route
        )
    }

    private suspend fun enqueuePacket(packet: MeshPacket, nextHop: String?, priority: MessagePriority) {
        forwardingQueue.enqueue(QueuedPacket(packet, nextHop, priority))
        processForwardingQueue()
    }

    private suspend fun processForwardingQueue() {
        while (forwardingQueue.size() > 0) {
            val item = forwardingQueue.poll() ?: break
            sendMeshPacket(item.packet, item.nextHop)
        }
    }

    private suspend fun sendMeshPacket(packet: MeshPacket, nextHop: String?) {
        val bytes = PacketSerializer.serializePacket(packet)
        if (nextHop != null) {
            var sent = false
            for (transport in transports) {
                if (transport.sendToPeer(nextHop, bytes)) {
                    sent = true
                    break
                }
            }
            if (!sent) {
                eventLogger.log(LogCategory.FORWARDING, "Failed to send to $nextHop")
                metricsCollector.recordDropped()
            }
        } else {
            broadcastPacket(packet)
        }
    }

    private suspend fun broadcastPacket(packet: MeshPacket) {
        val bytes = PacketSerializer.serializePacket(packet)
        transports.forEach { it.broadcast(bytes) }
    }

    override fun onPacketReceived(fromNodeId: String?, data: ByteArray, transport: String) {
        scope.launch {
            val packet = PacketSerializer.deserializePacket(data) ?: return@launch
            handleIncomingPacket(packet, fromNodeId)
        }
    }

    override fun onBeaconReceived(beacon: BeaconPayload, rssi: Int, address: String?) {
        if (beacon.nodeId == nodeId) return
        val entry = NeighborEntry(
            nodeId = beacon.nodeId,
            displayName = beacon.displayName,
            lastSeen = System.currentTimeMillis(),
            rssi = rssi,
            battery = beacon.battery,
            connectionState = "DISCOVERED",
            statuses = setOf(NodeStatus.ONLINE, NodeStatus.RELAY),
            transportAddress = address
        )
        neighborTable.updateNeighbor(entry)
        scope.launch { persistNode(entry) }
        eventLogger.log(LogCategory.DISCOVERY, "${beacon.nodeId} discovered", "RSSI $rssi")
    }

    override fun onPeerConnected(nodeId: String, address: String) {
        val existing = neighborTable.getNeighbor(nodeId)
        neighborTable.updateNeighbor(
            existing?.copy(connectionState = "CONNECTED", transportAddress = address, lastSeen = System.currentTimeMillis())
                ?: NeighborEntry(nodeId = nodeId, connectionState = "CONNECTED", transportAddress = address)
        )
        scope.launch { retryPendingMessages() }
    }

    override fun onPeerDisconnected(nodeId: String) {
        neighborTable.markStale(nodeId, staleTimeoutMs, offlineTimeoutMs)
        routeTable.allRoutes().forEach { route ->
            if (route.path.contains(nodeId)) {
                scope.launch { routingEngine?.sendRerr(route.destinationId, nodeId) }
            }
        }
        eventLogger.log(LogCategory.DISCOVERY, "$nodeId disconnected")
    }

    private suspend fun handleIncomingPacket(packet: MeshPacket, fromNodeId: String?) {
        if (packet.ttl <= 0) {
            metricsCollector.recordDropped()
            return
        }
        if (seenMessageCache.hasSeen(packet.packetId)) {
            metricsCollector.recordDuplicate()
            return
        }
        seenMessageCache.markSeen(packet.packetId)
        database.seenMessageDao().markSeen(SeenMessageEntity(packet.packetId, System.currentTimeMillis()))

        when (packet.type) {
            PacketType.RREQ.name, PacketType.RREP.name, PacketType.RERR.name ->
                routingEngine?.handlePacket(packet, fromNodeId)
            PacketType.DATA.name, PacketType.SOS.name -> handleDataPacket(packet, fromNodeId)
            PacketType.ACK.name -> Unit
            else -> Unit
        }
    }

    private suspend fun handleDataPacket(packet: MeshPacket, fromNodeId: String?) {
        val envelope = PacketSerializer.deserializeEnvelope(packet.payload) ?: return
        if (envelope.signature.isBlank()) {
            eventLogger.log(LogCategory.SECURITY, "Unsigned message rejected", envelope.messageId)
            return
        }
        // Prototype v1: demonstrates signing; full cross-node PKI verification is limited
        eventLogger.log(LogCategory.SECURITY, "Authenticated message from ${envelope.sourceId}")

        if (envelope.destinationId == nodeId || envelope.destinationId == "BROADCAST") {
            storeForwardManager.add(envelope, DeliveryState.DELIVERED)
            persistMessage(envelope, DeliveryState.DELIVERED)
            val hops = packet.route.size.coerceAtLeast(1)
            metricsCollector.recordDelivery(
                System.currentTimeMillis() - envelope.timestamp,
                hops
            )
            if (envelope.type == MessageType.SOS.name) {
                neighborTable.updateNeighbor(
                    neighborTable.getNeighbor(envelope.sourceId)?.copy(statuses = setOf(NodeStatus.SOS, NodeStatus.ONLINE))
                        ?: NeighborEntry(nodeId = envelope.sourceId, statuses = setOf(NodeStatus.SOS, NodeStatus.ONLINE))
                )
            }
            eventLogger.log(LogCategory.MESSAGE, "Message delivered from ${envelope.sourceId}")
            return
        }

        if (!batteryRelayPolicy.shouldRelay(envelope.priority == MessagePriority.CRITICAL.name)) {
            eventLogger.log(LogCategory.BATTERY, "Relay skipped due to battery policy")
            return
        }

        val decremented = envelope.copy(ttl = envelope.ttl - 1)
        if (decremented.ttl <= 0) return

        val route = routeTable.getRoute(decremented.destinationId)
        val nextHop = route?.nextHop ?: fromNodeId
        if (nextHop != null) {
            forwardEnvelope(decremented, nextHop, packet.route + nodeId)
        } else {
            val fwd = packet.copy(ttl = packet.ttl - 1, hopCount = packet.hopCount + 1)
            broadcastPacket(fwd)
        }
    }

    private suspend fun retryPendingMessages() {
        storeForwardManager.getRetryable().forEach { pending ->
            deliverOrQueue(pending.envelope)
        }
    }

    private suspend fun maintainNetwork() {
        neighborTable.refreshStaleStates(staleTimeoutMs, offlineTimeoutMs)
        routeTable.expireRoutes()
        storeForwardManager.expireOldMessages()
        publishBeacon()
        retryPendingMessages()
        updateMetrics()
    }

    private suspend fun publishBeacon() {
        val beacon = BeaconPayload(
            nodeId = nodeId,
            battery = batteryPercentProvider(),
            role = if (isGateway) "GATEWAY" else "RELAY",
            displayName = identityManager.getDisplayName()
        )
        val bytes = PacketSerializer.serializeBeacon(beacon)
        transports.forEach { it.broadcast(bytes) }
    }

    private suspend fun updateMetrics() {
        metricsCollector.updateCounts(
            discovered = neighborTable.allNeighbors().size,
            active = neighborTable.onlineCount(),
            offline = neighborTable.allNeighbors().count { it.statuses.contains(NodeStatus.OFFLINE) },
            routes = routeTable.allRoutes().size,
            queued = storeForwardManager.queuedCount(),
            sos = storeForwardManager.allMessages().count { it.envelope.type == MessageType.SOS.name },
            gateways = neighborTable.allNeighbors().count { it.statuses.contains(NodeStatus.GATEWAY) } + if (isGateway) 1 else 0,
            stored = storeForwardManager.storedCount(),
            meshActive = transports.isNotEmpty(),
            internet = internetOnline,
            cellular = cellularAvailable
        )
    }

    private suspend fun loadPersistedState() = withContext(Dispatchers.IO) {
        database.messageDao().getAll().forEach { entity ->
            val envelope = PacketSerializer.deserializeEnvelope(entity.payload) ?: return@forEach
            storeForwardManager.add(envelope, DeliveryState.valueOf(entity.state))
        }
        database.routeDao().getAll().forEach { entity ->
            routeTable.addRoute(
                RouteEntry(
                    destinationId = entity.destinationId,
                    nextHop = entity.nextHop,
                    path = entity.path.split(",").filter { it.isNotBlank() },
                    hopCount = entity.hopCount,
                    sequenceNumber = entity.sequenceNumber,
                    createdAt = entity.createdAt,
                    expiresAt = entity.expiresAt
                )
            )
        }
    }

    private suspend fun persistMessage(envelope: MessageEnvelope, state: DeliveryState) =
        withContext(Dispatchers.IO) {
            database.messageDao().upsert(
                MessageEntity(
                    messageId = envelope.messageId,
                    sourceId = envelope.sourceId,
                    destinationId = envelope.destinationId,
                    timestamp = envelope.timestamp,
                    priority = envelope.priority,
                    type = envelope.type,
                    payload = PacketSerializer.serializeEnvelope(envelope),
                    state = state.name,
                    routePath = envelope.route.joinToString(","),
                    expiresAt = System.currentTimeMillis() + messageExpirationMs
                )
            )
        }

    private suspend fun persistNode(entry: NeighborEntry) = withContext(Dispatchers.IO) {
        database.nodeDao().upsert(
            NodeEntity(
                nodeId = entry.nodeId,
                displayName = entry.displayName,
                battery = entry.battery,
                rssi = entry.rssi,
                lastSeen = entry.lastSeen,
                latitude = entry.latitude,
                longitude = entry.longitude,
                statuses = entry.statuses.joinToString(","),
                transportAddress = entry.transportAddress
            )
        )
    }
}
