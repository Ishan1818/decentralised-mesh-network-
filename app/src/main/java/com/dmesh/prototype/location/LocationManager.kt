package com.dmesh.prototype.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager as AndroidLocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NodeLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val lastUpdated: Long = System.currentTimeMillis()
)

class LocationManager(private val context: Context) {
    private val systemLocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
    private val locationFlow = MutableStateFlow<NodeLocation?>(null)
    val currentLocation: StateFlow<NodeLocation?> = locationFlow.asStateFlow()

    private var sharingEnabled = false
    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) = updateLocation(location)
        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) = Unit
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    fun setSharingEnabled(enabled: Boolean) {
        sharingEnabled = enabled
        if (!enabled) stopUpdates()
    }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun requestSingleUpdate() {
        if (!hasPermission()) return
        val providers = listOf(
            AndroidLocationManager.GPS_PROVIDER,
            AndroidLocationManager.NETWORK_PROVIDER
        )
        for (provider in providers) {
            if (systemLocationManager.isProviderEnabled(provider)) {
                val loc = systemLocationManager.getLastKnownLocation(provider)
                if (loc != null) {
                    updateLocation(loc)
                    return
                }
            }
        }
    }

    fun startUpdates(intervalMs: Long = 30_000) {
        if (!hasPermission() || !sharingEnabled) return
        stopUpdates()
        val providers = listOf(
            AndroidLocationManager.GPS_PROVIDER,
            AndroidLocationManager.NETWORK_PROVIDER
        )
        providers.forEach { provider ->
            if (systemLocationManager.isProviderEnabled(provider)) {
                systemLocationManager.requestLocationUpdates(
                    provider,
                    intervalMs,
                    10f,
                    listener
                )
            }
        }
    }

    fun stopUpdates() {
        systemLocationManager.removeUpdates(listener)
    }

    private fun updateLocation(loc: Location) {
        locationFlow.value = NodeLocation(
            latitude = loc.latitude,
            longitude = loc.longitude,
            accuracy = loc.accuracy
        )
    }

    fun getLocationOrNull(): NodeLocation? = locationFlow.value
}
