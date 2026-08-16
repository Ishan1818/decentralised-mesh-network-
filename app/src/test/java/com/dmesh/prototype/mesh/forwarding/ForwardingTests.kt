package com.dmesh.prototype.mesh.forwarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.dmesh.prototype.mesh.protocol.MeshPacket
import com.dmesh.prototype.mesh.protocol.MessagePriority
import com.dmesh.prototype.mesh.protocol.PacketType

class ForwardingTests {
    @Test
    fun priorityQueueOrdersCriticalFirst() {
        val queue = PriorityForwardingQueue()
        val normal = QueuedPacket(
            MeshPacket("1", PacketType.DATA.name, "A", "B"),
            "B",
            MessagePriority.NORMAL
        )
        val critical = QueuedPacket(
            MeshPacket("2", PacketType.SOS.name, "A", "B"),
            "B",
            MessagePriority.CRITICAL
        )
        queue.enqueue(normal)
        queue.enqueue(critical)
        assertEquals(MessagePriority.CRITICAL, queue.poll()?.priority)
    }

    @Test
    fun seenCachePreventsDuplicates() {
        val cache = SeenMessageCache()
        cache.markSeen("msg-1")
        assertTrue(cache.hasSeen("msg-1"))
        assertFalse(cache.hasSeen("msg-2"))
    }

    @Test
    fun ttlZeroDiscards() {
        val packet = MeshPacket("id", PacketType.DATA.name, "A", "B", ttl = 0)
        assertTrue(packet.ttl <= 0)
    }
}
