package com.dmesh.prototype.gateway

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Optional gateway: detects Internet and may upload SOS externally.
 * Mesh continues operating when gateway/Internet is unavailable.
 */
class GatewayManager(private val context: Context) {
    fun isInternetAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun isCellularAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    suspend fun uploadSosIfGateway(payload: String): Boolean {
        if (!isInternetAvailable()) return false
        // Optional external upload stub for prototype demonstrations
        return false
    }
}
