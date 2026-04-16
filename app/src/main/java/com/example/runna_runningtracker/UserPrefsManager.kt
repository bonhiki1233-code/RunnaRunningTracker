package com.example.runna_runningtracker

import android.content.Context
import com.example.runna_runningtracker.data.model.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object UserPrefsManager {

    private const val PREFS_NAME = "UserPrefs"
    private const val KEY_UID = "uid"
    private const val KEY_NAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_AGE = "age"
    private const val KEY_BIRTH_DATE = "birth_date"
    private const val KEY_GENDER = "gender"
    private const val KEY_HEIGHT = "height"
    private const val KEY_WEIGHT = "weight"

    fun saveUser(context: Context, user: User) {
        // Luu cache local de Home va Profile doc nhanh hon Firestore
        val ageToSave = calculateAgeFromBirthDate(user.birthDate).ifBlank { user.age }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_UID, user.uid)
            .putString(KEY_NAME, user.name)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_AGE, ageToSave)
            .putString(KEY_BIRTH_DATE, user.birthDate)
            .putString(KEY_GENDER, user.gender)
            .putString(KEY_HEIGHT, user.height)
            .putString(KEY_WEIGHT, user.weight)
            .apply()
    }

    fun clear(context: Context) {
        // Logout se xoa sach cache local trong may
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun getUserWeightOrNull(context: Context): Double? {
        // Can nang duoc doi sang so de tinh calories luc finish run
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WEIGHT, null)
            ?.trim()
            ?.toDoubleOrNull()
    }

    private fun calculateAgeFromBirthDate(birthDate: String): String {
        // Tuoi luon tinh lai tu ngay sinh de tranh bi cu theo thoi gian
        if (birthDate.isBlank()) return ""

        return runCatching {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate = formatter.parse(birthDate) ?: return ""
            val birthCalendar = Calendar.getInstance().apply { time = parsedDate }
            val today = Calendar.getInstance()

            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            if (
                today.get(Calendar.MONTH) < birthCalendar.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == birthCalendar.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(Calendar.DAY_OF_MONTH))
            ) {
                age -= 1
            }

            age.coerceAtLeast(0).toString()
        }.getOrDefault("")
    }
}
