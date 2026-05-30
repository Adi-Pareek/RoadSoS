package com.roadsafety.roadsos.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var lastLocation: Location? = null
    private var lastTime = 0L
    var currentSpeed = 0f
    var currentLat = 0.0
    var currentLng = 0.0

    private var previousSpeed = 0f

    companion object {
        var instance: LocationService? = null
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        startForeground(
            1,
            buildNotification()
        )

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        locationCallback =
            object : LocationCallback() {

                override fun onLocationResult(
                    result: LocationResult
                ) {

                    val location =
                        result.lastLocation ?: return

                    if (location.accuracy > 20f) {
                        Log.d(
                            "LOCATION",
                            "Ignoring inaccurate location: ${location.accuracy}m"
                        )
                        return
                    }

                    currentLat =
                        location.latitude

                    currentLng =
                        location.longitude

                    if (location.accuracy > 20f) {
                        return
                    }

                    if (lastLocation != null) {

                        val distance =
                            location.distanceTo(lastLocation!!)

                        val timeSeconds =
                            (location.time - lastTime) / 1000f

                        if (timeSeconds > 0f) {

                            currentSpeed =
                                ((distance / timeSeconds) * 3.6f)

                            // Filter GPS drift
                            if (currentSpeed < 5f) {
                                currentSpeed = 0f
                            }

                            // Cap unrealistic values
                            if (currentSpeed > 150f) {
                                currentSpeed = previousSpeed
                            }

                            // Smooth speed
                            currentSpeed =
                                (previousSpeed * 0.7f) +
                                        (currentSpeed * 0.3f)

                            previousSpeed = currentSpeed
                        }

                    } else {

                        currentSpeed = 0f
                    }

                    lastLocation = location
                    lastTime = location.time

                    Log.d(
                        "LOCATION",
                        "Speed: ${currentSpeed.toInt()} km/h | Lat: $currentLat | Lng: $currentLng"
                    )
                }
            }

        startLocationUpdates()

        Log.d(
            "LOCATION",
            "LocationService started ✅"
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        return START_STICKY
    }

    private fun startLocationUpdates() {

        val request =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000L
            )
                .apply {

                    setMinUpdateIntervalMillis(
                        2000L
                    )

                    setWaitForAccurateLocation(
                        true
                    )
                }
                .build()

        try {

            fusedLocationClient
                .requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )

        } catch (e: SecurityException) {

            Log.e(
                "LOCATION",
                "Location permission not granted: ${e.message}"
            )
        }
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }

    override fun onDestroy() {

        super.onDestroy()

        fusedLocationClient
            .removeLocationUpdates(
                locationCallback
            )

        instance = null

        Log.d(
            "LOCATION",
            "LocationService stopped ❌"
        )
    }

    private fun buildNotification(): Notification {

        val channelId =
            "roadsos_location"

        val channel =
            NotificationChannel(
                channelId,
                "RoadSoS Location",
                NotificationManager.IMPORTANCE_LOW
            )

        getSystemService(
            NotificationManager::class.java
        ).createNotificationChannel(channel)

        return NotificationCompat.Builder(
            this,
            channelId
        )
            .setContentTitle(
                "RoadSoS Location Active"
            )
            .setContentText(
                "Tracking location for accident detection..."
            )
            .setSmallIcon(
                android.R.drawable.ic_dialog_alert
            )
            .build()
    }
}