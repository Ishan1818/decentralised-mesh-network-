package com.dmesh.prototype.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dmesh.prototype.mesh.protocol.MessageType
import com.dmesh.prototype.ui.MeshViewModel

@Composable
fun MapScreen(vm: MeshViewModel) {
    val state by vm.uiState.collectAsState()
    val baseLat = state.location?.latitude ?: 26.9124
    val baseLon = state.location?.longitude ?: 75.7873

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Offline Map View")
        state.location?.let { loc ->
            Text("Current Location")
            Text("Latitude: ${loc.latitude}")
            Text("Longitude: ${loc.longitude}")
            Text("Accuracy: ${loc.accuracy}m")
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scale = 50000f
            fun toOffset(lat: Double, lon: Double): Offset {
                val x = ((lon - baseLon) * scale).toFloat() + size.width / 2
                val y = ((lat - baseLat) * -scale).toFloat() + size.height / 2
                return Offset(x, y)
            }
            state.location?.let {
                drawCircle(Color.Blue, 16f, toOffset(it.latitude, it.longitude))
            }
            state.neighbors.forEachIndexed { i, n ->
                val lat = n.latitude ?: baseLat + i * 0.001
                val lon = n.longitude ?: baseLon + i * 0.001
                drawCircle(Color(0xFF81C784), 12f, toOffset(lat, lon))
            }
            state.messages.filter { it.envelope.type == MessageType.SOS.name }.forEach { msg ->
                val lat = msg.envelope.latitude ?: baseLat
                val lon = msg.envelope.longitude ?: baseLon
                drawCircle(Color.Red, 20f, toOffset(lat, lon))
            }
        }
    }
}
