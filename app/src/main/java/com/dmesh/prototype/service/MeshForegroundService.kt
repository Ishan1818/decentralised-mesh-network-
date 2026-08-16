package com.dmesh.prototype.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dmesh.prototype.DMeshApplication
import com.dmesh.prototype.MainActivity
import com.dmesh.prototype.R

class MeshForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        (application as DMeshApplication).meshController.startMesh(useSimulation = false)
    }

    override fun onDestroy() {
        (application as DMeshApplication).meshController.stopMesh()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, DMeshApplication.CHANNEL_ID)
            .setContentTitle("DMesh Prototype Active")
            .setContentText("Decentralized mesh networking is running")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
