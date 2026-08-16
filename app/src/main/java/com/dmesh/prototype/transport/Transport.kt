package com.dmesh.prototype.transport

import com.dmesh.prototype.mesh.protocol.BeaconPayload

interface MeshTransport {
    val name: String
    fun start()
    fun stop()
    suspend fun sendToPeer(nodeId: String, data: ByteArray): Boolean
    suspend fun broadcast(data: ByteArray)
    fun isPeerConnected(nodeId: String): Boolean
}

interface TransportListener {
    fun onPacketReceived(fromNodeId: String?, data: ByteArray, transport: String)
    fun onBeaconReceived(beacon: BeaconPayload, rssi: Int, address: String?)
    fun onPeerConnected(nodeId: String, address: String)
    fun onPeerDisconnected(nodeId: String)
}
