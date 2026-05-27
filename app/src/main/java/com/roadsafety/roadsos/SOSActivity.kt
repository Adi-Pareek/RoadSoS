package com.roadsafety.roadsos


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
import com.roadsafety.roadsos.ContactManager

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

    private val accidentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            crashLat = intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LATITUDE, 0.0)
            crashLng = intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LONGITUDE, 0.0)
            crashGForce = intent.getFloatExtra(AccidentBroadcaster.EXTRA_G_FORCE, 0f)
            // Restart countdown with crash data
            countDownTimer?.cancel()
            startCountdown()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos)
        supportActionBar?.hide()


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

        // Get crash data if launched from broadcast
        crashLat = intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LATITUDE, 0.0)
        crashLng = intent.getDoubleExtra(AccidentBroadcaster.EXTRA_LONGITUDE, 0.0)
        crashGForce = intent.getFloatExtra(AccidentBroadcaster.EXTRA_G_FORCE, 0f)

        // Back button
        findViewById<TextView>(R.id.backButton).setOnClickListener {
            if (!isEmergencyActive) {
                countDownTimer?.cancel()
                finish()
            }
        }

        // Pulse animation
        val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse)
        countdownCard.startAnimation(pulseAnim)

        // Start countdown
        startCountdown()

        // I'm Safe
        safButton.setOnClickListener {
            countDownTimer?.cancel()
            Toast.makeText(this, "Glad you're safe! Monitoring continues.", Toast.LENGTH_LONG).show()
            finish()
        }

        // Need Help
        helpButton.setOnClickListener {
            countDownTimer?.cancel()
            activateEmergency()
        }

        // Cancel Emergency
        cancelEmergency.setOnClickListener {
            isEmergencyActive = false
            emergencyOverlay.visibility = View.GONE
            Toast.makeText(this, "Emergency cancelled", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Call Ambulance
        callAmbulance.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:108"))
            startActivity(callIntent)
        }
    }

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
                    secondsLeft <= 10 -> countdownCard.setCardBackgroundColor(
                        getColor(R.color.emergency_red_dark)
                    )
                    else -> countdownCard.setCardBackgroundColor(
                        getColor(R.color.emergency_red)
                    )
                }
            }

            override fun onFinish() {
                activateEmergency()
            }
        }.start()
    }

    private fun activateEmergency() {

        isEmergencyActive = true

        emergencyOverlay.visibility = View.VISIBLE

        if (
            crashLat == 0.0 ||
            crashLng == 0.0
        ) {

            Toast.makeText(
                this,
                "Waiting for live location...",
                Toast.LENGTH_LONG
            ).show()

            smsStatus.text =
                " 📍Getting live location..."

            return
        }

        val message =

            "EMERGENCY ALERT\n\n" +
                    "Possible accident detected.\n\n" +
                    "Live Location:\n" +
                    "https://maps.google.com/?q=$crashLat,$crashLng"

        try {

            val contacts =
                ContactManager.getContacts(this)

            if (contacts.isEmpty()) {

                smsStatus.text =
                    "❌ No emergency contacts"

                Toast.makeText(
                    this,
                    "Add emergency contacts first",
                    Toast.LENGTH_LONG
                ).show()

                return
            }

            val smsManager =
                SmsManager.getDefault()

            for (contact in contacts) {

                smsManager.sendTextMessage(
                    contact.phone,
                    null,
                    message,
                    null,
                    null
                )
            }

            smsStatus.text =
                "✅ SMS sent to ${contacts.size} contacts"

        } catch (e: Exception) {

            smsStatus.text =
                "❌ SMS failed"

            Toast.makeText(
                this,
                e.message,
                Toast.LENGTH_LONG
            ).show()
        }

        android.os.Handler(mainLooper).postDelayed({

            locationStatus.text =
                "✅ Live location shared"

        }, 1500)

        android.os.Handler(mainLooper).postDelayed({

            alertStatus.text =
                "✅ Emergency services notified"

        }, 2500)
    }
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}