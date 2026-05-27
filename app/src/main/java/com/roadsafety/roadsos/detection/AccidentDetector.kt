package com.roadsafety.roadsos.detection
import android.content.Context
import android.util.Log
import com.roadsafety.roadsos.detection.AccidentBroadcaster

class AccidentDetector(
    private val context: Context,
    private val onAccidentDetected: (CrashEvent) -> Unit
) {
    companion object {
        const val IMPACT_THRESHOLD_G = 3.0f
        const val DROP_THRESHOLD_G = 1.5f
        const val SPEED_DROP_THRESHOLD = 20f
        const val MIN_SPEED_FOR_DETECTION = 15f
        const val CONFIRMATION_WINDOW_MS = 2000L
        const val FREEFALL_THRESHOLD_G = 0.5f
        const val COOLDOWN_MS = 3000L
    }

    private var lastSpeed = 0f
    private var impactTime = 0L
    private var freefallTime = 0L
    private var lastEventTime = 0L
    private var isFiring = false

    @Synchronized
    fun onSensorUpdate(gForce: Float, speed: Float, lat: Double, lng: Double) {
        val now = System.currentTimeMillis()

        if (isFiring) return
        if ((now - lastEventTime) < COOLDOWN_MS) return

        val speedDrop = lastSpeed - speed
        val isRecentFreefall = (now - freefallTime) < CONFIRMATION_WINDOW_MS
        val isRecentImpact = (now - impactTime) < CONFIRMATION_WINDOW_MS
        val isSuddenStop = speedDrop > SPEED_DROP_THRESHOLD && lastSpeed > MIN_SPEED_FOR_DETECTION

        when {
            gForce < FREEFALL_THRESHOLD_G -> {
                freefallTime = now
                impactTime = 0L
                Log.d("ACCIDENT", "📉 Freefall! G-Force: $gForce")
            }

            isRecentFreefall && gForce > DROP_THRESHOLD_G -> {
                isFiring = true
                freefallTime = 0L
                impactTime = 0L
                lastEventTime = now
                lastSpeed = speed
                Log.d("ACCIDENT", "📱 Drop detected! G-Force: $gForce")
                val crashEvent = CrashEvent(now, gForce, lat, lng, speed)
                AccidentBroadcaster.broadcast(context, crashEvent)
                onAccidentDetected(crashEvent)
                isFiring = false
            }

            gForce > IMPACT_THRESHOLD_G && lastSpeed > MIN_SPEED_FOR_DETECTION -> {
                impactTime = now
                Log.d("ACCIDENT", "⚠️ Impact! G-Force: $gForce")
            }

            isRecentImpact && isSuddenStop -> {
                isFiring = true
                impactTime = 0L
                lastEventTime = now
                Log.d("ACCIDENT", "🚨 Crash detected! G-Force: $gForce")
                val crashEvent = CrashEvent(now, gForce, lat, lng, speed)
                AccidentBroadcaster.broadcast(context, crashEvent)
                onAccidentDetected(crashEvent)
                isFiring = false
            }
        }

        lastSpeed = speed
    }
}
