package com.dmesh.prototype.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.dmesh.prototype.mesh.protocol.RelayMode

class BatteryMonitor(private val context: Context) {
    fun batteryPercent(): Int {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level < 0 || scale <= 0) return 100
        return (level * 100 / scale)
    }

    fun isCharging(): Boolean {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }
}

class BatteryRelayPolicy(private val batteryMonitor: BatteryMonitor) {
    var configuredMode: RelayMode? = null

    fun currentMode(): RelayMode {
        configuredMode?.let { return it }
        val battery = batteryMonitor.batteryPercent()
        return when {
            battery > 50 -> RelayMode.AGGRESSIVE
            battery >= 20 -> RelayMode.NORMAL
            battery >= 10 -> RelayMode.CONSERVE
            else -> RelayMode.MINIMAL
        }
    }

    fun shouldRelay(isCritical: Boolean): Boolean {
        if (isCritical) return true
        return when (currentMode()) {
            RelayMode.AGGRESSIVE -> true
            RelayMode.NORMAL -> true
            RelayMode.CONSERVE -> batteryMonitor.batteryPercent() > 15
            RelayMode.MINIMAL -> false
        }
    }

    fun maxConcurrentRelays(): Int = when (currentMode()) {
        RelayMode.AGGRESSIVE -> 7
        RelayMode.NORMAL -> 5
        RelayMode.CONSERVE -> 3
        RelayMode.MINIMAL -> 1
    }
}
