package com.roadsafety.roadsos.detection

import android.content.Context
import android.content.Intent
import android.util.Log

object AccidentBroadcaster {
    const val ACTION_ACCIDENT_DETECTED = "com.example.roadsos.ACCIDENT_DETECTED"
    const val EXTRA_LATITUDE = "latitude"
    const val EXTRA_LONGITUDE = "longitude"
    const val EXTRA_SPEED = "speed"
    const val EXTRA_G_FORCE = "g_force"
    const val EXTRA_TIMESTAMP = "timestamp"

    fun broadcast(context: Context, crashEvent: CrashEvent) {
        val intent = Intent(ACTION_ACCIDENT_DETECTED).apply {
            putExtra(EXTRA_LATITUDE, crashEvent.latitude)
            putExtra(EXTRA_LONGITUDE, crashEvent.longitude)
            putExtra(EXTRA_SPEED, crashEvent.speed)
            putExtra(EXTRA_G_FORCE, crashEvent.accelerationForce)
            putExtra(EXTRA_TIMESTAMP, crashEvent.timestamp)
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
        Log.d("BROADCAST", "🚨 Accident broadcast sent! Lat: ${crashEvent.latitude} | Lng: ${crashEvent.longitude}")
    }
}