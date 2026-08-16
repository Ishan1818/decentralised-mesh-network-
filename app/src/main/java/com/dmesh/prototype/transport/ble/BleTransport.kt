package com.dmesh.prototype.transport.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.dmesh.prototype.mesh.protocol.BeaconPayload
import com.dmesh.prototype.mesh.protocol.PacketSerializer
import com.dmesh.prototype.transport.MeshTransport
import com.dmesh.prototype.transport.TransportListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BleTransport(
    private val context: Context,
    private val scope: CoroutineScope,
    private val localNodeId: String,
    private val batteryProvider: () -> Int,
    private val displayNameProvider: () -> String,
    private val listener: TransportListener
) : MeshTransport {
    override val name = "BLE"

  companion object {
        val SERVICE_UUID: UUID = UUID.fromString("FEFD0001-0000-1000-8000-00805F9B34FB")
        val MESH_RX_UUID: UUID = UUID.fromString("FEFD0002-0000-1000-8000-00805F9B34FB")
        val BEACON_UUID: UUID = UUID.fromString("FEFD0003-0000-1000-8000-00805F9B34FB")
        const val MAX_CONNECTIONS = 7
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private var advertiser: BluetoothLeAdvertiser? = null
    private var scanner: BluetoothLeScanner? = null
    private var gattServer: BluetoothGattServer? = null

    private val nodeToAddress = ConcurrentHashMap<String, String>()
    private val addressToNode = ConcurrentHashMap<String, String>()
    private val gattConnections = ConcurrentHashMap<String, BluetoothGatt>()
    private val pendingWrites = ConcurrentHashMap<String, ByteArray>()

    private var scanning = false
    private var advertising = false

    @SuppressLint("MissingPermission")
    override fun start() {
        if (adapter == null || !adapter.isEnabled) return
        startGattServer()
        startAdvertising()
        startScanning()
    }

    @SuppressLint("MissingPermission")
    override fun stop() {
        stopScanning()
        stopAdvertising()
        gattConnections.values.forEach { it.close() }
        gattConnections.clear()
        gattServer?.close()
        gattServer = null
    }

    override suspend fun sendToPeer(nodeId: String, data: ByteArray): Boolean {
        val address = nodeToAddress[nodeId]
        if (address == null) {
            connectToPeer(nodeId)
            pendingWrites[nodeId] = data
            return false
        }
        return writeToGatt(address, data)
    }

    @SuppressLint("MissingPermission")
    override suspend fun broadcast(data: ByteArray) {
        gattConnections.keys.forEach { address ->
            writeToGatt(address, data)
        }
        // Also embed in advertisement for beacon-style broadcast of small payloads
        if (data.size <= 20) {
            scope.launch(Dispatchers.Main) { restartAdvertisingWithPayload(data) }
        }
    }

    override fun isPeerConnected(nodeId: String): Boolean =
        nodeToAddress[nodeId]?.let { gattConnections.containsKey(it) } == true

    @SuppressLint("MissingPermission")
    private fun startGattServer() {
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val meshChar = BluetoothGattCharacteristic(
            MESH_RX_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(meshChar)
        gattServer?.addService(service)
    }

  private val gattServerCallback = object : BluetoothGattServerCallback() {
        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            if (characteristic.uuid == MESH_RX_UUID) {
                val fromNode = addressToNode[device.address]
                listener.onPacketReceived(fromNode, value, name)
            }
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            val nodeId = addressToNode[device.address]
            if (newState == BluetoothProfile.STATE_CONNECTED && nodeId != null) {
                listener.onPeerConnected(nodeId, device.address)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED && nodeId != null) {
                listener.onPeerDisconnected(nodeId)
                gattConnections.remove(device.address)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
        advertiser = adapter?.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()
        advertiser?.startAdvertising(settings, data, advertiseCallback)
        advertising = true
    }

    @SuppressLint("MissingPermission")
    private fun restartAdvertisingWithPayload(payload: ByteArray) {
        if (!advertising) return
        advertiser?.stopAdvertising(advertiseCallback)
        val beacon = BeaconPayload(
            nodeId = localNodeId,
            battery = batteryProvider(),
            displayName = displayNameProvider()
        )
        val beaconBytes = PacketSerializer.serializeBeacon(beacon)
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .build()
        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .addServiceData(ParcelUuid(BEACON_UUID), beaconBytes.take(20).toByteArray())
            .build()
        advertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) = Unit
        override fun onStartFailure(errorCode: Int) = Unit
    }

    @SuppressLint("MissingPermission")
    private fun stopAdvertising() {
        if (advertising) advertiser?.stopAdvertising(advertiseCallback)
        advertising = false
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        scanner = adapter?.bluetoothLeScanner
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner?.startScan(listOf(filter), settings, scanCallback)
        scanning = true
    }

    @SuppressLint("MissingPermission")
    private fun stopScanning() {
        if (scanning) scanner?.stopScan(scanCallback)
        scanning = false
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val beaconBytes = result.scanRecord?.getServiceData(ParcelUuid(BEACON_UUID))
            val beacon = beaconBytes?.let { PacketSerializer.deserializeBeacon(it) }
            if (beacon != null && beacon.nodeId != localNodeId) {
                addressToNode[device.address] = beacon.nodeId
                nodeToAddress[beacon.nodeId] = device.address
                listener.onBeaconReceived(beacon, result.rssi, device.address)
                connectIfNeeded(beacon.nodeId, device)
            } else {
                // Unknown peer advertising our service - assign temp id from address
                val tempId = "NODE-${device.address.takeLast(6).replace(":", "")}"
                addressToNode[device.address] = tempId
                nodeToAddress[tempId] = device.address
                val fallbackBeacon = BeaconPayload(nodeId = tempId, battery = 0, displayName = "Unknown")
                listener.onBeaconReceived(fallbackBeacon, result.rssi, device.address)
                connectIfNeeded(tempId, device)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectIfNeeded(nodeId: String, device: BluetoothDevice) {
        if (gattConnections.containsKey(device.address)) return
        if (gattConnections.size >= MAX_CONNECTIONS) return
        device.connectGatt(context, false, gattClientCallback)
    }

    @SuppressLint("MissingPermission")
    private fun connectToPeer(nodeId: String) {
        val address = nodeToAddress[nodeId] ?: return
        val device = adapter?.getRemoteDevice(address) ?: return
        connectIfNeeded(nodeId, device)
    }

    @SuppressLint("MissingPermission")
    private fun writeToGatt(address: String, data: ByteArray): Boolean {
        val gatt = gattConnections[address] ?: return false
        val service = gatt.getService(SERVICE_UUID) ?: return false
        val characteristic = service.getCharacteristic(MESH_RX_UUID) ?: return false
        characteristic.value = data
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        return gatt.writeCharacteristic(characteristic)
    }

    private val gattClientCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val address = gatt.device.address
            val nodeId = addressToNode[address]
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gattConnections[address] = gatt
                gatt.discoverServices()
                if (nodeId != null) listener.onPeerConnected(nodeId, address)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gattConnections.remove(address)
                gatt.close()
                if (nodeId != null) listener.onPeerDisconnected(nodeId)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return
            val nodeId = addressToNode[gatt.device.address]
            pendingWrites[nodeId]?.let { data ->
                writeToGatt(gatt.device.address, data)
                pendingWrites.remove(nodeId)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            val fromNode = addressToNode[gatt.device.address]
            listener.onPacketReceived(fromNode, value, name)
        }
    }
}
