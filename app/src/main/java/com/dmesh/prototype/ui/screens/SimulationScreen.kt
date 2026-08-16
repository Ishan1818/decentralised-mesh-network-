package com.dmesh.prototype.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dmesh.prototype.ui.MeshViewModel

@Composable
fun SimulationScreen(vm: MeshViewModel) {
    val state by vm.uiState.collectAsState()
    var newNodeId by remember { mutableStateOf("NODE-F") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Disaster Simulation Mode")
        Text("Uses the same routing engine as real BLE mesh")
        RowWithSwitch("Enable simulation", state.simulationEnabled) { vm.enableSimulation(it) }
        Button(onClick = { vm.addVirtualNode(newNodeId) }) { Text("Add Node $newNodeId") }
        Button(onClick = { vm.killNode("NODE-C") }) { Text("Kill NODE-C") }
        Button(onClick = { vm.disableLink("NODE-B", "NODE-C") }) { Text("Disable Link B-C") }
        Button(onClick = { vm.setPacketDrop(30) }) { Text("Drop 30% packets") }
        Button(onClick = { vm.setCongestion(true) }) { Text("High Congestion") }
        Button(onClick = {
            vm.bridgeNodes("NODE-C", "NODE-D")
        }) { Text("Bridge partition C-D") }
        Text("Virtual nodes: ${state.simulationNodes.size}")
        state.simulationNodes.forEach { node ->
            Text("${node.nodeId} battery ${node.battery}% alive=${node.isAlive}")
        }
    }
}

@Composable
private fun RowWithSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
