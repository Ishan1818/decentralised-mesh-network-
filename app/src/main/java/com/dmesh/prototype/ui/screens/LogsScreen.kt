package com.dmesh.prototype.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dmesh.prototype.ui.MeshViewModel

@Composable
fun LogsScreen(vm: MeshViewModel) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Network Event Log")
        Button(onClick = {
            val logs = vm.exportLogs()
            context.openFileOutput("dmesh_logs.txt", android.content.Context.MODE_APPEND).use {
                it.write(logs.toByteArray())
            }
        }) { Text("Export Logs") }
        LazyColumn {
            items(state.events.reversed()) { event ->
                Text("${event.formattedTime()} [${event.category}] ${event.message}")
            }
        }
    }
}
