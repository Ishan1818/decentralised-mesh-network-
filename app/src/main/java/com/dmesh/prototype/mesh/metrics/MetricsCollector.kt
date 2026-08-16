package com.dmesh.prototype.mesh.metrics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NetworkMetrics(
    val nodesDiscovered: Int = 0,
    val activeNodes: Int = 0,
    val offlineNodes: Int = 0,
    val knownRoutes: Int = 0,
    val messagesQueued: Int = 0,
    val messagesDelivered: Int = 0,
    val sosAlerts: Int = 0,
    val gatewayNodes: Int = 0,
    val averageLatencyMs: Long = 0,
    val deliveryRate: Float = 0f,
    val routeDiscoveryTimeMs: Long = 0,
    val averageHopCount: Float = 0f,
    val duplicationRate: Float = 0f,
    val messagesDropped: Int = 0,
    val messagesStored: Int = 0,
    val networkDiameter: Int = 0,
    val meshActive: Boolean = false,
    val internetOnline: Boolean = false,
    val cellularAvailable: Boolean = false
)

class MetricsCollector {
    private val metrics = MutableStateFlow(NetworkMetrics())
    val metricsFlow: StateFlow<NetworkMetrics> = metrics.asStateFlow()

    private var latencySum = 0L
    private var latencyCount = 0
    private var hopSum = 0
    private var hopCount = 0
    private var delivered = 0
    private var attempted = 0
    private var duplicates = 0
    private var duplicateTotal = 0

    fun recordDelivery(latencyMs: Long, hops: Int) {
        latencySum += latencyMs
        latencyCount++
        hopSum += hops
        hopCount++
        delivered++
        attempted++
        updateDerived()
    }

    fun recordAttempt() {
        attempted++
        updateDerived()
    }

    fun recordDuplicate() {
        duplicates++
        duplicateTotal++
        updateDerived()
    }

    fun recordDropped() {
        metrics.update { it.copy(messagesDropped = it.messagesDropped + 1) }
    }

    fun recordRouteDiscovery(timeMs: Long) {
        metrics.update { it.copy(routeDiscoveryTimeMs = timeMs) }
    }

    fun updateCounts(
        discovered: Int,
        active: Int,
        offline: Int,
        routes: Int,
        queued: Int,
        sos: Int,
        gateways: Int,
        stored: Int,
        meshActive: Boolean,
        internet: Boolean,
        cellular: Boolean
    ) {
        metrics.update {
            it.copy(
                nodesDiscovered = discovered,
                activeNodes = active,
                offlineNodes = offline,
                knownRoutes = routes,
                messagesQueued = queued,
                messagesDelivered = delivered,
                sosAlerts = sos,
                gatewayNodes = gateways,
                messagesStored = stored,
                meshActive = meshActive,
                internetOnline = internet,
                cellularAvailable = cellular
            )
        }
    }

    fun setNetworkDiameter(diameter: Int) {
        metrics.update { it.copy(networkDiameter = diameter) }
    }

    private fun updateDerived() {
        metrics.update {
            it.copy(
                messagesDelivered = delivered,
                averageLatencyMs = if (latencyCount > 0) latencySum / latencyCount else 0,
                deliveryRate = if (attempted > 0) delivered.toFloat() / attempted else 0f,
                averageHopCount = if (hopCount > 0) hopSum.toFloat() / hopCount else 0f,
                duplicationRate = if (duplicateTotal > 0) duplicates.toFloat() / duplicateTotal else 0f
            )
        }
    }
}
