package com.roadsafety.roadsos.detection

data class CrashEvent(
    val timestamp: Long,
    val accelerationForce: Float,
    val latitude: Double,
    val longitude: Double,
    val speed: Float
)