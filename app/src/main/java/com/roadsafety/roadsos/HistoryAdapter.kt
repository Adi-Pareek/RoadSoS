package com.roadsafety.roadsos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val historyList: List<AccidentHistory>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val severityBadge: TextView = view.findViewById(R.id.severityBadge)
        val date: TextView = view.findViewById(R.id.historyDate)
        val time: TextView = view.findViewById(R.id.historyTime)
        val location: TextView = view.findViewById(R.id.historyLocation)
        val alertsSent: TextView = view.findViewById(R.id.alertsSent)
        val status: TextView = view.findViewById(R.id.historyStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]

        holder.severityBadge.text = history.severity.uppercase()
        holder.date.text = history.date
        holder.time.text = history.time
        holder.location.text = "📍 ${history.location}"
        holder.alertsSent.text = "📱 ${history.alertsSent} alerts sent"
        holder.status.text = "● ${history.status}"

        // Color badge based on severity
        when (history.severity.lowercase()) {
            "minor" -> holder.severityBadge.setBackgroundColor(
                Color.parseColor("#2E7D32")
            )
            "medium" -> holder.severityBadge.setBackgroundColor(
                Color.parseColor("#F57F17")
            )
            "severe" -> holder.severityBadge.setBackgroundColor(
                Color.parseColor("#B71C1C")
            )
        }

        // Color status text
        when (history.status.lowercase()) {
            "resolved" -> holder.status.setTextColor(Color.parseColor("#4CAF50"))
            "active" -> holder.status.setTextColor(Color.parseColor("#E53935"))
            else -> holder.status.setTextColor(Color.parseColor("#FFC107"))
        }
    }

    override fun getItemCount() = historyList.size
}