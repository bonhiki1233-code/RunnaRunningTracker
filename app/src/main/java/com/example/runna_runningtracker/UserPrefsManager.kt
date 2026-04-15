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
    private const val KEY_LAST_DISTANCE ="last_distance"
    private const val KEY_LAST_DURATION ="last_duration"
    private const val KEY_LAST_CALORIES ="last_calories"

    fun saveUser(context: Context, user: User) {
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
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun calculateAgeFromBirthDate(birthDate: String): String {
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

    fun saveRunSession(context: Context, distance: Float, durationSeconds: Int, calories: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_LAST_DISTANCE, distance)
            .putInt(KEY_LAST_DURATION, durationSeconds)
            .putInt(KEY_LAST_CALORIES, calories)
            .apply()
    }

    fun getUserWeightOrNull(context: Context): Double? {
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val weightStr = prefs.getString("weight", "") ?: ""
        return weightStr.toDoubleOrNull()
    }
}