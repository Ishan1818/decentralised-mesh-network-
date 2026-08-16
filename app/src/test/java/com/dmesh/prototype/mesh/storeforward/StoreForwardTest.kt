package com.dmesh.prototype.mesh.storeforward

import com.dmesh.prototype.mesh.protocol.DeliveryState
import com.dmesh.prototype.mesh.protocol.MessageEnvelope
import com.dmesh.prototype.mesh.protocol.MessagePriority
import com.dmesh.prototype.mesh.protocol.MessageType
import org.junit.Assert.assertEquals
import org.junit.Test

class StoreForwardTest {
    @Test
    fun messageStoredWhenNoRoute() {
        val manager = StoreForwardManager()
        val envelope = MessageEnvelope(
            messageId = "m1",
            sourceId = "A",
            destinationId = "F",
            timestamp = System.currentTimeMillis(),
            ttl = 12,
            priority = MessagePriority.NORMAL.name,
            type = MessageType.TEXT.name,
            payload = "partition test"
        )
        manager.add(envelope, DeliveryState.STORED)
        assertEquals(1, manager.storedCount())
        manager.updateState("m1", DeliveryState.DELIVERED, listOf("A", "B", "F"))
        assertEquals(DeliveryState.DELIVERED, manager.getById("m1")?.state)
    }
}
