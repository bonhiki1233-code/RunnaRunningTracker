package com.example.runna_runningtracker.data.model

import com.google.firebase.Timestamp

data class CreateChallengeInput(
    val title: String,
    val description: String,
    val goalDistanceMeters: Long,
    val startAt: Timestamp,
    val endAt: Timestamp,
    val createdBy: String,
    val createdByName: String
)
