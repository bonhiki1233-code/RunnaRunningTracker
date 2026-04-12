package com.example.runna_runningtracker.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val age: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val height: String = "",
    val weight: String = ""
) {
    // Chuyen model thanh map de ghi len Firestore
    fun toMap(): Map<String, String> {
        return mapOf(
            KEY_NAME to name,
            KEY_EMAIL to email,
            KEY_BIRTH_DATE to birthDate,
            KEY_GENDER to gender,
            KEY_HEIGHT to height,
            KEY_WEIGHT to weight
        )
    }

    companion object {
        // Constant dung chung de tranh viet tay ten collection/field o nhieu noi.
        const val COLLECTION_USERS = "users"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val KEY_AGE = "age"
        const val KEY_BIRTH_DATE = "birthDate"
        const val KEY_GENDER = "gender"
        const val KEY_HEIGHT = "height"
        const val KEY_WEIGHT = "weight"

        fun fromMap(uid: String, data: Map<String, Any>?): User {
            // Doc tu Firestore: neu field nao thieu thi cho ve chuoi rong.
            val birthDate = data?.get(KEY_BIRTH_DATE) as? String ?: ""
            return User(
                uid = uid,
                name = data?.get(KEY_NAME) as? String ?: "",
                email = data?.get(KEY_EMAIL) as? String ?: "",
                age = calculateAgeFromBirthDate(birthDate).ifBlank { data?.get(KEY_AGE) as? String ?: "" },
                birthDate = birthDate,
                gender = data?.get(KEY_GENDER) as? String ?: "",
                height = data?.get(KEY_HEIGHT) as? String ?: "",
                weight = data?.get(KEY_WEIGHT) as? String ?: ""
            )
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
    }
}
