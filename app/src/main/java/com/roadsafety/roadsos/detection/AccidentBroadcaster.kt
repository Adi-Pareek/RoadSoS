package com.roadsafety.roadsos.detection

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.roadsafety.roadsos.SOSActivity

object AccidentBroadcaster {
    const val ACTION_ACCIDENT_DETECTED = "com.roadsafety.roadsos.ACCIDENT_DETECTED"
    const val EXTRA_LATITUDE = "latitude"
    const val EXTRA_LONGITUDE = "longitude"
    const val EXTRA_SPEED = "speed"
    const val EXTRA_G_FORCE = "g_force"
    const val EXTRA_TIMESTAMP = "timestamp"

    @RequiresApi(Build.VERSION_CODES.O)
    fun broadcast(context: Context, crashEvent: CrashEvent) {
        // Send local broadcast
        val intent = Intent(ACTION_ACCIDENT_DETECTED).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_LATITUDE, crashEvent.latitude)
            putExtra(EXTRA_LONGITUDE, crashEvent.longitude)
            putExtra(EXTRA_SPEED, crashEvent.speed)
            putExtra(EXTRA_G_FORCE, crashEvent.accelerationForce)
            putExtra(EXTRA_TIMESTAMP, crashEvent.timestamp)
        }
        context.sendBroadcast(intent)

        // Send high priority notification to open SOSActivity
        sendSosNotification(context, crashEvent)

        Log.d("BROADCAST", "🚨 Accident broadcast sent! Lat: ${crashEvent.latitude} | Lng: ${crashEvent.longitude}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendSosNotification(context: Context, crashEvent: CrashEvent) {
        val channelId = "roadsos_sos_alert"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "SOS Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            enableLights(true)
            setBypassDnd(true)
        }
        notificationManager.createNotificationChannel(channel)

        val sosIntent = Intent(context, SOSActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_LATITUDE, crashEvent.latitude)
            putExtra(EXTRA_LONGITUDE, crashEvent.longitude)
            putExtra(EXTRA_G_FORCE, crashEvent.accelerationForce)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, sosIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 Accident Detected!")
            .setContentText("Tap to open SOS screen immediately")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true)  // ← this opens SOSActivity automatically
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(999, notification)
    }
}