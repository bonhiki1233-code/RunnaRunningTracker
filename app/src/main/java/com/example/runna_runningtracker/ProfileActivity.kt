package com.example.runna_runningtracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.runna_runningtracker.data.repository.AuthRepository
import com.example.runna_runningtracker.data.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{7,10}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        NavigationHelper.setupBottomNav(this, NavigationHelper.ActiveTab.PROFILE)
        authRepository = AuthRepository()
        userRepository = UserRepository()

        val txtName = findViewById<TextView>(R.id.profileName)
        val txtEmail = findViewById<TextView>(R.id.profileEmail)
        val txtAge = findViewById<TextView>(R.id.profileAge)
        val txtHeight = findViewById<TextView>(R.id.profileHeight)
        val txtWeight = findViewById<TextView>(R.id.profileWeight)
        val btnAppSettings = findViewById<View>(R.id.appSettingsButton)
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

        btnAppSettings.setOnClickListener {
            showAppSettingsMenu(txtEmail)
        }

        btnLogout.setOnClickListener {
            authRepository.signOut()
            UserPrefsManager.clear(this)

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showAppSettingsMenu(emailView: TextView) {
        val items = arrayOf(
            getString(R.string.reset_password),
            getString(R.string.update_email),
            getString(R.string.app_information)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.app_information_settings)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showResetPasswordDialog()
                    1 -> showUpdateEmailDialog(emailView)
                    2 -> showAppInformationDialog()
                }
            }
            .show()
    }

    private fun showResetPasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null)
        val currentPasswordInput = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val newPasswordInput = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.etConfirmNewPassword)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.reset_password)
            .setView(dialogView)
            .setNegativeButton(R.string.close, null)
            .setPositiveButton(R.string.save_changes, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val currentPassword = currentPasswordInput.text.toString().trim()
                val newPassword = newPasswordInput.text.toString().trim()
                val confirmPassword = confirmPasswordInput.text.toString().trim()

                when {
                    currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                        Toast.makeText(this, getString(R.string.fill_all_register_fields), Toast.LENGTH_SHORT).show()
                    }
                    !passwordRegex.matches(newPassword) -> {
                        newPasswordInput.error = getString(R.string.password_rule_error)
                    }
                    newPassword != confirmPassword -> {
                        confirmPasswordInput.error = getString(R.string.passwords_do_not_match)
                    }
                    else -> {
                        authRepository.updatePassword(
                            currentPassword = currentPassword,
                            newPassword = newPassword,
                            onSuccess = {
                                Toast.makeText(this, getString(R.string.password_updated_success), Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            },
                            onFailure = { error ->
                                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        dialog.show()
    }

    private fun showUpdateEmailDialog(emailView: TextView) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_email, null)
        val currentEmailView = dialogView.findViewById<TextView>(R.id.tvCurrentEmailValue)
        val newEmailInput = dialogView.findViewById<EditText>(R.id.etNewEmail)
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentEmail = sharedPref.getString("email", "") ?: ""
        currentEmailView.text = getString(R.string.current_email_format, currentEmail)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.update_email)
            .setView(dialogView)
            .setNegativeButton(R.string.close, null)
            .setPositiveButton(R.string.save_changes, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newEmail = newEmailInput.text.toString().trim()
                val uid = authRepository.getCurrentUserId()

                when {
                    newEmail.isBlank() -> {
                        newEmailInput.error = getString(R.string.please_enter_email)
                    }
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() -> {
                        newEmailInput.error = getString(R.string.invalid_email)
                    }
                    uid.isNullOrBlank() -> {
                        Toast.makeText(this, getString(R.string.user_id_not_found), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        authRepository.updateEmail(
                            newEmail = newEmail,
                            onSuccess = {
                                userRepository.updateUserEmail(
                                    uid = uid,
                                    email = newEmail,
                                    onSuccess = {
                                        sharedPref.edit().putString("email", newEmail).apply()
                                        emailView.text = newEmail
                                        Toast.makeText(this, getString(R.string.email_update_verification_sent), Toast.LENGTH_LONG).show()
                                        dialog.dismiss()
                                    },
                                    onFailure = { error ->
                                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onFailure = { error ->
                                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        dialog.show()
    }

    private fun showAppInformationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.app_information)
            .setMessage(getString(R.string.app_info_text))
            .setPositiveButton(R.string.close, null)
            .show()
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
