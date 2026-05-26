package com.roadsafety.roadsos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial

class DashboardActivity : AppCompatActivity() {

    private lateinit var sosButton: Button
    private lateinit var monitoringSwitch: SwitchMaterial
    private lateinit var monitoringStatus: TextView
    private lateinit var contactsCard: CardView
    private lateinit var hospitalsCard: CardView
    private lateinit var historyCard: CardView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var greetingText: TextView

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

        // Set greeting
        greetingText.text = "Hello, User"
        findViewById<TextView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // SOS Button
        sosButton.setOnClickListener {
            startActivity(Intent(this, SOSActivity::class.java))
        }

        // Monitoring Toggle
        monitoringSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                monitoringStatus.text = "● ACTIVE"
                monitoringStatus.setTextColor(getColor(R.color.status_safe))
                Toast.makeText(this, "Monitoring activated", Toast.LENGTH_SHORT).show()
            } else {
                monitoringStatus.text = "● INACTIVE"
                monitoringStatus.setTextColor(getColor(R.color.status_danger))
                Toast.makeText(this, "Monitoring deactivated", Toast.LENGTH_SHORT).show()
            }
        }

        // Quick Action Cards
        contactsCard.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        hospitalsCard.setOnClickListener {
            Toast.makeText(this, "Hospitals - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        historyCard.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Bottom Navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_contacts -> {
                    startActivity(Intent(this, ContactsActivity::class.java))
                    true
                }
                R.id.nav_map -> {
                    Toast.makeText(this, "Map", Toast.LENGTH_SHORT).show()
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
}