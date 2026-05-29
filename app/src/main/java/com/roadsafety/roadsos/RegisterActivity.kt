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

            // --- YAHAN TUMHARA ASLI BACKEND CODE HAI ---

            // Registration process start karne ke liye Toast dikhao
            Toast.makeText(this, "Registering... Please wait", Toast.LENGTH_SHORT).show()

            val authManager = FirebaseAuthManager()

            // 1. Firebase mein account banao
            authManager.registerUser(email, password) { isAuthSuccess, authErrorMessage ->
                if (isAuthSuccess) {

                    // 2. Account ban gaya, ab uski nayi ID nikaalo
                    val userId = authManager.getCurrentUserId() ?: ""

                    // 3. User ke form wale details (Name, Phone) UserModel mein dalo
                    val newUser = UserModel(
                        userId = userId,
                        name = name,
                        phone = phone,
                        bloodGroup = "", // Ise user baad mein profile settings me update karega
                        emergencyContacts = emptyList()
                    )

                    // 4. Firestore Database mein saara data save karo
                    val firestoreManager = FirestoreManager()
                    firestoreManager.saveUserProfile(newUser) { isDbSuccess, dbErrorMessage ->
                        if (isDbSuccess) {
                            // Sab kuch 100% success ho gaya!
                            // Fix: popup ko gayab hone se bachane ke liye applicationContext use kiya hai
                            Toast.makeText(applicationContext, "Registration Successful! Please login.", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                            finish()
                        } else {
                            // Database error
                            Toast.makeText(this@RegisterActivity, "Database Error: $dbErrorMessage", Toast.LENGTH_LONG).show()
                        }
                    }

                } else {
                    // Registration error (jaise email pehle se use mein hai)
                    Toast.makeText(this@RegisterActivity, "Registration Failed: $authErrorMessage", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Login link
        loginLink.setOnClickListener {
            finish()
        }
    }
}