package com.example.runna_runningtracker.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class ChallengeMember(
    val challengeId: String = "",
    val userId: String = "",
    val joinedAt: Timestamp? = null,
    val progressDistanceMeters: Long = 0L,
    val isCompleted: Boolean = false,
    val completedAt: Timestamp? = null,
    val lastRunAppliedAt: Timestamp? = null,
    val displayNameSnapshot: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        FIELD_CHALLENGE_ID to challengeId,
        FIELD_USER_ID to userId,
        FIELD_JOINED_AT to joinedAt,
        FIELD_PROGRESS_DISTANCE_METERS to progressDistanceMeters,
        FIELD_IS_COMPLETED to isCompleted,
        FIELD_COMPLETED_AT to completedAt,
        FIELD_LAST_RUN_APPLIED_AT to lastRunAppliedAt,
        FIELD_DISPLAY_NAME_SNAPSHOT to displayNameSnapshot
    )

    companion object {
        const val COLLECTION = "challenge_members"
        const val FIELD_CHALLENGE_ID = "challengeId"
        const val FIELD_USER_ID = "userId"
        const val FIELD_JOINED_AT = "joinedAt"
        const val FIELD_PROGRESS_DISTANCE_METERS = "progressDistanceMeters"
        const val FIELD_IS_COMPLETED = "isCompleted"
        const val FIELD_COMPLETED_AT = "completedAt"
        const val FIELD_LAST_RUN_APPLIED_AT = "lastRunAppliedAt"
        const val FIELD_DISPLAY_NAME_SNAPSHOT = "displayNameSnapshot"

        fun documentId(challengeId: String, userId: String): String = "${challengeId}_${userId}"

        fun fromDocument(document: DocumentSnapshot): ChallengeMember {
            return ChallengeMember(
                challengeId = document.getString(FIELD_CHALLENGE_ID).orEmpty(),
                userId = document.getString(FIELD_USER_ID).orEmpty(),
                joinedAt = document.getTimestamp(FIELD_JOINED_AT),
                progressDistanceMeters = document.getLong(FIELD_PROGRESS_DISTANCE_METERS) ?: 0L,
                isCompleted = document.getBoolean(FIELD_IS_COMPLETED) ?: false,
                completedAt = document.getTimestamp(FIELD_COMPLETED_AT),
                lastRunAppliedAt = document.getTimestamp(FIELD_LAST_RUN_APPLIED_AT),
                displayNameSnapshot = document.getString(FIELD_DISPLAY_NAME_SNAPSHOT).orEmpty()
            )
        }
    }
}
