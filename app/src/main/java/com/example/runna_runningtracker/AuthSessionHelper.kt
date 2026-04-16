package com.example.runna_runningtracker

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object AuthSessionHelper {
    fun getCurrentUserId(context: Context): String? {
        // Uu tien uid tu Firebase va fallback sang local neu can
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("uid", null)
    }

    fun getCurrentDisplayName(context: Context): String {
        // Ten nay dung cho challenge create join va leaderboard
        return context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getString("name", null)
            ?.takeIf { it.isNotBlank() }
            ?: FirebaseAuth.getInstance().currentUser?.displayName
            ?: "Runner"
    }
}
