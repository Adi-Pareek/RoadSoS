package com.roadsafety.roadsos

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({

            val currentUser =
                FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {

                startActivity(
                    Intent(
                        this,
                        DashboardActivity::class.java
                    )
                )

            } else {

                startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    )
                )
            }

            finish()

        }, 2500)
    }
}