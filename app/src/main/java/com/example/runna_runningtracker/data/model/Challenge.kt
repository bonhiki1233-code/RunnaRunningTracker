package com.example.runna_runningtracker.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val goalDistanceMeters: Long = 0L,
    val startAt: Timestamp? = null,
    val endAt: Timestamp? = null,
    val createdBy: String = "",
    val createdByName: String = "",
    val visibility: String = VISIBILITY_PUBLIC,
    val status: String = STATUS_ACTIVE,
    val participantCount: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        FIELD_TITLE to title,
        FIELD_DESCRIPTION to description,
        FIELD_GOAL_DISTANCE_METERS to goalDistanceMeters,
        FIELD_START_AT to startAt,
        FIELD_END_AT to endAt,
        FIELD_CREATED_BY to createdBy,
        FIELD_CREATED_BY_NAME to createdByName,
        FIELD_VISIBILITY to visibility,
        FIELD_STATUS to status,
        FIELD_PARTICIPANT_COUNT to participantCount,
        FIELD_CREATED_AT to createdAt,
        FIELD_UPDATED_AT to updatedAt
    )

    companion object {
        const val COLLECTION = "challenges"
        const val FIELD_TITLE = "title"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_GOAL_DISTANCE_METERS = "goalDistanceMeters"
        const val FIELD_START_AT = "startAt"
        const val FIELD_END_AT = "endAt"
        const val FIELD_CREATED_BY = "createdBy"
        const val FIELD_CREATED_BY_NAME = "createdByName"
        const val FIELD_VISIBILITY = "visibility"
        const val FIELD_STATUS = "status"
        const val FIELD_PARTICIPANT_COUNT = "participantCount"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"

        const val VISIBILITY_PUBLIC = "public"
        const val STATUS_DRAFT = "draft"
        const val STATUS_ACTIVE = "active"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_ARCHIVED = "archived"

        fun fromDocument(document: DocumentSnapshot): Challenge {
            return Challenge(
                id = document.id,
                title = document.getString(FIELD_TITLE).orEmpty(),
                description = document.getString(FIELD_DESCRIPTION).orEmpty(),
                goalDistanceMeters = document.getLong(FIELD_GOAL_DISTANCE_METERS) ?: 0L,
                startAt = document.getTimestamp(FIELD_START_AT),
                endAt = document.getTimestamp(FIELD_END_AT),
                createdBy = document.getString(FIELD_CREATED_BY).orEmpty(),
                createdByName = document.getString(FIELD_CREATED_BY_NAME).orEmpty(),
                visibility = document.getString(FIELD_VISIBILITY).orEmpty()
                    .ifBlank { VISIBILITY_PUBLIC },
                status = document.getString(FIELD_STATUS).orEmpty()
                    .ifBlank { STATUS_ACTIVE },
                participantCount = (document.getLong(FIELD_PARTICIPANT_COUNT) ?: 0L).toInt(),
                createdAt = document.getTimestamp(FIELD_CREATED_AT),
                updatedAt = document.getTimestamp(FIELD_UPDATED_AT)
            )
        }
    }
}
