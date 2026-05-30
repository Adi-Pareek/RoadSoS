package com.roadsafety.roadsos

import android.content.Intent

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText


class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        // Connect UI elements to code
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.registerLink)

        // Login button click
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Basic validation
            if (email.isEmpty()) {
                emailInput.error = "Enter email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordInput.error = "Enter password"
                return@setOnClickListener
            }

            // --- YAHAN MEMBER 4 KA ASLI BACKEND CODE AAYEGA ---

            // 1. FirebaseAuthManager ko bulao
            val authManager = FirebaseAuthManager()

            // 2. Email aur password check karne ke liye Firebase ko bhejo
            authManager.loginUser(email, password) { isSuccess, errorMessage ->
                if (isSuccess) {
                    // Agar password SAHI hai, toh Dashboard par bhejo
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    goToDashboard()
                } else {
                    // Agar password GALAT hai, toh error dikhao aur yahin rok lo
                    Toast.makeText(this, "Login Failed: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }

            // For now go straight to Dashboard
            // Firebase login will be added by Satish
            goToDashboard()

        }

        // Register link click
        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}