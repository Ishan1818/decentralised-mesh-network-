package com.dmesh.prototype.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmesh.prototype.MeshController
import com.dmesh.prototype.mesh.events.MeshEvent
import com.dmesh.prototype.mesh.metrics.NetworkMetrics
import com.dmesh.prototype.mesh.neighbor.NeighborEntry
import com.dmesh.prototype.mesh.protocol.RelayMode
import com.dmesh.prototype.mesh.routing.RouteEntry
import com.dmesh.prototype.mesh.storeforward.PendingMessage
import com.dmesh.prototype.location.NodeLocation
import com.dmesh.prototype.simulation.SimulatedLink
import com.dmesh.prototype.simulation.SimulatedNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MeshUiState(
    val nodeId: String = "",
    val displayName: String = "",
    val batteryPercent: Int = 100,
    val neighbors: List<NeighborEntry> = emptyList(),
    val routes: List<RouteEntry> = emptyList(),
    val messages: List<PendingMessage> = emptyList(),
    val events: List<MeshEvent> = emptyList(),
    val metrics: NetworkMetrics = NetworkMetrics(),
    val activeRoute: List<String> = emptyList(),
    val location: NodeLocation? = null,
    val simulationNodes: List<SimulatedNode> = emptyList(),
    val simulationLinks: List<SimulatedLink> = emptyList(),
    val simulationEnabled: Boolean = false
)

class MeshViewModel(private val controller: MeshController) : ViewModel() {
    private val simulationEnabled = MutableStateFlow(false)

    val uiState: StateFlow<MeshUiState> = combine(
        combine(
            controller.engine.neighborTable.neighborsFlow,
            controller.engine.routeTable.routesFlow,
            controller.engine.storeForwardManager.pendingFlow,
            controller.engine.eventLogger.eventsFlow,
            controller.engine.metricsCollector.metricsFlow
        ) { neighborsMap, routesMap, messages, events, metrics ->
            Tuple5(neighborsMap, routesMap, messages, events, metrics)
        },
        controller.engine.activeRouteFlow,
        controller.locationManager.currentLocation,
        simulationEnabled
    ) { tuple5, activeRoute, location, simEnabled ->
        MeshUiState(
            nodeId = controller.engine.nodeId,
            displayName = controller.identity().getDisplayName(),
            batteryPercent = controller.batteryPercent(),
            neighbors = tuple5.first.values.toList(),
            routes = tuple5.second.values.toList(),
            messages = tuple5.third,
            events = tuple5.fourth,
            metrics = tuple5.fifth,
            activeRoute = activeRoute,
            location = location,
            simulationNodes = if (simEnabled) controller.simulatedTransport.allNodes() else emptyList(),
            simulationLinks = if (simEnabled) controller.simulatedTransport.allLinks() else emptyList(),
            simulationEnabled = simEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MeshUiState())

    fun sendMessage(destinationId: String, text: String) {
        viewModelScope.launch { controller.engine.sendText(destinationId, text) }
    }

    fun sendSos(message: String) {
        viewModelScope.launch {
            val loc = controller.locationManager.getLocationOrNull()
            controller.engine.sendSos(
                message = message,
                lat = loc?.latitude,
                lon = loc?.longitude,
                battery = controller.batteryPercent()
            )
        }
    }

    fun setDisplayName(name: String) = controller.identity().setDisplayName(name)
    fun setRelayMode(mode: RelayMode) { controller.batteryPolicy().configuredMode = mode }
    fun setTtl(ttl: Int) { controller.engine.defaultTtl = ttl }
    fun setLocationSharing(enabled: Boolean) {
        controller.locationManager.setSharingEnabled(enabled)
        if (enabled) controller.locationManager.startUpdates() else controller.locationManager.stopUpdates()
    }
    fun enableSimulation(enabled: Boolean) {
        simulationEnabled.value = enabled
        controller.setSimulationEnabled(enabled)
    }
    fun addVirtualNode(nodeId: String) = controller.addVirtualNode(nodeId)
    fun killNode(nodeId: String) = controller.simulatedTransport.killNode(nodeId)
    fun disableLink(from: String, to: String) = controller.simulatedTransport.disableLink(from, to)
    fun setPacketDrop(percent: Int) = controller.simulatedTransport.setPacketDropPercent(percent)
    fun setCongestion(enabled: Boolean) = controller.simulatedTransport.setHighCongestion(enabled)
    fun bridgeNodes(a: String, b: String) = controller.simulatedTransport.bridgePartition(a, b)
    fun exportLogs(): String = controller.engine.eventLogger.export()

    private data class Tuple5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
}
