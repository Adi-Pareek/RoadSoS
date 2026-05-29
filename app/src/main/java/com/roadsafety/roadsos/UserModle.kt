package com.roadsafety.roadsos
// Ye ek Data Class hai jo user ki information hold karegi
data class UserModel(
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val bloodGroup: String = "",
    val emergencyContacts: List<String> = emptyList()
)