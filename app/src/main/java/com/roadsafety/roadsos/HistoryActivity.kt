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

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout

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

        // Back button
        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Clear all button
        findViewById<TextView>(R.id.clearAll).setOnClickListener {
            showClearDialog()
        }

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