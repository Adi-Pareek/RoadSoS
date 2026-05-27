package com.roadsafety.roadsos

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.roadsafety.roadsos.detection.AccidentBroadcaster
import com.roadsafety.roadsos.service.LocationService
import com.roadsafety.roadsos.service.SensorService

class DashboardActivity : AppCompatActivity() {

    private lateinit var sosButton: Button
    private lateinit var monitoringSwitch: SwitchMaterial
    private lateinit var monitoringStatus: TextView
    private lateinit var contactsCard: CardView
    private lateinit var hospitalsCard: CardView
    private lateinit var historyCard: CardView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var greetingText: TextView

    private val LOCATION_PERMISSION_REQUEST = 100

    // Broadcast receiver for accident detection
    private val accidentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val sosIntent = Intent(this@DashboardActivity, SOSActivity::class.java).apply {
                putExtra(
                    AccidentBroadcaster.EXTRA_LATITUDE,
                    intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LATITUDE, 0.0)
                )
                putExtra(
                    AccidentBroadcaster.EXTRA_LONGITUDE,
                    intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LONGITUDE, 0.0)
                )
                putExtra(
                    AccidentBroadcaster.EXTRA_G_FORCE,
                    intent.getFloatExtra(AccidentBroadcaster.EXTRA_G_FORCE, 0f)
                )
            }
            startActivity(sosIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        supportActionBar?.hide()

        // Connect UI
        sosButton = findViewById(R.id.sosButton)
        monitoringSwitch = findViewById(R.id.monitoringSwitch)
        monitoringStatus = findViewById(R.id.monitoringStatus)
        contactsCard = findViewById(R.id.contactsCard)
        hospitalsCard = findViewById(R.id.hospitalsCard)
        historyCard = findViewById(R.id.historyCard)
        bottomNav = findViewById(R.id.bottomNav)
        greetingText = findViewById(R.id.greetingText)

        greetingText.text = "Hello, User"
        findViewById<TextView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        requestLocationPermission()

        sosButton.setOnClickListener {
            startActivity(Intent(this, SOSActivity::class.java))
        }

        monitoringSwitch.isChecked = true
        monitoringStatus.text = "● ACTIVE"
        monitoringStatus.setTextColor(getColor(R.color.status_safe))

        monitoringSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startServices()
                monitoringStatus.text = "● ACTIVE"
                monitoringStatus.setTextColor(getColor(R.color.status_safe))
                Toast.makeText(this, "Monitoring activated", Toast.LENGTH_SHORT).show()
            } else {
                stopService(Intent(this, SensorService::class.java))
                stopService(Intent(this, LocationService::class.java))
                monitoringStatus.text = "● INACTIVE"
                monitoringStatus.setTextColor(getColor(R.color.status_danger))
                Toast.makeText(this, "Monitoring deactivated", Toast.LENGTH_SHORT).show()
            }
        }

        contactsCard.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        hospitalsCard.setOnClickListener {

            val intent =
                Intent(
                    this,
                    EmergencyMapActivity::class.java
                )

            intent.putExtra(
                "mode",
                "emergency"
            )

            startActivity(intent)
        }

        historyCard.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_contacts -> {
                    startActivity(Intent(this, ContactsActivity::class.java))
                    true
                }

                R.id.nav_map -> {

                    val intent =
                        Intent(
                            this,
                            EmergencyMapActivity::class.java
                        )

                    intent.putExtra(
                        "mode",
                        "emergency"
                    )

                    startActivity(intent)

                    true
                }

                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(AccidentBroadcaster.ACTION_ACCIDENT_DETECTED)
        registerReceiver(accidentReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(accidentReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startServices()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    private fun requestSmsPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.SEND_SMS
                ),
                101
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

                startServices()

                requestSmsPermission()

            } else {

                Toast.makeText(
                    this,
                    "Location permission required for accident detection",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startServices() {

        startForegroundService(
            Intent(this, SensorService::class.java)
        )

        startForegroundService(
            Intent(this, LocationService::class.java)
        )
    }
}