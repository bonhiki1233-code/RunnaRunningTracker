package com.example.runna_runningtracker

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.runna_runningtracker.data.model.User
import com.example.runna_runningtracker.data.repository.AuthRepository
import com.example.runna_runningtracker.data.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var loginScreen: View
    private lateinit var personalInfoScreen: View
    private lateinit var forgotPasswordOverlay: View

    private lateinit var loginEmailInput: EditText
    private lateinit var loginPasswordInput: EditText
    private lateinit var registerEmailInput: EditText
    private lateinit var registerPasswordInput: EditText
    private lateinit var registerConfirmPasswordInput: EditText
    private lateinit var personalNameInput: EditText
    private lateinit var personalEmailInput: EditText
    private lateinit var personalAgeInput: EditText
    private lateinit var personalGenderInput: EditText
    private lateinit var personalHeightInput: EditText
    private lateinit var personalWeightInput: EditText
    private lateinit var forgotPasswordEmailInput: EditText
    private lateinit var showRegisterText: TextView

    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository

    private var pendingRegisterUid: String? = null
    private var currentUser = User(
        name = "",
        email = "",
        age = "",
        birthDate = "",
        gender = "",
        height = "",
        weight = ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authRepository = AuthRepository()
        userRepository = UserRepository()

        bindViews()
        hideLegacyViews()
        setupProfilePickers()
        styleInlineRegisterText()
        bindActions()

        if (intent.getBooleanExtra("SHOW_PERSONAL_INFO", false)) {
            val email = intent.getStringExtra("USER_EMAIL") ?: ""
            val uid = intent.getStringExtra("USER_UID")
            currentUser = currentUser.copy(name = "", email = email)
            pendingRegisterUid = uid
            showPersonalInfoScreen()
        } else {
            checkExistingSession()
        }
    }

    private fun bindViews() {
        loginScreen = findViewById(R.id.loginScreen)
        personalInfoScreen = findViewById(R.id.personalInfoScreen)
        forgotPasswordOverlay = findViewById(R.id.forgotPasswordOverlay)

        loginEmailInput = findViewById(R.id.loginEmailInput)
        loginPasswordInput = findViewById(R.id.loginPasswordInput)
        personalNameInput = findViewById(R.id.personalNameInput)
        personalEmailInput = findViewById(R.id.personalEmailInput)
        personalAgeInput = findViewById(R.id.personalAgeInput)
        personalGenderInput = findViewById(R.id.personalGenderInput)
        personalHeightInput = findViewById(R.id.personalHeightInput)
        personalWeightInput = findViewById(R.id.personalWeightInput)
        forgotPasswordEmailInput = findViewById(R.id.forgotPasswordEmailInput)
        showRegisterText = findViewById(R.id.showRegisterText)
    }

    private fun hideLegacyViews() {
        val legacyIds = listOf(
            R.id.appScreen,
            R.id.trackingScreen,
            R.id.pauseOverlay,
            R.id.summaryScreen,
            R.id.editProfileOverlay,
            R.id.appInfoOverlay
        )
        legacyIds.forEach { id ->
            findViewById<View>(id)?.visibility = View.GONE
        }
    }

    private fun bindActions() {
        showRegisterText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<TextView>(R.id.forgotPasswordText).setOnClickListener {
            loginScreen.visibility = View.GONE
            forgotPasswordOverlay.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.sendResetButton).setOnClickListener { handleForgotPassword() }
        findViewById<Button>(R.id.loginButton).setOnClickListener { handleLogin() }
        findViewById<Button>(R.id.completeProfileButton).setOnClickListener { completePersonalInfo() }
    }

    private fun styleInlineRegisterText() {
        val fullText = getString(R.string.register_prompt)
        val registerWord = getString(R.string.register_word)
        val startIndex = fullText.indexOf(registerWord)
        val spannable = SpannableString(fullText)
        if (startIndex >= 0) {
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.runna_primary)),
                startIndex,
                startIndex + registerWord.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        showRegisterText.text = spannable
    }

    private fun checkExistingSession() {
        val currentUserId = authRepository.getCurrentUserId() ?: return
        loadProfileAndGoHome(currentUserId)
    }

    private fun handleLogin() {
        val email = loginEmailInput.text.toString().trim()
        val password = loginPasswordInput.text.toString().trim()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, getString(R.string.please_enter_email_password), Toast.LENGTH_SHORT).show()
            return
        }

        authRepository.login(
            email = email,
            password = password,
            onSuccess = { uid ->
                Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                loadProfileAndGoHome(uid)
            },
            onFailure = { error ->
                Toast.makeText(this, getString(R.string.login_failed, error), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun handleForgotPassword() {
        val resetEmail = forgotPasswordEmailInput.text.toString().trim()
        if (resetEmail.isBlank()) {
            Toast.makeText(this, getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show()
            return
        }

        authRepository.sendPasswordReset(
            email = resetEmail,
            onSuccess = {
                forgotPasswordOverlay.visibility = View.GONE
                loginScreen.visibility = View.VISIBLE
                Toast.makeText(this, getString(R.string.reset_link_sent), Toast.LENGTH_SHORT).show()
            },
            onFailure = { error ->
                Toast.makeText(this, getString(R.string.reset_failed, error), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showPersonalInfoScreen() {
        loginScreen.visibility = View.GONE
        personalInfoScreen.visibility = View.VISIBLE
        personalNameInput.setText("")
        personalEmailInput.setText(currentUser.email)
        personalAgeInput.setText("")
        personalGenderInput.setText("")
        personalHeightInput.setText("")
        personalWeightInput.setText("")
    }

    private fun completePersonalInfo() {
        val finalUid = pendingRegisterUid ?: authRepository.getCurrentUserId()
        if (finalUid == null) {
            Toast.makeText(this, getString(R.string.please_register_first), Toast.LENGTH_SHORT).show()
            return
        }

        currentUser = currentUser.copy(
            uid = finalUid,
            name = personalNameInput.text.toString().trim().ifBlank { currentUser.name },
            email = personalEmailInput.text.toString().trim().ifBlank { currentUser.email },
            birthDate = personalAgeInput.text.toString().trim().ifBlank { currentUser.birthDate },
            age = calculateAgeFromBirthDate(personalAgeInput.text.toString().trim().ifBlank { currentUser.birthDate }),
            gender = personalGenderInput.text.toString().trim().ifBlank { currentUser.gender },
            height = personalHeightInput.text.toString().trim().ifBlank { currentUser.height },
            weight = personalWeightInput.text.toString().trim().ifBlank { currentUser.weight }
        )

        userRepository.createUserProfile(
            user = currentUser,
            onSuccess = {
                UserPrefsManager.saveUser(this, currentUser)
                Toast.makeText(this, getString(R.string.register_successful), Toast.LENGTH_SHORT).show()
                goToHome(finalUid)
            },
            onFailure = { error ->
                Toast.makeText(this, getString(R.string.profile_save_failed, error), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun goToHome(uid: String?) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(HomeActivity.EXTRA_USER_ID, uid)
        startActivity(intent)
        finish()
    }

    private fun loadProfileAndGoHome(uid: String?) {
        if (uid.isNullOrBlank()) {
            goToHome(null)
            return
        }

        userRepository.loadUserProfile(
            uid = uid,
            onSuccess = { user ->
                currentUser = user.copy(uid = uid)
                UserPrefsManager.saveUser(this, currentUser)
                goToHome(uid)
            },
            onFailure = {
                // Nếu Firestore chưa có profile thì vẫn cho vào app bằng dữ liệu tối thiểu.
                UserPrefsManager.saveUser(this, currentUser.copy(uid = uid))
                goToHome(uid)
            }
        )
    }

    private fun setupProfilePickers() {
        personalGenderInput.setOnClickListener { showGenderPicker(personalGenderInput) }
        personalAgeInput.setOnClickListener { showBirthDatePicker(personalAgeInput) }
    }

    private fun showGenderPicker(target: EditText) {
        val genderOptions = resources.getStringArray(R.array.gender_options)
        AlertDialog.Builder(this)
            .setItems(genderOptions) { _, which -> target.setText(genderOptions[which]) }
            .show()
    }

    private fun showBirthDatePicker(target: EditText) {
        val calendar = parseBirthDate(target.text.toString()) ?: Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                target.setText(formatBirthDate(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatBirthDate(calendar: Calendar): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
    }

    private fun parseBirthDate(value: String): Calendar? {
        if (value.isBlank()) return null
        return runCatching {
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(value) ?: return null
            Calendar.getInstance().apply { time = date }
        }.getOrNull()
    }

    private fun calculateAgeFromBirthDate(birthDate: String): String {
        val birthCalendar = parseBirthDate(birthDate) ?: return ""
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        if (
            today.get(Calendar.MONTH) < birthCalendar.get(Calendar.MONTH) ||
            (today.get(Calendar.MONTH) == birthCalendar.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(Calendar.DAY_OF_MONTH))
        ) {
            age -= 1
        }
        return age.coerceAtLeast(0).toString()
    }
}
