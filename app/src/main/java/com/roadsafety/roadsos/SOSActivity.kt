package com.roadsafety.roadsos

<<<<<<< HEAD
=======

>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.roadsafety.roadsos.detection.AccidentBroadcaster
import android.telephony.SmsManager
import android.util.Log
<<<<<<< HEAD
import com.google.firebase.auth.FirebaseAuth
=======
import com.roadsafety.roadsos.ContactManager
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3

class SOSActivity : AppCompatActivity() {

    private lateinit var countdownNumber: TextView
    private lateinit var countdownCard: CardView
    private lateinit var safButton: MaterialButton
    private lateinit var helpButton: MaterialButton
    private lateinit var emergencyOverlay: LinearLayout
    private lateinit var cancelEmergency: MaterialButton
    private lateinit var callAmbulance: MaterialButton
    private lateinit var smsStatus: TextView
    private lateinit var locationStatus: TextView
    private lateinit var alertStatus: TextView

    private var countDownTimer: CountDownTimer? = null
    private var isEmergencyActive = false
    private var crashLat = 0.0
    private var crashLng = 0.0
    private var crashGForce = 0f

<<<<<<< HEAD
    // Firebase Managers aur Contacts List Storage
    private val auth = FirebaseAuth.getInstance()
    private val firestoreManager = FirestoreManager()
    private var emergencyContactsList = listOf<Contact>()

=======
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
    private val accidentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            crashLat = intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LATITUDE, 0.0)
            crashLng = intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LONGITUDE, 0.0)
            crashGForce = intent.getFloatExtra(AccidentBroadcaster.EXTRA_G_FORCE, 0f)
<<<<<<< HEAD
=======
            // Restart countdown with crash data
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
            countDownTimer?.cancel()
            startCountdown()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos)
        supportActionBar?.hide()

<<<<<<< HEAD
=======

>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
        // Connect UI
        countdownNumber = findViewById(R.id.countdownNumber)
        countdownCard = findViewById(R.id.countdownCard)
        safButton = findViewById(R.id.safButton)
        helpButton = findViewById(R.id.helpButton)
        emergencyOverlay = findViewById(R.id.emergencyOverlay)
        cancelEmergency = findViewById(R.id.cancelEmergency)
        callAmbulance = findViewById(R.id.callAmbulance)
        smsStatus = findViewById(R.id.smsStatus)
        locationStatus = findViewById(R.id.locationStatus)
        alertStatus = findViewById(R.id.alertStatus)

<<<<<<< HEAD
        // Screen khulte hi Firebase se contacts load karna shuru karo (Countdown khatam hone se pehle ready milenge)
        fetchContactsFromFirebaseCloud()

=======
        // Get crash data if launched from broadcast
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
        crashLat = intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LATITUDE, 0.0)
        crashLng = intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LONGITUDE, 0.0)
        crashGForce = intent.getFloatExtra(AccidentBroadcaster.EXTRA_G_FORCE, 0f)

<<<<<<< HEAD
=======
        // Back button
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
        findViewById<TextView>(R.id.backButton).setOnClickListener {
            if (!isEmergencyActive) {
                countDownTimer?.cancel()
                finish()
            }
        }

<<<<<<< HEAD
        val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse)
        countdownCard.startAnimation(pulseAnim)

        startCountdown()

=======
        // Pulse animation
        val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse)
        countdownCard.startAnimation(pulseAnim)

        // Start countdown
        startCountdown()

        // I'm Safe
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
        safButton.setOnClickListener {
            countDownTimer?.cancel()
            Toast.makeText(this, "Glad you're safe! Monitoring continues.", Toast.LENGTH_LONG).show()
            finish()
        }

<<<<<<< HEAD
=======
        // Need Help
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
        helpButton.setOnClickListener {
            countDownTimer?.cancel()
            activateEmergency()
        }

<<<<<<< HEAD
=======
        // Cancel Emergency
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
        cancelEmergency.setOnClickListener {
            isEmergencyActive = false
            emergencyOverlay.visibility = View.GONE
            Toast.makeText(this, "Emergency cancelled", Toast.LENGTH_SHORT).show()
            finish()
        }

<<<<<<< HEAD
=======
        // Call Ambulance
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
        callAmbulance.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:108"))
            startActivity(callIntent)
        }
    }

<<<<<<< HEAD
    // Firebase se Contacts load karne ka naya function
    private fun fetchContactsFromFirebaseCloud() {
        val userId = auth.currentUser?.uid ?: return
        firestoreManager.getEmergencyContacts(userId) { contacts, error ->
            if (contacts != null) {
                emergencyContactsList = contacts
                Log.d("SOSActivity", "Cloud se ${contacts.size} contacts successfully load ho gaye.")
            } else {
                Log.e("SOSActivity", "Contacts load karne me dikkat: $error")
            }
        }
    }

=======
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(AccidentBroadcaster.ACTION_ACCIDENT_DETECTED)
        registerReceiver(accidentReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(accidentReceiver)
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                countdownNumber.text = secondsLeft.toString()
                when {
<<<<<<< HEAD
                    secondsLeft <= 10 -> countdownCard.setCardBackgroundColor(getColor(R.color.emergency_red_dark))
                    else -> countdownCard.setCardBackgroundColor(getColor(R.color.emergency_red))
=======
                    secondsLeft <= 10 -> countdownCard.setCardBackgroundColor(
                        getColor(R.color.emergency_red_dark)
                    )
                    else -> countdownCard.setCardBackgroundColor(
                        getColor(R.color.emergency_red)
                    )
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
                }
            }

            override fun onFinish() {
                activateEmergency()
            }
        }.start()
    }

<<<<<<< HEAD
=======


>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
    private fun activateEmergency() {
        isEmergencyActive = true
        emergencyOverlay.visibility = View.VISIBLE
        smsStatus.text = "📍 Getting location..."

<<<<<<< HEAD
=======
        // Try LocationService first
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
        val locationServiceInstance = com.roadsafety.roadsos.service.LocationService.instance
        if (crashLat == 0.0 || crashLng == 0.0) {
            crashLat = locationServiceInstance?.currentLat ?: 0.0
            crashLng = locationServiceInstance?.currentLng ?: 0.0
        }

<<<<<<< HEAD
        if (crashLat == 0.0 || crashLng == 0.0) {
            val fusedClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)
=======
        // If still 0, get from FusedLocation
        if (crashLat == 0.0 || crashLng == 0.0) {
            val fusedClient = com.google.android.gms.location.LocationServices
                .getFusedLocationProviderClient(this)
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
            try {
                fusedClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        crashLat = location.latitude
                        crashLng = location.longitude
                    }
                    sendSos()
                }.addOnFailureListener {
                    sendSos()
                }
            } catch (e: SecurityException) {
                sendSos()
            }
        } else {
            sendSos()
        }
    }

    private fun sendSos() {
<<<<<<< HEAD
        // PRO FEATURE: Asli Google Maps coordinate URL link banaya hai yahan 👇
        val message = "EMERGENCY ALERT\n\nPossible accident detected.\n\nLive Location:\nhttps://maps.google.com/?q=$crashLat,$crashLng"

        try {
            // Local Manager ki jagah humne Firebase wala Cloud storage list use kiya hai 👇
            if (emergencyContactsList.isEmpty()) {
                smsStatus.text = "❌ No emergency contacts found on Cloud"
                Toast.makeText(this, "Please add emergency contacts in settings first", Toast.LENGTH_LONG).show()
=======
        val message = "EMERGENCY ALERT\n\nPossible accident detected.\n\nLive Location:\nhttps://maps.google.com/?q=$crashLat,$crashLng"

        try {
            val contacts = ContactManager.getContacts(this)
            if (contacts.isEmpty()) {
                smsStatus.text = "❌ No emergency contacts"
                Toast.makeText(this, "Add emergency contacts first", Toast.LENGTH_LONG).show()
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
                return
            }

            val smsManager = SmsManager.getDefault()
<<<<<<< HEAD
            for (contact in emergencyContactsList) {
                smsManager.sendTextMessage(contact.phone, null, message, null, null)
            }
            smsStatus.text = "✅ SMS sent to ${emergencyContactsList.size} contacts"
            Log.d("SOS", "SMS sent successfully to cloud contacts!")

            // AUTOLOG HISTORY: Accident ko automatic database history collection me bhi save karo 👇
            val userId = auth.currentUser?.uid
            if (userId != null) {
                firestoreManager.logAccidentEvent(userId, "Severe", crashLat, crashLng) { success, error ->
                    if (success) Log.d("SOS", "Accident successfully logged to user history!")
                    else Log.e("SOS", "Failed to log accident to history: $error")
                }
            }
=======
            for (contact in contacts) {
                smsManager.sendTextMessage(contact.phone, null, message, null, null)
            }
            smsStatus.text = "✅ SMS sent to ${contacts.size} contacts"
            Log.d("SOS", "SMS sent! lat: $crashLat lng: $crashLng")
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3

        } catch (e: Exception) {
            smsStatus.text = "❌ SMS failed: ${e.message}"
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

        android.os.Handler(mainLooper).postDelayed({
            locationStatus.text = "✅ Live location shared"
        }, 1500)

        android.os.Handler(mainLooper).postDelayed({
            alertStatus.text = "✅ Emergency services notified"
        }, 2500)
    }
<<<<<<< HEAD

=======
>>>>>>> ca394ebcc234837c355ae690eb7e61058ba164c3
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}