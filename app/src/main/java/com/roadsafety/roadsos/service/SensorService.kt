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

class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private lateinit var accidentDetector: AccidentDetector

    private var gyroX = 0f
    private var gyroY = 0f
    private var gyroZ = 0f

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        accidentDetector = AccidentDetector(this@SensorService) { crashEvent ->
            Log.d("ACCIDENT", "🚨 Event fired! G-Force: ${crashEvent.accelerationForce}")
        }

        startForeground(1, buildNotification())
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        Log.d("SENSOR", "SensorService started ✅")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]
                val gyroMagnitude = Math.sqrt((gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ).toDouble()).toFloat()
                Log.d("GYRO", "Gyro magnitude: $gyroMagnitude")
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val gForce = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat() / 9.8f
                val gyroMagnitude = Math.sqrt((gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ).toDouble()).toFloat()

                Log.d("SENSOR", "G-Force: $gForce | Gyro: $gyroMagnitude")

                val location = LocationService.instance
                accidentDetector.onSensorUpdate(
                    gForce,
                    gyroMagnitude,
                    location?.currentSpeed ?: 0f,
                    location?.currentLat ?: 0.0,
                    location?.currentLng ?: 0.0
                )
            }
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
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("RoadSoS Active")
            .setContentText("Monitoring for accidents...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
    }
}