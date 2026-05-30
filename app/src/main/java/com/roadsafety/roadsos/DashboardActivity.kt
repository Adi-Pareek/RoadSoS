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
import com.google.firebase.auth.FirebaseAuth
import com.roadsafety.roadsos.detection.AccidentBroadcaster
import com.roadsafety.roadsos.service.LocationService
import com.roadsafety.roadsos.service.SensorService
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var sosButton: Button
    private lateinit var monitoringSwitch: SwitchMaterial
    private lateinit var monitoringStatus: TextView
    private lateinit var contactsCard: CardView
    private lateinit var hospitalsCard: CardView
    private lateinit var historyCard: CardView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var greetingText: TextView
    private lateinit var riskScoreText: TextView
    private lateinit var riskRecommendation: TextView
    private lateinit var reasonsContainer: android.widget.LinearLayout

    private val LOCATION_PERMISSION_REQUEST = 100

    // Naye Firebase Managers Add Kiye Hain 👇
    private val auth = FirebaseAuth.getInstance()
    private val firestoreManager = FirestoreManager()





    private val accidentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val sosIntent = Intent(this@DashboardActivity, SOSActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                putExtra(AccidentBroadcaster.EXTRA_LATITUDE, intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LATITUDE, 0.0))
                putExtra(AccidentBroadcaster.EXTRA_LONGITUDE, intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LONGITUDE, 0.0))
                putExtra(AccidentBroadcaster.EXTRA_G_FORCE, intent.getFloatExtra(AccidentBroadcaster.EXTRA_G_FORCE, 0f))
            }
            startActivity(sosIntent)
        }
    }


    private val riskReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val score = intent.getIntExtra("RISK_SCORE", 0)
            val reasons = intent.getStringArrayListExtra("RISK_REASONS") ?: arrayListOf()

            riskScoreText.text = "$score%"
            
            when {
                score < 30 -> {
                    riskScoreText.setTextColor(getColor(R.color.status_safe))
                    riskRecommendation.text = "Low risk. Conditions are safe."
                }
                score < 60 -> {
                    riskScoreText.setTextColor(getColor(R.color.status_warning))
                    riskRecommendation.text = "Moderate risk. Stay alert."
                }
                else -> {
                    riskScoreText.setTextColor(getColor(R.color.status_danger))
                    riskRecommendation.text = "High risk detected! Drive carefully."
                }
            }

            reasonsContainer.removeAllViews()
            for (reason in reasons) {
                val tv = TextView(this@DashboardActivity).apply {
                    text = "• $reason"
                    setTextColor(getColor(R.color.text_secondary))
                    textSize = 12f
                    setPadding(0, 2, 0, 2)
                    textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
                }
                reasonsContainer.addView(tv)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        supportActionBar?.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 102)
        }

        sosButton = findViewById(R.id.sosButton)
        monitoringSwitch = findViewById(R.id.monitoringSwitch)
        monitoringStatus = findViewById(R.id.monitoringStatus)
        contactsCard = findViewById(R.id.contactsCard)
        hospitalsCard = findViewById(R.id.hospitalsCard)
        historyCard = findViewById(R.id.historyCard)
        bottomNav = findViewById(R.id.bottomNav)
        greetingText = findViewById(R.id.greetingText)
        riskScoreText = findViewById(R.id.riskScoreText)
        riskRecommendation = findViewById(R.id.riskRecommendation)
        reasonsContainer = findViewById(R.id.reasonsContainer)

        // MAGIC: Ab hardcoded "Hello, User" ki jagah Firebase se live data load hoga 👇
        loadDashboardUserData()


        greetingText.text = "Hello, User"

        findViewById<TextView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val filter = IntentFilter(AccidentBroadcaster.ACTION_ACCIDENT_DETECTED)
        registerReceiver(accidentReceiver, filter, RECEIVER_NOT_EXPORTED)

        
        LocalBroadcastManager.getInstance(this).registerReceiver(riskReceiver, IntentFilter("ACTION_RISK_UPDATE"))


        // Set switch UI state without triggering listener
        monitoringSwitch.isChecked = false
        monitoringStatus.text = "● INACTIVE"
        monitoringStatus.setTextColor(getColor(R.color.status_danger))

        requestLocationPermission()

        sosButton.setOnClickListener {
            startActivity(Intent(this, SOSActivity::class.java))
        }

        monitoringSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    startServices()
                    monitoringStatus.text = "● ACTIVE"
                    monitoringStatus.setTextColor(getColor(R.color.status_safe))
                    Toast.makeText(this, "Monitoring activated", Toast.LENGTH_SHORT).show()
                } else {
                    monitoringSwitch.isChecked = false
                    Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
                    requestLocationPermission()
                }
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
            startActivity(Intent(this, EmergencyMapActivity::class.java).apply {
                putExtra("mode", "emergency")
            })
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
                    startActivity(Intent(this, EmergencyMapActivity::class.java).apply {
                        putExtra("mode", "emergency")
                    })
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


    // Firebase se User Profile nikal kar Name set karne ka naya function 👇
    private fun loadDashboardUserData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            greetingText.text = "Hello, User"
            return
        }

        firestoreManager.getUserProfile(userId) { userModel, error ->
            if (userModel != null) {
                // Database se registration wala name mil gaya!
                greetingText.text = "Hello, ${userModel.name}"
            } else {
                greetingText.text = "Hello, User"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(accidentReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(riskReceiver)

    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            requestBackgroundLocation()
            enableMonitoring()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    private fun enableMonitoring() {
        startServices()
        monitoringSwitch.isChecked = true
        monitoringStatus.text = "● ACTIVE"
        monitoringStatus.setTextColor(getColor(R.color.status_safe))
    }

    private fun requestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 101)
        }
    }

    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 103)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestBackgroundLocation()
                    enableMonitoring()
                    requestSmsPermission()
                } else {
                    Toast.makeText(this, "Location permission required for accident detection", Toast.LENGTH_LONG).show()
                }
            }
            101 -> {
                val msg = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    "24/7 monitoring enabled!" else "Background location denied. Keep app open for monitoring."
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startServices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, SensorService::class.java))
            startForegroundService(Intent(this, LocationService::class.java))
        }
    }
}