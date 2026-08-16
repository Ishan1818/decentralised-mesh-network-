package com.dmesh.prototype.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import com.dmesh.prototype.mesh.protocol.RelayMode
import com.dmesh.prototype.ui.MeshViewModel

@Composable
fun SettingsScreen(vm: MeshViewModel) {
    val state by vm.uiState.collectAsState()
    var name by remember(state.displayName) { mutableStateOf(state.displayName) }
    var ttl by remember { mutableStateOf("12") }
    var locationSharing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings")
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Display Name") })
        Button(onClick = { vm.setDisplayName(name) }) { Text("Save Name") }
        Text("Relay mode")
        RelayMode.entries.forEach { mode ->
            Button(onClick = { vm.setRelayMode(mode) }) { Text(mode.name) }
        }
        OutlinedTextField(value = ttl, onValueChange = { ttl = it }, label = { Text("TTL") })
        Button(onClick = { ttl.toIntOrNull()?.let { vm.setTtl(it) } }) { Text("Save TTL") }
        RowWithSwitch("Location sharing", locationSharing) {
            locationSharing = it
            vm.setLocationSharing(it)
        }
        Text("Battery policy: configurable via relay mode")
        Text("Discovery interval: 5s (default)")
        Text("Message expiration: 1 hour (default)")
    }
}

@Composable
private fun RowWithSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
