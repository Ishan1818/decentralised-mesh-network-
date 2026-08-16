package com.dmesh.prototype.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dmesh.prototype.ui.MeshViewModel

@Composable
fun NodesScreen(vm: MeshViewModel) {
    val state by vm.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Known Nodes")
        LazyColumn {
            items(state.neighbors) { node ->
                Card(modifier = Modifier.padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(node.nodeId)
                        Text("Last seen: ${(System.currentTimeMillis() - node.lastSeen) / 1000}s ago")
                        Text("Signal: ${node.signalLabel()} (${node.rssi} dBm)")
                        Text("Battery: ${node.battery}%")
                        Text("Status: ${node.statuses.joinToString(", ")}")
                        Text("Connection: ${node.connectionState}")
                    }
                }
            }
        }
    }
}
