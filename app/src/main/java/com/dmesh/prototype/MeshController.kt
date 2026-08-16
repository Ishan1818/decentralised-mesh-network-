package com.dmesh.prototype

import android.content.Context
import com.dmesh.prototype.battery.BatteryMonitor
import com.dmesh.prototype.battery.BatteryRelayPolicy
import com.dmesh.prototype.database.DMeshDatabase
import com.dmesh.prototype.gateway.GatewayManager
import com.dmesh.prototype.location.LocationManager
import com.dmesh.prototype.mesh.MeshNetworkEngine
import com.dmesh.prototype.security.NodeIdentityManager
import com.dmesh.prototype.simulation.SimulatedNode
import com.dmesh.prototype.simulation.SimulatedTransport
import com.dmesh.prototype.transport.ble.BleTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MeshController(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val identityManager = NodeIdentityManager(context)
    private val batteryMonitor = BatteryMonitor(context)
    private val batteryRelayPolicy = BatteryRelayPolicy(batteryMonitor)
    private val database = DMeshDatabase.get(context)
    val locationManager = LocationManager(context)

    val engine: MeshNetworkEngine = MeshNetworkEngine(
        scope = scope,
        identityManager = identityManager,
        batteryRelayPolicy = batteryRelayPolicy,
        database = database,
        batteryPercentProvider = { batteryMonitor.batteryPercent() }
    )

    private val bleTransport = BleTransport(
        context = context,
        scope = scope,
        localNodeId = engine.nodeId,
        batteryProvider = { batteryMonitor.batteryPercent() },
        displayNameProvider = { identityManager.getDisplayName() },
        listener = engine
    )

    val simulatedTransport = SimulatedTransport(
        scope = scope,
        localNodeId = engine.nodeId,
        listener = engine
    )

    private var simulationMode = false

    private var meshStarted = false

    private val gatewayManager = GatewayManager(context)

    fun startMesh(useSimulation: Boolean = false) {
        if (meshStarted) return
        meshStarted = true
        simulationMode = useSimulation
        engine.registerTransport(bleTransport)
        if (useSimulation) {
            engine.registerTransport(simulatedTransport)
            seedDefaultSimulation()
        }
        engine.start()
        locationManager.requestSingleUpdate()
        scope.launch {
            while (isActive) {
                engine.internetOnline = gatewayManager.isInternetAvailable()
                engine.cellularAvailable = gatewayManager.isCellularAvailable()
                delay(5000)
            }
        }
    }

    fun stopMesh() {
        engine.stop()
        bleTransport.stop()
    }

    fun setSimulationEnabled(enabled: Boolean) {
        simulationMode = enabled
        if (enabled) {
            engine.registerTransport(simulatedTransport)
            if (simulatedTransport.allNodes().isEmpty()) seedDefaultSimulation()
            simulatedTransport.start()
        }
    }

    fun seedDefaultSimulation() {
        val nodes = listOf("NODE-B", "NODE-C", "NODE-D", "NODE-E")
        nodes.forEachIndexed { index, id ->
            simulatedTransport.addNode(
                SimulatedNode(
                    nodeId = id,
                    displayName = id,
                    battery = 60 + index * 8,
                    latitude = 26.9124 + index * 0.001,
                    longitude = 75.7873 + index * 0.001
                )
            )
        }
        simulatedTransport.addLink("NODE-B", "NODE-C")
        simulatedTransport.addLink("NODE-C", "NODE-D")
        simulatedTransport.addLink("NODE-D", "NODE-E")
        simulatedTransport.addLink(engine.nodeId, "NODE-B")
    }

    fun addVirtualNode(nodeId: String) {
        simulatedTransport.addNode(SimulatedNode(nodeId = nodeId, displayName = nodeId))
        simulatedTransport.addLink(engine.nodeId, nodeId)
    }

    fun identity(): NodeIdentityManager = identityManager
    fun batteryPolicy(): BatteryRelayPolicy = batteryRelayPolicy
    fun batteryPercent(): Int = batteryMonitor.batteryPercent()
    fun isSimulationMode(): Boolean = simulationMode

    fun activeRoute(): StateFlow<List<String>> = engine.activeRouteFlow
}
