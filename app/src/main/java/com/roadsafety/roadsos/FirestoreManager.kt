package com.roadsafety.roadsos

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    // 1. Naye user ka data save karna (Registration ke time)
    fun saveUserProfile(user: UserModel, onResult: (Boolean, String?) -> Unit) {
        if (user.userId.isEmpty()) {
            onResult(false, "User ID khali hai")
            return
        }

        db.collection("Users").document(user.userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("FirestoreManager", "User profile saved!")
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreManager", "Error saving profile", e)
                onResult(false, e.message)
            }
    }

    // 2. User ka data nikalna (Profile page ya app open hone par)
    fun getUserProfile(userId: String, onResult: (UserModel?, String?) -> Unit) {
        db.collection("Users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(UserModel::class.java)
                    onResult(user, null)
                } else {
                    onResult(null, "User nahi mila")
                }
            }
            .addOnFailureListener { e ->
                onResult(null, e.message)
            }
    }

    // 3. Accident ka data log karna
    fun logAccidentEvent(userId: String, severity: String, lat: Double, lng: Double, onResult: (Boolean, String?) -> Unit) {
        val accidentData = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "severity" to severity,
            "latitude" to lat,
            "longitude" to lng
        )

        db.collection("Users").document(userId)
            .collection("AccidentHistory").add(accidentData)
            .addOnSuccessListener {
                Log.d("FirestoreManager", "Accident logged!")
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreManager", "Error logging accident", e)
                onResult(false, e.message)
            }
    }

    // --- NAYE CONTACTS WALE FUNCTIONS YAHAN HAIN ---

    // 4. Firebase par Contact Save karna
    fun addEmergencyContact(userId: String, contact: Contact, onResult: (Boolean, String?) -> Unit) {
        db.collection("Users").document(userId)
            .collection("EmergencyContacts").document(contact.id)
            .set(contact)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    // 5. Firebase se Contacts Wapas Lana (Fetch)
    fun getEmergencyContacts(userId: String, onResult: (List<Contact>?, String?) -> Unit) {
        db.collection("Users").document(userId)
            .collection("EmergencyContacts")
            .get()
            .addOnSuccessListener { documents ->
                val contactsList = mutableListOf<Contact>()
                for (doc in documents) {
                    val contact = doc.toObject(Contact::class.java)
                    contactsList.add(contact)
                }
                onResult(contactsList, null)
            }
            .addOnFailureListener { e -> onResult(null, e.message) }
    }

    // 6. Firebase se Contact Delete karna
    fun deleteEmergencyContact(userId: String, contactId: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("Users").document(userId)
            .collection("EmergencyContacts").document(contactId)
            .delete()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }
}