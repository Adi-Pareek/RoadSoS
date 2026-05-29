package com.roadsafety.roadsos

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    // UI Elements Declarations
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profilePhone: TextView

    // Firebase Managers
    private val auth = FirebaseAuth.getInstance()
    private val firestoreManager = FirestoreManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide()

        // 1. UI Views ko Connect Karo (Inki IDs apne XML ke hisaab se match kar lena)
        profileName = findViewById(R.id.profileName)   // Jahan 'User Name' show hota hai
        profileEmail = findViewById(R.id.profileEmail) // Jahan email show hota hai
        profilePhone = findViewById(R.id.profilePhone) // Jahan phone number show hota hai

        // 2. Screen khulte hi Firebase se live data load karo
        loadUserProfileData()

        // Back button
        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Edit profile
        findViewById<TextView>(R.id.editProfile).setOnClickListener {
            fetchAndShowEditProfileDialog()
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

    // --- ASLI BACKEND FETCH LOGIC ---
    private fun loadUserProfileData() {
        val userId = auth.currentUser?.uid
        val email = auth.currentUser?.email

        if (userId == null) return

        // Email direct Firebase Authentication se mil jata hai
        profileEmail.text = email ?: "No Email Found"

        // Name aur Phone ke liye Firestore se data fetch karenge
        firestoreManager.getUserProfile(userId) { userModel, error ->
            if (userModel != null) {
                profileName.text = userModel.name
                profilePhone.text = userModel.phone
            } else {
                Toast.makeText(this, "Profile data load karne me error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchAndShowEditProfileDialog() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Fetching details...", Toast.LENGTH_SHORT).show()

        firestoreManager.getUserProfile(userId) { userModel, error ->
            if (userModel != null) {
                showEditDialog(userModel)
            } else {
                Toast.makeText(this, "Error fetching profile: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(userModel: UserModel) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 40)
        }

        val nameInput = EditText(this).apply {
            hint = "Name"
            setText(userModel.name)
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
        }
        val phoneInput = EditText(this).apply {
            hint = "Phone Number"
            setText(userModel.phone)
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
        }
        val bloodGroupInput = EditText(this).apply {
            hint = "Blood Group (e.g. O+)"
            setText(userModel.bloodGroup)
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        layout.addView(nameInput)
        layout.addView(phoneInput)
        layout.addView(bloodGroupInput)

        AlertDialog.Builder(this, R.style.DarkDialog)
            .setTitle("Edit Profile")
            .setView(layout)
            .setPositiveButton("SAVE") { _, _ ->
                val updatedName = nameInput.text.toString().trim()
                val updatedPhone = phoneInput.text.toString().trim()
                val updatedBlood = bloodGroupInput.text.toString().trim()

                if (updatedName.isEmpty() || updatedPhone.isEmpty()) {
                    Toast.makeText(this, "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updatedUser = UserModel(
                    userId = userModel.userId,
                    name = updatedName,
                    phone = updatedPhone,
                    bloodGroup = updatedBlood,
                    emergencyContacts = userModel.emergencyContacts
                )

                firestoreManager.saveUserProfile(updatedUser) { success, saveError ->
                    if (success) {
                        Toast.makeText(this, "Profile successfully updated!", Toast.LENGTH_LONG).show()

                        // MAGIC: Save hote hi screen ke text ko bhi instantly update kar do bina refresh kiye
                        profileName.text = updatedName
                        profilePhone.text = updatedPhone
                    } else {
                        Toast.makeText(this, "Update failed: $saveError", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this, R.style.DarkDialog)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("LOGOUT") { _, _ ->
                auth.signOut()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }
}