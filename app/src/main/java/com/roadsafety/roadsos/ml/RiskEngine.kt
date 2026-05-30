package com.roadsafety.roadsos.ml

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.Calendar
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class RiskResult(
    val score: Int,
    val reasons: List<String>
)

class RiskEngine(private val context: Context) {

    private var centroids: List<Pair<Double, Double>> = emptyList()
    private var clusterRiskRanks: Map<Int, Double> = emptyMap()
    private var weights: List<Double> = emptyList()
    private var bias: Double = 0.0
    private var smoothedScore: Double = -1.0

    init {
        loadConfig()
    }

    private fun loadConfig() {
        try {
            val inputStream = context.assets.open("ml_config.json")
            val jsonString = InputStreamReader(inputStream).readText()
            val jsonObject = JSONObject(jsonString)

            val centroidsArray = jsonObject.getJSONArray("centroids")
            val tempCentroids = mutableListOf<Pair<Double, Double>>()
            for (i in 0 until centroidsArray.length()) {
                val pair = centroidsArray.getJSONArray(i)
                tempCentroids.add(Pair(pair.getDouble(0), pair.getDouble(1)))
            }
            centroids = tempCentroids

            val ranksObj = jsonObject.getJSONObject("cluster_risk_ranks")
            val tempRanks = mutableMapOf<Int, Double>()
            val keys = ranksObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                tempRanks[key.toInt()] = ranksObj.getDouble(key)
            }
            clusterRiskRanks = tempRanks

            val weightsArray = jsonObject.getJSONArray("weights")
            val tempWeights = mutableListOf<Double>()
            for (i in 0 until weightsArray.length()) {
                tempWeights.add(weightsArray.getDouble(i))
            }
            weights = tempWeights

            bias = jsonObject.getDouble("bias")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return r * c * 1000 // Return in meters
    }

    fun predictRisk(lat: Double, lng: Double, gForce: Float, gyroMag: Float, speed: Float): RiskResult {
        if (centroids.isEmpty()) return RiskResult(0, listOf("Initializing..."))

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val pythonDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val isWeekend = if (pythonDayOfWeek >= 5) 1.0 else 0.0

        val hourSin = sin(2 * Math.PI * hour / 24.0)
        val hourCos = cos(2 * Math.PI * hour / 24.0)

        // 1. Geographic Risk
        var nearestDistance = Double.MAX_VALUE
        var nearestClusterIndex = 0
        for ((index, centroid) in centroids.withIndex()) {
            val dist = calculateHaversineDistance(lat, lng, centroid.first, centroid.second)
            if (dist < nearestDistance) {
                nearestDistance = dist
                nearestClusterIndex = index
            }
        }

        var baseClusterRisk = clusterRiskRanks[nearestClusterIndex] ?: 50.0
        
        // Distance penalty & decay
        var distancePenalty = 0.0
        var distReason = ""
        if (nearestDistance < 500) {
            distancePenalty = 20.0
            distReason = "High accident-density zone (<500m)"
        } else if (nearestDistance < 2000) {
            distancePenalty = 10.0
            distReason = "Approaching moderate risk zone (~${nearestDistance.toInt()}m)"
        } else if (nearestDistance > 5000) {
            // Distance Decay: If user is far from the hotspot, the risk should drop significantly
            val decayFactor = (5000.0 / nearestDistance).coerceAtLeast(0.1)
            baseClusterRisk *= decayFactor
            distReason = "Safe distance from known hotspots (>5km)"
        }

        var geographicRisk = baseClusterRisk + distancePenalty
        geographicRisk = geographicRisk.coerceIn(0.0, 100.0)

        // 2. ML Prediction Risk
        val featureValues = listOf(
            lat, lng, hourSin, hourCos, pythonDayOfWeek.toDouble(), isWeekend, nearestClusterIndex.toDouble()
        )
        var mlRisk = bias
        if (weights.size == featureValues.size) {
            for (i in featureValues.indices) {
                mlRisk += featureValues[i] * weights[i]
            }
        }
        val mlRiskNormalized = (mlRisk.coerceIn(0.0, 1.0) * 100.0)

        // 3. Temporal Risk
        var temporalRisk = 0.0
        var tempReason = ""
        if (hour in 22..23 || hour in 0..5) {
            temporalRisk = 80.0
            tempReason = "Night-time risk elevated"
        } else if (isWeekend == 1.0) {
            temporalRisk = 60.0
            tempReason = "Weekend traffic conditions"
        } else {
            temporalRisk = 30.0
        }

        // 4. Sensor & Velocity Risk
        var sensorRisk = 0.0
        var sensorReason = ""
        val speedKmh = speed * 3.6f // Convert m/s to km/h
        
        if (gForce > 2.0f) {
            sensorRisk = 90.0
            sensorReason = "Sudden braking or acceleration detected"
        } else if (gyroMag > 4.0f) {
            sensorRisk = 80.0
            sensorReason = "Unstable device motion detected"
        } else if (speedKmh > 80f) {
            sensorRisk = 85.0
            sensorReason = "High speed detected (${speedKmh.toInt()} km/h)"
        } else if (speedKmh > 40f) {
            sensorRisk = 40.0
        } else {
            sensorRisk = 10.0
        }

        // --- Contextual Overrides & Stationary Check ---
        // A user stationary at home (speed < 3 km/h and stable sensors) has minimal accident risk
        val isStationary = speedKmh < 3.0f && gForce < 1.2f && gyroMag < 1.0f

        // Re-calibrated Hybrid Scoring System
        val geoContribution = 0.45 * geographicRisk
        val mlContribution = 0.25 * mlRiskNormalized
        val tempContribution = 0.10 * temporalRisk
        val sensorContribution = 0.20 * sensorRisk
        
        var rawFinalScore = geoContribution + mlContribution + tempContribution + sensorContribution
        
        val reasons = mutableListOf<String>()

        if (isStationary) {
            // Suppress the risk heavily if the user is stationary
            rawFinalScore *= 0.15 // 85% risk reduction
            reasons.add("Stationary or parked. Risk is minimal.")
            if (geographicRisk > 50) {
                reasons.add("Parked in a high-risk zone (+${(rawFinalScore * 0.45).toInt()})")
            }
        } else {
            // Build standard reasons
            if (geographicRisk > 70) {
                reasons.add("Severe Hotspot: This area has recorded multiple serious accidents (+${geoContribution.toInt()})")
            } else if (distReason.isNotEmpty() && !distReason.contains("Safe")) {
                reasons.add("$distReason (+${geoContribution.toInt()})")
            } else if (distReason.contains("Safe")) {
                reasons.add(distReason)
            }
            
            if (tempReason.isNotEmpty() && (rawFinalScore > 40 || temporalRisk > 70)) {
                reasons.add("$tempReason (+${tempContribution.toInt()})")
            }

            if (sensorReason.isNotEmpty()) {
                reasons.add("$sensorReason (+${sensorContribution.toInt()})")
            }
            
            if (mlRiskNormalized > 60 && reasons.size < 3) {
                reasons.add("AI Model predicts elevated local risk (+${mlContribution.toInt()})")
            }

            if (reasons.isEmpty()) {
                reasons.add("No immediate localized threats detected")
            }
        }

        // --- Exponential Moving Average (EMA) Smoothing ---
        if (smoothedScore < 0) {
            smoothedScore = rawFinalScore
        } else {
            // Smooth out sudden spikes/drops (30% new, 70% old)
            smoothedScore = (0.3 * rawFinalScore) + (0.7 * smoothedScore)
        }

        val finalScoreInt = smoothedScore.toInt().coerceIn(0, 100)
        return RiskResult(finalScoreInt, reasons)
    }
}
