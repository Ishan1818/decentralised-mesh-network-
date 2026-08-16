package com.dmesh.prototype

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DMeshApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    lateinit var meshController: MeshController

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        meshController = MeshController(this, applicationScope)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DMesh Mesh Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "dmesh_mesh_channel"
    }
}
