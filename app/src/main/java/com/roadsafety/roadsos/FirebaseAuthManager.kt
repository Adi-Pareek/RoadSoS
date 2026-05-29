package com.roadsafety.roadsos
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class FirebaseAuthManager {

    // Firebase Auth ka instance le rahe hain
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // 1. Naya User Register Karne Ka Function
    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    Log.d("FirebaseAuth", "User registered successfully")
                    onResult(true, null)
                } else {
                    // Registration fail ho gaya (e.g. password chhota hai, email galat hai)
                    Log.e("FirebaseAuth", "Registration failed", task.exception)
                    onResult(false, task.exception?.message)
                }
            }
    }

    // 2. Purane User Ko Login Karne Ka Function
    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful
                    Log.d("FirebaseAuth", "User logged in successfully")
                    onResult(true, null)
                } else {
                    // Login fail ho gaya
                    Log.e("FirebaseAuth", "Login failed", task.exception)
                    onResult(false, task.exception?.message)
                }
            }
    }

    // 3. Check karna ki user pehle se login hai ya nahi
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // 4. Current user ki ID (UID) nikalna (Ye database mein kaam aayega)
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // 5. Logout Function
    fun logoutUser() {
        auth.signOut()
    }
}
