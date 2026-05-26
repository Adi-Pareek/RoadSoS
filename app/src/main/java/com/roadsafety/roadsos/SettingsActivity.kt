package com.roadsafety.roadsos

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide()

        // Back button
        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Edit profile
        findViewById<TextView>(R.id.editProfile).setOnClickListener {
            Toast.makeText(this, "Edit profile — Satish connecting Firebase", Toast.LENGTH_SHORT).show()
        }

        // Auto detect toggle
        findViewById<SwitchMaterial>(R.id.switchAutoDetect).setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Auto detection enabled" else "Auto detection disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // SMS toggle
        findViewById<SwitchMaterial>(R.id.switchSMS).setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "SMS alerts enabled" else "SMS alerts disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Sound toggle
        findViewById<SwitchMaterial>(R.id.switchSound).setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Sound alerts enabled" else "Sound alerts disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Logout
        findViewById<MaterialButton>(R.id.logoutButton).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this, R.style.DarkDialog)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("LOGOUT") { _, _ ->
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }
}