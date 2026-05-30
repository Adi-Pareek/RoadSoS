package com.roadsafety.roadsos.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import androidx.core.app.NotificationCompat
import com.roadsafety.roadsos.DashboardActivity
import com.roadsafety.roadsos.SOSActivity
import com.roadsafety.roadsos.detection.AccidentBroadcaster
import com.roadsafety.roadsos.detection.AccidentDetector

import com.roadsafety.roadsos.ml.RiskEngine
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private lateinit var accidentDetector: AccidentDetector

    private lateinit var riskEngine: RiskEngine


    private var gyroX = 0f
    private var gyroY = 0f
    private var gyroZ = 0f

    private var lastRiskUpdate = 0L


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)


        riskEngine = RiskEngine(this)


        accidentDetector = AccidentDetector(this@SensorService) { crashEvent ->
            Log.d("ACCIDENT", "🚨 Event fired! G-Force: ${crashEvent.accelerationForce}")
            val intent = Intent(this@SensorService, SOSActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                putExtra(AccidentBroadcaster.EXTRA_LATITUDE, crashEvent.latitude)
                putExtra(AccidentBroadcaster.EXTRA_LONGITUDE, crashEvent.longitude)
                putExtra(AccidentBroadcaster.EXTRA_G_FORCE, crashEvent.accelerationForce)
            }
            startActivity(intent)
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


                // Continuous Risk Monitoring
                val now = System.currentTimeMillis()
                if (now - lastRiskUpdate > 2000) {
                    val riskResult = riskEngine.predictRisk(
                        location?.currentLat ?: 0.0,
                        location?.currentLng ?: 0.0,
                        gForce,
                        gyroMagnitude,
                        location?.currentSpeed ?: 0f
                    )
                    
                    val riskIntent = Intent("ACTION_RISK_UPDATE")
                    riskIntent.putExtra("RISK_SCORE", riskResult.score)
                    riskIntent.putStringArrayListExtra("RISK_REASONS", java.util.ArrayList(riskResult.reasons))
                    LocalBroadcastManager.getInstance(this@SensorService).sendBroadcast(riskIntent)
                    
                    lastRiskUpdate = now
                }

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

    private fun buildNotification(): Notification {
        val channelId = "roadsos_sensor"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("RoadSoS Active")
            .setContentText("Monitoring for accidents...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()
    }
}