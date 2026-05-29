package com.roadsafety.roadsos.detection

import android.content.Context
import android.util.Log

class AccidentDetector(
    private val context: Context,
    private val onAccidentDetected: (CrashEvent) -> Unit
) {
    companion object {
        const val IMPACT_THRESHOLD_G = 3.4f
        const val DROP_THRESHOLD_G = 2.0f
        const val SPEED_DROP_THRESHOLD = 20f
        const val MIN_SPEED_FOR_DETECTION = 20f
        const val CONFIRMATION_WINDOW_MS = 2000L
        const val FREEFALL_THRESHOLD_G = 0.3f
        const val COOLDOWN_MS = 15000L
        const val GYRO_THRESHOLD = 6.0f
    }

    private var lastSpeed = 0f
    private var impactTime = 0L
    private var freefallTime = 0L
    private var lastEventTime = 0L
    private var isFiring = false

    @Synchronized
    fun onSensorUpdate(gForce: Float, gyroMagnitude: Float, speed: Float, lat: Double, lng: Double) {
        val now = System.currentTimeMillis()

        if (isFiring) return
        if ((now - lastEventTime) < COOLDOWN_MS) return

        val speedDrop = lastSpeed - speed
        val isRecentFreefall = (now - freefallTime) < CONFIRMATION_WINDOW_MS
        val isRecentImpact = (now - impactTime) < CONFIRMATION_WINDOW_MS
        val isSuddenStop = speedDrop > SPEED_DROP_THRESHOLD && lastSpeed > MIN_SPEED_FOR_DETECTION

        // Gyro alone — violent rotation (bike tip, rollover)
        val isViolentRotation = gyroMagnitude > GYRO_THRESHOLD * 2

        when {
            // Freefall detected
            gForce < FREEFALL_THRESHOLD_G -> {
                freefallTime = now
                impactTime = 0L
                Log.d("ACCIDENT", "📉 Freefall! G-Force: $gForce")
            }

            // Freefall + impact = drop/fall
            isRecentFreefall && gForce > DROP_THRESHOLD_G -> {
                fire(now, gForce, lat, lng, speed, "Drop")
            }

            // Hard impact while moving
            gForce > IMPACT_THRESHOLD_G -> {
                impactTime = now
                Log.d("ACCIDENT", "⚠️ Impact recorded! G-Force: $gForce Gyro: $gyroMagnitude")
            }

            // Impact + sudden stop OR violent rotation = crash
            isRecentImpact && (isSuddenStop || gyroMagnitude > GYRO_THRESHOLD) -> {
                fire(now, gForce, lat, lng, speed, "Crash")
            }

            // Violent rotation alone (rollover/flip)
            isViolentRotation -> {
                impactTime = now
                Log.d("ACCIDENT", "🔄 Violent rotation! Gyro: $gyroMagnitude")
                if (isRecentImpact) fire(now, gForce, lat, lng, speed, "Rollover")
            }
        }

        lastSpeed = speed
    }

    private fun fire(now: Long, gForce: Float, lat: Double, lng: Double, speed: Float, type: String) {
        isFiring = true
        freefallTime = 0L
        impactTime = 0L
        lastEventTime = now
        Log.d("ACCIDENT", "🚨 $type detected! G-Force: $gForce")
        val crashEvent = CrashEvent(now, gForce, lat, lng, speed)
        AccidentBroadcaster.broadcast(context, crashEvent)
        onAccidentDetected(crashEvent)
        isFiring = false
    }
}