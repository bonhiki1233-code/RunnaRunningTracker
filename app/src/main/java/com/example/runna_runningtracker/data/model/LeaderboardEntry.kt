package com.example.runna_runningtracker.data.model

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val progressDistanceMeters: Long,
    val progressPercent: Double
)
