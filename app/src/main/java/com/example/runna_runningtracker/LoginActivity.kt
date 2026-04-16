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

    // 3 man nay nam chung trong 1 layout nen code se an hien theo flow
    private lateinit var loginScreen: View
    private lateinit var personalInfoScreen: View
    private lateinit var forgotPasswordOverlay: View

    // Nhom input login va complete profile de doc du lieu tu form
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

    // uid nay duoc giu tam sau register de luu dung profile cho user moi
    private var pendingRegisterUid: String? = null
    // currentUser la bo nho tam giua login complete info va luu profile
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

        // Neu vua register xong thi mo thang form complete info
        if (intent.getBooleanExtra("SHOW_PERSONAL_INFO", false)) {
            val email = intent.getStringExtra("USER_EMAIL") ?: ""
            val uid = intent.getStringExtra("USER_UID")
            currentUser = currentUser.copy(name = "", email = email)
            pendingRegisterUid = uid
            showPersonalInfoScreen()
        } else {
            // Neu da co session cu thi bo qua man login
            checkExistingSession()
        }
    }

    private fun bindViews() {
        // Gan bien Kotlin voi view trong XML de dung lai cho ca flow
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
        // Layout nay tung gom nhieu man cu nen can an bot de tranh chong len nhau
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
        // Click Register thi sang man tao tai khoan rieng
        showRegisterText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Quen mat khau se doi tu login sang overlay reset
        findViewById<TextView>(R.id.forgotPasswordText).setOnClickListener {
            loginScreen.visibility = View.GONE
            forgotPasswordOverlay.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.sendResetButton).setOnClickListener { handleForgotPassword() }
        findViewById<Button>(R.id.loginButton).setOnClickListener { handleLogin() }
        findViewById<Button>(R.id.completeProfileButton).setOnClickListener { completePersonalInfo() }
    }

    private fun styleInlineRegisterText() {
        // To mau rieng chu Register de user nhin de bam de hon
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
        // Firebase con nho uid thi load profile roi vao home luon
        val currentUserId = authRepository.getCurrentUserId() ?: return
        loadProfileAndGoHome(currentUserId)
    }

    private fun handleLogin() {
        // Login chi cho di tiep khi email va password da du
        val email = loginEmailInput.text.toString().trim()
        val password = loginPasswordInput.text.toString().trim()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, getString(R.string.please_enter_email_password), Toast.LENGTH_SHORT).show()
            return
        }

        // Dang nhap Auth truoc roi moi dong bo profile Firestore
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
        // Reset password can email de Firebase gui mail
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
        // Form nay giu email vua register nhung de trong cac field con lai
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
        // Uu tien uid vua register xong neu dang o flow tao tai khoan moi
        val finalUid = pendingRegisterUid ?: authRepository.getCurrentUserId()
        if (finalUid == null) {
            Toast.makeText(this, getString(R.string.please_register_first), Toast.LENGTH_SHORT).show()
            return
        }

        // Gom du lieu form thanh 1 object user de luu Firestore va local
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

        // Profile duoc tao trong Firestore sau buoc register Auth
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
        // Qua home xong thi dong man login de back khong quay lai day
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(HomeActivity.EXTRA_USER_ID, uid)
        startActivity(intent)
        finish()
    }

    private fun loadProfileAndGoHome(uid: String?) {
        // uid null thi van vao home an toan de tranh crash flow
        if (uid.isNullOrBlank()) {
            goToHome(null)
            return
        }

        // Login xong phai load profile Firestore de Home va Profile co du data
        userRepository.loadUserProfile(
            uid = uid,
            onSuccess = { user ->
                currentUser = user.copy(uid = uid)
                UserPrefsManager.saveUser(this, currentUser)
                goToHome(uid)
            },
            onFailure = {
                // Neu Firestore chua co profile thi van vao app bang data toi thieu
                UserPrefsManager.saveUser(this, currentUser.copy(uid = uid))
                goToHome(uid)
            }
        )
    }

    private fun setupProfilePickers() {
        // Gender va birthday duoc chon bang picker de nhap cho on dinh hon
        personalGenderInput.setOnClickListener { showGenderPicker(personalGenderInput) }
        personalAgeInput.setOnClickListener { showBirthDatePicker(personalAgeInput) }
    }

    private fun showGenderPicker(target: EditText) {
        // Picker nay tra ve 1 gia tri trong danh sach gioi tinh
        val genderOptions = resources.getStringArray(R.array.gender_options)
        AlertDialog.Builder(this)
            .setItems(genderOptions) { _, which -> target.setText(genderOptions[which]) }
            .show()
    }

    private fun showBirthDatePicker(target: EditText) {
        // Neu da co ngay sinh thi mo lai ngay cu de user sua nhanh hon
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
        // App dang thong nhat ngay sinh theo dang dd MM yyyy
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
    }

    private fun parseBirthDate(value: String): Calendar? {
        // Doi text ngay sinh thanh Calendar de tinh tuoi va mo lai picker
        if (value.isBlank()) return null
        return runCatching {
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(value) ?: return null
            Calendar.getInstance().apply { time = date }
        }.getOrNull()
    }

    private fun calculateAgeFromBirthDate(birthDate: String): String {
        // Age la du lieu suy ra tu birthday nen app tinh lai moi lan can
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
