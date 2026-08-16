package com.dmesh.prototype.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dmesh.prototype.ui.MeshViewModel

@Composable
fun HomeScreen(vm: MeshViewModel) {
    val state by vm.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "RESEARCH PROTOTYPE — Not for production emergency use",
            color = Color(0xFFFF7043),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x33FF7043))
                .padding(8.dp)
        )
        Text("Mesh Status", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Your Node: ${state.nodeId}")
                Text("Display Name: ${state.displayName}")
                Text("Battery: ${state.batteryPercent}%")
                Text("Nearby Nodes: ${state.neighbors.size}")
                Text("Active Nodes: ${state.metrics.activeNodes}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Internet: ${if (state.metrics.internetOnline) "ONLINE" else "OFFLINE"}")
                Text("Cellular: ${if (state.metrics.cellularAvailable) "AVAILABLE" else "UNAVAILABLE"}")
                Text(
                    "Mesh: ${if (state.metrics.meshActive) "ACTIVE" else "INACTIVE"}",
                    color = if (state.metrics.meshActive) Color(0xFF81C784) else Color.Gray
                )
                if (state.metrics.meshActive) {
                    Text("Network still operational without Internet")
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("NETWORK STATUS", fontWeight = FontWeight.Bold)
                Text("Nodes discovered: ${state.metrics.nodesDiscovered}")
                Text("Messages queued: ${state.metrics.messagesQueued}")
                Text("Messages delivered: ${state.metrics.messagesDelivered}")
                Text("SOS alerts: ${state.metrics.sosAlerts}")
                Text("Known routes: ${state.metrics.knownRoutes}")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { vm.sendSos("Emergency assistance needed") },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SOS", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("SEND EMERGENCY")
            }
        }
    }
}
