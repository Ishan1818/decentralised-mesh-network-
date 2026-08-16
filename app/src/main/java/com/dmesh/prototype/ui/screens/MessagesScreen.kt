package com.dmesh.prototype.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dmesh.prototype.mesh.protocol.DeliveryState
import com.dmesh.prototype.ui.MeshViewModel

@Composable
fun MessagesScreen(vm: MeshViewModel) {
    val state by vm.uiState.collectAsState()
    var destination by remember { mutableStateOf("NODE-E") }
    var text by remember { mutableStateOf("There are 3 injured people at Location X.") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Messages", fontWeight = FontWeight.Bold)
        OutlinedTextField(value = destination, onValueChange = { destination = it }, label = { Text("Destination") })
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Message") })
        Button(onClick = { vm.sendMessage(destination, text) }) { Text("Send Message") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.messages.sortedByDescending { it.envelope.timestamp }) { msg ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${msg.envelope.type} — ${msg.envelope.priority}", fontWeight = FontWeight.Bold)
                        Text("${msg.envelope.sourceId} → ${msg.envelope.destinationId}")
                        Text(msg.envelope.payload)
                        Text(
                            stateLabel(msg.state),
                            color = when (msg.state) {
                                DeliveryState.DELIVERED -> Color(0xFF81C784)
                                DeliveryState.STORED -> Color(0xFFFF7043)
                                DeliveryState.FAILED, DeliveryState.EXPIRED -> Color.Red
                                else -> Color.Gray
                            }
                        )
                        if (msg.routePath.isNotEmpty()) {
                            Text("Path: ${msg.routePath.joinToString(" → ")}")
                        }
                        Text("TTL: ${msg.envelope.ttl}")
                    }
                }
            }
        }
    }
}

private fun stateLabel(state: DeliveryState): String = when (state) {
    DeliveryState.CREATED -> "Created"
    DeliveryState.QUEUED -> "Queued"
    DeliveryState.DISCOVERING_ROUTE -> "Discovering route"
    DeliveryState.ROUTE_FOUND -> "Route found"
    DeliveryState.FORWARDING -> "Forwarding"
    DeliveryState.STORED -> "STORED LOCALLY — WAITING FOR NETWORK"
    DeliveryState.RETRYING -> "Retrying"
    DeliveryState.DELIVERED -> "DELIVERED"
    DeliveryState.EXPIRED -> "Expired"
    DeliveryState.FAILED -> "Failed"
}
