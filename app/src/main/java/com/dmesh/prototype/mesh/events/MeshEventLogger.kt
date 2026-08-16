package com.dmesh.prototype.mesh.events

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogCategory {
    DISCOVERY, ROUTING, FORWARDING, MESSAGE, SECURITY, BATTERY, LOCATION, SIMULATION
}

data class MeshEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val category: LogCategory,
    val message: String,
    val detail: String? = null
) {
    fun formattedTime(): String =
        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(timestamp))
}

class MeshEventLogger {
    private val events = MutableStateFlow<List<MeshEvent>>(emptyList())
    val eventsFlow: StateFlow<List<MeshEvent>> = events.asStateFlow()
    private val maxEvents = 500

    fun log(category: LogCategory, message: String, detail: String? = null) {
        events.update { current ->
            (current + MeshEvent(category = category, message = message, detail = detail))
                .takeLast(maxEvents)
        }
    }

    fun clear() {
        events.value = emptyList()
    }

    fun export(): String = events.value.joinToString("\n") { e ->
        "${e.formattedTime()} [${e.category}] ${e.message}${e.detail?.let { " | $it" } ?: ""}"
    }
}
