package com.roadsafety.roadsos

import android.content.Intent
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

        // Back button
        findViewById<TextView>(R.id.backButton).setOnClickListener {
            if (!isEmergencyActive) {
                countDownTimer?.cancel()
                finish()
            }
        }

        // Start pulse animation on countdown circle
        val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse)
        countdownCard.startAnimation(pulseAnim)

        // Start countdown
        startCountdown()

        // I'm Safe button
        safButton.setOnClickListener {
            countDownTimer?.cancel()
            Toast.makeText(this, "Glad you're safe! Monitoring continues.", Toast.LENGTH_LONG).show()
            finish()
        }

        // Need Help button
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

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                countdownNumber.text = secondsLeft.toString()

                // Turn more red as countdown progresses
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
                // Auto activate emergency if no response
                activateEmergency()
            }
        }.start()
    }

    private fun activateEmergency() {
        isEmergencyActive = true
        emergencyOverlay.visibility = View.VISIBLE

        // Simulate sending alerts with delays
        // Raj and Alok will connect real SMS and location here
        android.os.Handler(mainLooper).postDelayed({
            smsStatus.text = "✅ SMS sent to contacts"
        }, 1500)

        android.os.Handler(mainLooper).postDelayed({
            locationStatus.text = "✅ Live location shared"
        }, 2500)

        android.os.Handler(mainLooper).postDelayed({
            alertStatus.text = "✅ Emergency services notified"
        }, 3500)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}