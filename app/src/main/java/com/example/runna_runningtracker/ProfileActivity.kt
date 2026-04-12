package com.example.runna_runningtracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.runna_runningtracker.data.repository.AuthRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        NavigationHelper.setupBottomNav(this, NavigationHelper.ActiveTab.PROFILE)
        authRepository = AuthRepository()

        val txtName = findViewById<TextView>(R.id.profileName)
        val txtEmail = findViewById<TextView>(R.id.profileEmail)
        val txtAge = findViewById<TextView>(R.id.profileAge)
        val txtHeight = findViewById<TextView>(R.id.profileHeight)
        val txtWeight = findViewById<TextView>(R.id.profileWeight)
        val btnLogout = findViewById<View>(R.id.logoutButton)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedName = sharedPref.getString("name", "Người dùng")
        val savedEmail = sharedPref.getString("email", "Chưa có email")
        val savedAge = sharedPref.getString("age", "0")
        val savedBirthDate = sharedPref.getString("birth_date", "")
        val savedHeight = sharedPref.getString("height", "0")
        val savedWeight = sharedPref.getString("weight", "0")
        val currentAge = calculateAgeFromBirthDate(savedBirthDate).ifBlank { savedAge ?: "0" }

        txtName.text = savedName
        txtEmail.text = savedEmail
        txtAge.text = "$currentAge years"
        txtHeight.text = "$savedHeight cm"
        txtWeight.text = "$savedWeight kg"

        btnLogout.setOnClickListener {
            authRepository.signOut()
            UserPrefsManager.clear(this)

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun calculateAgeFromBirthDate(birthDate: String?): String {
        if (birthDate.isNullOrBlank()) return ""

        return runCatching {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDateParsed = formatter.parse(birthDate) ?: return ""
            val birthCalendar = Calendar.getInstance().apply { time = birthDateParsed }
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
