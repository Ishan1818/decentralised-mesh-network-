package com.dmesh.prototype.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dmesh.prototype.mesh.protocol.NodeStatus
import com.dmesh.prototype.ui.MeshViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MeshScreen(vm: MeshViewModel) {
    val state by vm.uiState.collectAsState()
    val allNodeIds = buildList {
        add(state.nodeId)
        addAll(state.neighbors.map { it.nodeId })
        addAll(state.simulationNodes.map { it.nodeId })
    }.distinct()

    Text("Mesh Graph", modifier = Modifier.padding(16.dp))
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 3
        val positions = allNodeIds.mapIndexed { index, id ->
            val angle = (2 * Math.PI * index / allNodeIds.size.coerceAtLeast(1)).toFloat()
            id to Offset(
                center.x + radius * cos(angle),
                center.y + radius * sin(angle)
            )
        }.toMap()

        val links = state.simulationLinks.ifEmpty {
            state.neighbors.map { com.dmesh.prototype.simulation.SimulatedLink(state.nodeId, it.nodeId) }
        }
        links.forEach { link ->
            val from = positions[link.from]
            val to = positions[link.to]
            if (from != null && to != null) {
                val isActiveRoute = state.activeRoute.contains(link.from) && state.activeRoute.contains(link.to)
                drawLine(
                    color = if (isActiveRoute) Color(0xFF4FC3F7) else Color.Gray.copy(alpha = 0.5f),
                    start = from,
                    end = to,
                    strokeWidth = if (isActiveRoute) 6f else 2f
                )
            }
        }

        positions.forEach { (id, pos) ->
            val neighbor = state.neighbors.find { it.nodeId == id }
            val isOffline = neighbor?.statuses?.contains(NodeStatus.OFFLINE) == true
            val isSos = neighbor?.statuses?.contains(NodeStatus.SOS) == true
            val color = when {
                isSos -> Color.Red
                isOffline -> Color.Gray.copy(alpha = 0.4f)
                id == state.nodeId -> Color(0xFF4FC3F7)
                else -> Color(0xFF81C784)
            }
            drawCircle(color = color, radius = 24f, center = pos)
            drawCircle(color = Color.White, radius = 24f, center = pos, style = Stroke(width = 2f))
        }
    }
}
