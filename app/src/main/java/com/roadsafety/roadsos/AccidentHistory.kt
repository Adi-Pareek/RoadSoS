package com.roadsafety.roadsos

data class AccidentHistory(
    val id: String = "",
    val date: String = "",
    val time: String = "",
    val severity: String = "",
    val location: String = "",
    val alertsSent: Int = 0,
    val status: String = ""
)