package com.dmesh.prototype.mesh.forwarding

import com.dmesh.prototype.mesh.protocol.MessagePriority
import com.dmesh.prototype.mesh.protocol.MeshPacket
import java.util.PriorityQueue

data class QueuedPacket(
    val packet: MeshPacket,
    val nextHop: String?,
    val priority: MessagePriority,
    val enqueuedAt: Long = System.currentTimeMillis()
) : Comparable<QueuedPacket> {
    override fun compareTo(other: QueuedPacket): Int {
        val p = priority.weight().compareTo(other.priority.weight())
        return if (p != 0) p else enqueuedAt.compareTo(other.enqueuedAt)
    }
}

class PriorityForwardingQueue(private val maxSize: Int = 200) {
    private val queue = PriorityQueue<QueuedPacket>()

    fun enqueue(item: QueuedPacket): Boolean {
        if (queue.size >= maxSize && item.priority != MessagePriority.CRITICAL) return false
        queue.offer(item)
        return true
    }

    fun poll(): QueuedPacket? = queue.poll()

    fun size(): Int = queue.size

    fun clear() = queue.clear()
}

class SeenMessageCache(private val maxSize: Int = 1000) {
    private val seen = LinkedHashMap<String, Long>(maxSize, 0.75f, true)

    fun hasSeen(messageId: String, windowMs: Long = 300_000): Boolean {
        val ts = seen[messageId]
        if (ts == null) return false
        return System.currentTimeMillis() - ts < windowMs
    }

    fun markSeen(messageId: String) {
        if (seen.size >= maxSize) {
            val oldest = seen.entries.firstOrNull()
            if (oldest != null) seen.remove(oldest.key)
        }
        seen[messageId] = System.currentTimeMillis()
    }
}
