package com.roadsafety.roadsos.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.roadsafety.roadsos.SOSActivity
import com.roadsafety.roadsos.detection.AccidentBroadcaster

class AccidentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AccidentBroadcaster.ACTION_ACCIDENT_DETECTED) {
            Log.d("ACCIDENT_RECEIVER", "🚨 Accident detected — launching SOSActivity")

            val sosIntent = Intent(context, SOSActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(AccidentBroadcaster.EXTRA_LATITUDE, intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LATITUDE, 0.0))
                putExtra(AccidentBroadcaster.EXTRA_LONGITUDE, intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LONGITUDE, 0.0))
                putExtra(AccidentBroadcaster.EXTRA_G_FORCE, intent.getFloatExtra(AccidentBroadcaster.EXTRA_G_FORCE, 0f))
            }
            context.startActivity(sosIntent)
        }
    }
}