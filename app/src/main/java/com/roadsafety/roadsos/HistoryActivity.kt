package com.roadsafety.roadsos

import android.os.Bundle

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout

    private lateinit var adapter: HistoryAdapter

    // Upar ke Stats wale TextBoxes
    private lateinit var totalText: TextView
    private lateinit var severeText: TextView
    private lateinit var resolvedText: TextView


    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    // Dummy data — Satish will replace with Firebase data
    private val historyList = mutableListOf(
        AccidentHistory(
            id = "1",
            date = "22 May 2026",
            time = "10:45 AM",
            severity = "Severe",
            location = "Mumbai Highway, Maharashtra",
            alertsSent = 3,
            status = "Resolved"
        ),
        AccidentHistory(
            id = "2",
            date = "18 May 2026",
            time = "06:20 PM",
            severity = "Minor",
            location = "Pune Road, Maharashtra",
            alertsSent = 2,
            status = "Resolved"
        ),
        AccidentHistory(
            id = "3",
            date = "10 May 2026",
            time = "02:15 PM",
            severity = "Medium",
            location = "Thane, Maharashtra",
            alertsSent = 2,
            status = "Active"
        )
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        supportActionBar?.hide()

        recyclerView = findViewById(R.id.historyRecyclerView)
        emptyState = findViewById(R.id.emptyState)


        // YAHAN APNI XML WALI IDs DALNA (Step 1 wali) 👇
        totalText = findViewById(R.id.totalCount)
        severeText = findViewById(R.id.severeCount)
        resolvedText = findViewById(R.id.resolvedCount)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(historyList)
        recyclerView.adapter = adapter

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }

        // Clear All Button Update

        // Back button
        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Clear all button

        findViewById<TextView>(R.id.clearAll).setOnClickListener {
            showClearDialog()
        }


        fetchHistoryFromFirebase()
    }

    private fun fetchHistoryFromFirebase() {
        val userId = auth.currentUser?.uid
        if (userId == null) return

        db.collection("Users").document(userId).collection("AccidentHistory")
            .get()
            .addOnSuccessListener { documents ->
                historyList.clear()

                for (document in documents) {
                    val timestamp = document.getLong("timestamp") ?: 0L
                    val severity = document.getString("severity") ?: "Unknown"
                    val lat = document.getDouble("latitude") ?: 0.0
                    val lng = document.getDouble("longitude") ?: 0.0

                    val dateObj = Date(timestamp)
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(dateObj)
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(dateObj)

                    historyList.add(AccidentHistory(
                        id = document.id,
                        date = dateFormat,
                        time = timeFormat,
                        severity = severity,
                        location = "Lat: $lat, Lng: $lng",
                        alertsSent = 3,
                        status = "Active"
                    ))
                }

                adapter.notifyDataSetChanged()
                updateUIState() // Data aane ke baad dabbe update honge
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUIState() {
        // Dabbe me number database se gin kar aayenge
        totalText.text = historyList.size.toString()
        severeText.text = historyList.count { it.severity.equals("severe", true) }.toString()
        resolvedText.text = historyList.count { it.status.equals("resolved", true) }.toString()


        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = HistoryAdapter(historyList)

        updateEmptyState()
    }

    private fun showClearDialog() {
        AlertDialog.Builder(this, R.style.DarkDialog)
            .setTitle("Clear History")
            .setMessage("Are you sure you want to clear all accident history?")
            .setPositiveButton("CLEAR") { _, _ ->
                historyList.clear()
                recyclerView.adapter?.notifyDataSetChanged()
                updateEmptyState()
                Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun updateEmptyState() {

        if (historyList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }


}