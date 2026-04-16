package com.example.runna_runningtracker.data.model

import com.google.firebase.Timestamp

data class CompletedRun(
    val runId: String,
    val userId: String,
    val distanceMeters: Long,
    val durationSeconds: Long,
    val startedAt: Timestamp? = null,
    val endedAt: Timestamp,
    val status: String = STATUS_COMPLETED,
    val routePreview: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        FIELD_USER_ID to userId,
        FIELD_DISTANCE_METERS to distanceMeters,
        FIELD_DURATION_SECONDS to durationSeconds,
        FIELD_STARTED_AT to startedAt,
        FIELD_ENDED_AT to endedAt,
        FIELD_STATUS to status,
        FIELD_ROUTE_PREVIEW to routePreview
    )

    companion object {
        const val COLLECTION = "runs"
        const val FIELD_USER_ID = "userId"
        const val FIELD_DISTANCE_METERS = "distanceMeters"
        const val FIELD_DURATION_SECONDS = "durationSeconds"
        const val FIELD_STARTED_AT = "startedAt"
        const val FIELD_ENDED_AT = "endedAt"
        const val FIELD_STATUS = "status"
        const val FIELD_ROUTE_PREVIEW = "routePreview"
        const val STATUS_COMPLETED = "completed"
    }
}
