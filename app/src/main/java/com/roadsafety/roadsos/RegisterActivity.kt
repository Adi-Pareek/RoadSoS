package com.roadsafety.roadsos

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var loginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        // Connect UI
        nameInput = findViewById(R.id.nameInput)
        phoneInput = findViewById(R.id.phoneInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink)

        // Back button
        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Register button
        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // Validation
            if (name.isEmpty()) {
                nameInput.error = "Enter your name"
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                phoneInput.error = "Enter phone number"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                emailInput.error = "Enter email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordInput.error = "Enter password"
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                confirmPasswordInput.error = "Passwords don't match"
                return@setOnClickListener
            }
            if (password.length < 6) {
                passwordInput.error = "Password must be 6+ characters"
                return@setOnClickListener
            }

            // Satish will connect Firebase auth here
            Toast.makeText(this, "Account created! Please login.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Login link
        loginLink.setOnClickListener {
            finish()
        }
    }
}