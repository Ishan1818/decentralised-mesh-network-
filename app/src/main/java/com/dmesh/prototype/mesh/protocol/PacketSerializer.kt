package com.dmesh.prototype.mesh.protocol

import java.util.UUID

object PacketSerializer {
    fun serializePacket(packet: MeshPacket): ByteArray =
        PacketJson.json.encodeToString(MeshPacket.serializer(), packet).toByteArray(Charsets.UTF_8)

    fun deserializePacket(bytes: ByteArray): MeshPacket? = runCatching {
        PacketJson.json.decodeFromString(MeshPacket.serializer(), bytes.toString(Charsets.UTF_8))
    }.getOrNull()

    fun serializeEnvelope(envelope: MessageEnvelope): String =
        PacketJson.json.encodeToString(MessageEnvelope.serializer(), envelope)

    fun deserializeEnvelope(json: String): MessageEnvelope? = runCatching {
        PacketJson.json.decodeFromString(MessageEnvelope.serializer(), json)
    }.getOrNull()

    fun serializeBeacon(beacon: BeaconPayload): ByteArray =
        PacketJson.json.encodeToString(BeaconPayload.serializer(), beacon).toByteArray(Charsets.UTF_8)

    fun deserializeBeacon(bytes: ByteArray): BeaconPayload? = runCatching {
        PacketJson.json.decodeFromString(BeaconPayload.serializer(), bytes.toString(Charsets.UTF_8))
    }.getOrNull()

    fun newPacketId(): String = UUID.randomUUID().toString()
    fun newMessageId(): String = UUID.randomUUID().toString()
}
