package com.roadsafety.roadsos.service
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    var currentSpeed = 0f
    var currentLat = 0.0
    var currentLng = 0.0

    companion object {
        var instance: LocationService? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        startForeground(1, buildNotification())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location = result.lastLocation ?: return
                currentLat = location.latitude
                currentLng = location.longitude
                currentSpeed = if (location.hasSpeed()) {
                    location.speed * 3.6f
                } else {
                    0f
                }
                Log.d("LOCATION", "Speed: $currentSpeed kmph | Lat: $currentLat | Lng: $currentLng")
            }
        }

        startLocationUpdates()
        Log.d("LOCATION", "LocationService started ✅")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000L
        ).apply {
            setMinUpdateIntervalMillis(500L)
            setWaitForAccurateLocation(true)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("LOCATION", "Location permission not granted: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        instance = null
        Log.d("LOCATION", "LocationService stopped ❌")
    }

    private fun buildNotification(): Notification {
        val channelId = "roadsos_location"
        val channel = NotificationChannel(
            channelId,
            "RoadSoS Location",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("RoadSoS Location Active")
            .setContentText("Tracking location for accident detection...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()
    }
}