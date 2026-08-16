package com.dmesh.prototype.mesh.storeforward

import com.dmesh.prototype.mesh.protocol.DeliveryState
import com.dmesh.prototype.mesh.protocol.MessageEnvelope
import com.dmesh.prototype.mesh.protocol.MessagePriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PendingMessage(
    val envelope: MessageEnvelope,
    val state: DeliveryState = DeliveryState.CREATED,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 3600_000,
    val routePath: List<String> = emptyList(),
    val retryCount: Int = 0,
    val lastError: String? = null
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
}

class StoreForwardManager {
    private val pending = MutableStateFlow<List<PendingMessage>>(emptyList())
    val pendingFlow: StateFlow<List<PendingMessage>> = pending.asStateFlow()

    fun add(envelope: MessageEnvelope, state: DeliveryState = DeliveryState.QUEUED): PendingMessage {
        val msg = PendingMessage(envelope = envelope, state = state)
        pending.update { it + msg }
        return msg
    }

    fun updateState(messageId: String, state: DeliveryState, routePath: List<String> = emptyList()) {
        pending.update { list ->
            list.map { msg ->
                if (msg.envelope.messageId == messageId) {
                    msg.copy(state = state, routePath = routePath.ifEmpty { msg.routePath })
                } else msg
            }
        }
    }

    fun getRetryable(): List<PendingMessage> = pending.value.filter {
        !it.isExpired() && it.state in setOf(
            DeliveryState.QUEUED, DeliveryState.STORED, DeliveryState.RETRYING,
            DeliveryState.DISCOVERING_ROUTE, DeliveryState.ROUTE_FOUND
        )
    }

    fun expireOldMessages() {
        pending.update { list ->
            list.map { msg ->
                if (msg.isExpired() && msg.state != DeliveryState.DELIVERED) {
                    msg.copy(state = DeliveryState.EXPIRED)
                } else msg
            }
        }
    }

    fun queuedCount(): Int = pending.value.count {
        it.state in setOf(DeliveryState.QUEUED, DeliveryState.STORED, DeliveryState.RETRYING)
    }

    fun storedCount(): Int = pending.value.count { it.state == DeliveryState.STORED }

    fun allMessages(): List<PendingMessage> = pending.value

    fun getById(messageId: String): PendingMessage? =
        pending.value.find { it.envelope.messageId == messageId }

    fun remove(messageId: String) {
        pending.update { it.filterNot { m -> m.envelope.messageId == messageId } }
    }

    fun loadFromDb(messages: List<PendingMessage>) {
        pending.value = messages
    }
}
