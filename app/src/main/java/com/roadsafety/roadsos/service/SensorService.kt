package com.roadsafety.roadsos.service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.roadsafety.roadsos.detection.AccidentDetector
import com.roadsafety.roadsos.detection.CrashEvent
import com.roadsafety.roadsos.service.LocationService

class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var accidentDetector: AccidentDetector

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accidentDetector = AccidentDetector(this@SensorService) { crashEvent ->
            Log.d("ACCIDENT", "🚨 Event fired! G-Force: ${crashEvent.accelerationForce}")
        }

        startForeground(1, buildNotification())
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        Log.d("SENSOR", "SensorService started ✅")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val gForce = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat() / 9.8f

            Log.d("SENSOR", "G-Force: $gForce")

            // Speed = 0 for now, LocationService will provide it later
            val location = LocationService.instance
            accidentDetector.onSensorUpdate(
                gForce,
                location?.currentSpeed ?: 0f,
                location?.currentLat ?: 0.0,
                location?.currentLng ?: 0.0
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.d("SENSOR", "SensorService stopped ❌")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildNotification(): Notification {
        val channelId = "roadsos_sensor"
        val channel = NotificationChannel(
            channelId,
            "RoadSoS Monitoring",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("RoadSoS Active")
            .setContentText("Monitoring for accidents...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()
    }
}