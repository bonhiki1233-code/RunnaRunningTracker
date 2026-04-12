package com.example.runna_runningtracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.runna_runningtracker.data.model.User
import com.example.runna_runningtracker.data.repository.AuthRepository
import com.example.runna_runningtracker.data.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity() {


    private lateinit var loginScreen: android.view.View
    private lateinit var registerScreen: android.view.View
    private lateinit var personalInfoScreen: android.view.View
    private lateinit var appScreen: android.view.View
    private lateinit var trackingScreen: android.view.View
    private lateinit var pauseOverlay: android.view.View
    private lateinit var summaryScreen: android.view.View
    private lateinit var forgotPasswordOverlay: android.view.View
    private lateinit var editProfileOverlay: android.view.View
    private lateinit var appInfoOverlay: android.view.View

    private lateinit var homeScreen: android.view.View
    private lateinit var startScreen: android.view.View
    private lateinit var historyScreen: android.view.View
    private lateinit var profileScreen: android.view.View

    private lateinit var navHome: TextView
    private lateinit var navStart: TextView
    private lateinit var navHistory: TextView
    private lateinit var navProfile: TextView

    private lateinit var typeEasy: TextView
    private lateinit var typeLong: TextView
    private lateinit var typeInterval: TextView
    private lateinit var typeWalking: TextView
    private lateinit var beginRunButton: Button
    private lateinit var runTypeChip: TextView
    private lateinit var trackingTimerText: TextView
    private lateinit var trackingDistanceText: TextView
    private lateinit var trackingPaceText: TextView
    private lateinit var trackingCaloriesText: TextView
    private lateinit var pauseTimeText: TextView
    private lateinit var pauseDistanceText: TextView
    private lateinit var pausePaceText: TextView
    private lateinit var summaryDurationText: TextView
    private lateinit var summaryDistanceText: TextView
    private lateinit var summaryPaceText: TextView
    private lateinit var summaryCaloriesText: TextView
    private lateinit var homeWelcomeText: TextView
    private lateinit var profileInfoText: TextView
    private lateinit var recentActivityText: TextView
    private lateinit var showRegisterText: TextView

    private lateinit var registerNameInput: EditText
    private lateinit var registerEmailInput: EditText
    private lateinit var registerPasswordInput: EditText
    private lateinit var registerConfirmPasswordInput: EditText
    private lateinit var personalNameInput: EditText
    private lateinit var personalEmailInput: EditText
    private lateinit var personalAgeInput: EditText
    private lateinit var personalGenderInput: EditText
    private lateinit var personalHeightInput: EditText
    private lateinit var personalWeightInput: EditText
    private lateinit var editNameInput: EditText
    private lateinit var editEmailInput: EditText
    private lateinit var editAgeInput: EditText
    private lateinit var editGenderInput: EditText
    private lateinit var editHeightInput: EditText
    private lateinit var editWeightInput: EditText

    // AuthRepository chi lo viec login,reg , reset password, logout.
    private lateinit var authRepository: AuthRepository

    // UserRepository lo du lieu profile ben fire
    private lateinit var userRepository: UserRepository
    private val timerHandler = Handler(Looper.getMainLooper())
    private var elapsedSeconds = 2
    private var selectedRunType = ""


    private var pendingRegisterUid: String? = null


    private var currentUser = User(
        name = "Nghi",
        email = "nghi@gmail.com",
        age = "19",
        birthDate = "",
        gender = "Female",
        height = "165",
        weight = "62"
    )

    private val timerRunnable = object : Runnable {
        override fun run() {

            elapsedSeconds += 1
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            val distance = 0.08 + (elapsedSeconds * 0.012)
            val calories = 6 + elapsedSeconds
            val pace = if (distance > 0) String.format("%.2f", minutes.toDouble() / distance) else "0.00"
            trackingTimerText.text = String.format("%02d:%02d", minutes, seconds)
            trackingDistanceText.text = String.format("%.2f", distance)
            trackingPaceText.text = pace
            trackingCaloriesText.text = calories.toString()
            pauseTimeText.text = String.format("%02d:%02d", minutes, seconds)
            pauseDistanceText.text = String.format("%.1f km", distance)
            pausePaceText.text = pace
            summaryDurationText.text = String.format("%02d:%02d", minutes, seconds)
            summaryDistanceText.text = String.format("%.1f", distance)
            summaryPaceText.text = pace
            summaryCaloriesText.text = calories.toString()
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Khoi tao repository ngay tu dau de MainActivity chi con dieu phoi flow.
        authRepository = AuthRepository()
        userRepository = UserRepository()
        bindViews()
        setupProfilePickers()
        styleInlineRegisterText()
        bindActions()
        val uid = authRepository.getCurrentUserId() ?: ""
        if (uid.isNotEmpty()) {
            userRepository.loadUserProfile(uid, onSuccess = { user ->
                runOnUiThread {
                    homeWelcomeText.text = "Welcome back, ${user.name}"
                }
            }, onFailure = { /* xử lý lỗi nếu cần */ })
        }
        selectSection(Section.HOME)
        selectRunType(getString(R.string.run_type_easy))
        checkExistingSession()
    }

    override fun onDestroy() {
        timerHandler.removeCallbacks(timerRunnable)
        super.onDestroy()
    }

    private fun bindViews() {
        // Gom toan bo findViewById vao 1 cho de de sua giao dien prototype.
        loginScreen = findViewById(R.id.loginScreen)
        registerScreen = findViewById(R.id.registerScreen)
        personalInfoScreen = findViewById(R.id.personalInfoScreen)
        appScreen = findViewById(R.id.appScreen)
        trackingScreen = findViewById(R.id.trackingScreen)
        pauseOverlay = findViewById(R.id.pauseOverlay)
        summaryScreen = findViewById(R.id.summaryScreen)
        forgotPasswordOverlay = findViewById(R.id.forgotPasswordOverlay)
        editProfileOverlay = findViewById(R.id.editProfileOverlay)
        appInfoOverlay = findViewById(R.id.appInfoOverlay)

        homeScreen = findViewById(R.id.homeScreen)
        startScreen = findViewById(R.id.startScreen)
        historyScreen = findViewById(R.id.historyScreen)
        profileScreen = findViewById(R.id.profileScreen)

        navHome = findViewById(R.id.navHome)
        navStart = findViewById(R.id.navStart)
        navHistory = findViewById(R.id.navHistory)
        navProfile = findViewById(R.id.navProfile)

        typeEasy = findViewById(R.id.typeEasy)
        typeLong = findViewById(R.id.typeLong)
        typeInterval = findViewById(R.id.typeInterval)
        typeWalking = findViewById(R.id.typeWalking)
        beginRunButton = findViewById(R.id.beginRunButton)
        runTypeChip = findViewById(R.id.runTypeChip)
        trackingTimerText = findViewById(R.id.trackingTimerText)
        trackingDistanceText = findViewById(R.id.trackingDistanceText)
        trackingPaceText = findViewById(R.id.trackingPaceText)
        trackingCaloriesText = findViewById(R.id.trackingCaloriesText)
        pauseTimeText = findViewById(R.id.pauseTimeText)
        pauseDistanceText = findViewById(R.id.pauseDistanceText)
        pausePaceText = findViewById(R.id.pausePaceText)
        summaryDurationText = findViewById(R.id.summaryDurationText)
        summaryDistanceText = findViewById(R.id.summaryDistanceText)
        summaryPaceText = findViewById(R.id.summaryPaceText)
        summaryCaloriesText = findViewById(R.id.summaryCaloriesText)
        homeWelcomeText = findViewById(R.id.homeWelcomeText)
        profileInfoText = findViewById(R.id.profileInfoText)
        recentActivityText = findViewById(R.id.recentActivityText)
        showRegisterText = findViewById(R.id.showRegisterText)

        registerNameInput = findViewById(R.id.registerNameInput)
        registerEmailInput = findViewById(R.id.registerEmailInput)
        registerPasswordInput = findViewById(R.id.registerPasswordInput)
        registerConfirmPasswordInput = findViewById(R.id.registerConfirmPasswordInput)
        personalNameInput = findViewById(R.id.personalNameInput)
        personalEmailInput = findViewById(R.id.personalEmailInput)
        personalAgeInput = findViewById(R.id.personalAgeInput)
        personalGenderInput = findViewById(R.id.personalGenderInput)
        personalHeightInput = findViewById(R.id.personalHeightInput)
        personalWeightInput = findViewById(R.id.personalWeightInput)
        editNameInput = findViewById(R.id.editNameInput)
        editEmailInput = findViewById(R.id.editEmailInput)
        editAgeInput = findViewById(R.id.editAgeInput)
        editGenderInput = findViewById(R.id.editGenderInput)
        editHeightInput = findViewById(R.id.editHeightInput)
        editWeightInput = findViewById(R.id.editWeightInput)
    }

    private fun bindActions() {
        // Login -> Register.
        showRegisterText.setOnClickListener {
            loginScreen.visibility = View.GONE
            registerScreen.visibility = View.VISIBLE
        }

        // Register -> Login.
        findViewById<View>(R.id.backToLoginButton).setOnClickListener {
            registerScreen.visibility = View.GONE
            loginScreen.visibility = View.VISIBLE
        }

        // Mo popup quen mat khau.
        findViewById<TextView>(R.id.forgotPasswordText).setOnClickListener {
            loginScreen.visibility = View.GONE
            forgotPasswordOverlay.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.sendResetButton).setOnClickListener {
            handleForgotPassword()
        }

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            handleLogin()
        }

        findViewById<Button>(R.id.createAccountButton).setOnClickListener {
            handleRegister()
        }

        findViewById<Button>(R.id.completeProfileButton).setOnClickListener {
            completePersonalInfo()
        }

        findViewById<TextView>(R.id.logoutButton).setOnClickListener {
            timerHandler.removeCallbacks(timerRunnable)
            authRepository.signOut()
            appScreen.visibility = View.GONE
            trackingScreen.visibility = View.GONE
            summaryScreen.visibility = View.GONE
            pauseOverlay.visibility = View.GONE
            registerScreen.visibility = View.GONE
            personalInfoScreen.visibility = View.GONE
            editProfileOverlay.visibility = View.GONE
            appInfoOverlay.visibility = View.GONE
            loginScreen.visibility = View.VISIBLE
            findViewById<EditText>(R.id.loginEmailInput).setText("")
            findViewById<EditText>(R.id.loginPasswordInput).setText("")
        }

        findViewById<TextView>(R.id.editProfileButton).setOnClickListener {
            fillEditProfileForm()
            appScreen.visibility = View.GONE
            editProfileOverlay.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.saveProfileButton).setOnClickListener {
            saveProfile()
        }

        findViewById<TextView>(R.id.appInfoButton).setOnClickListener {
            appScreen.visibility = View.GONE
            appInfoOverlay.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.closeAppInfoButton).setOnClickListener {
            appInfoOverlay.visibility = View.GONE
            appScreen.visibility = View.VISIBLE
            selectSection(Section.PROFILE)
        }

        navHome.setOnClickListener { selectSection(Section.HOME) }
        navStart.setOnClickListener { selectSection(Section.START) }
        navHistory.setOnClickListener { selectSection(Section.HISTORY) }
        navProfile.setOnClickListener { selectSection(Section.PROFILE) }

        findViewById<Button>(R.id.homeStartRunningButton).setOnClickListener {
            selectSection(Section.START)
        }

        typeEasy.setOnClickListener { selectRunType(getString(R.string.run_type_easy)) }
        typeLong.setOnClickListener { selectRunType(getString(R.string.run_type_long)) }
        typeInterval.setOnClickListener { selectRunType(getString(R.string.run_type_interval)) }
        typeWalking.setOnClickListener { selectRunType(getString(R.string.run_type_walking)) }

        beginRunButton.setOnClickListener {
            appScreen.visibility = View.GONE
            summaryScreen.visibility = View.GONE
            pauseOverlay.visibility = View.GONE
            trackingScreen.visibility = View.VISIBLE
            runTypeChip.text = selectedRunType
            elapsedSeconds = 2
            trackingTimerText.text = getString(R.string.timer_default)
            timerHandler.removeCallbacks(timerRunnable)
            timerHandler.post(timerRunnable)
        }

        findViewById<Button>(R.id.pauseRunButton).setOnClickListener {
            timerHandler.removeCallbacks(timerRunnable)
            trackingScreen.visibility = View.GONE
            pauseOverlay.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.resumeRunButton).setOnClickListener {
            pauseOverlay.visibility = View.GONE
            trackingScreen.visibility = View.VISIBLE
            timerHandler.post(timerRunnable)
        }

        findViewById<Button>(R.id.finishRunButton).setOnClickListener {
            timerHandler.removeCallbacks(timerRunnable)
            pauseOverlay.visibility = View.GONE
            trackingScreen.visibility = View.GONE
            summaryScreen.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.summaryDoneButton).setOnClickListener {
            summaryScreen.visibility = View.GONE
            appScreen.visibility = View.VISIBLE
            selectSection(Section.HOME)
        }
    }

    private fun setupProfilePickers() {
        // Gender va birthday duoc chon theo picker de tranh user nhap sai tay.
        personalGenderInput.setOnClickListener { showGenderPicker(personalGenderInput) }
        editGenderInput.setOnClickListener { showGenderPicker(editGenderInput) }
        personalAgeInput.setOnClickListener { showBirthDatePicker(personalAgeInput) }
        editAgeInput.setOnClickListener { showBirthDatePicker(editAgeInput) }
    }

    private fun openMainApp() {
        // Day la diem vao app chinh sau khi login hoac register hoan tat.
        loginScreen.visibility = View.GONE
        registerScreen.visibility = View.GONE
        personalInfoScreen.visibility = View.GONE
        trackingScreen.visibility = View.GONE
        pauseOverlay.visibility = View.GONE
        forgotPasswordOverlay.visibility = View.GONE
        editProfileOverlay.visibility = View.GONE
        appInfoOverlay.visibility = View.GONE
        summaryScreen.visibility = View.GONE
        appScreen.visibility = View.VISIBLE
        refreshProfileUi()
        selectSection(Section.HOME)
    }

    private fun checkExistingSession() {
        // Neu app mo lai ma user van con session Firebase thi vao thang app chinh.
        val currentUserId = authRepository.getCurrentUserId() ?: return
        loginScreen.visibility = View.GONE
        registerScreen.visibility = View.GONE
        personalInfoScreen.visibility = View.GONE
        openMainApp()
        loadUserProfile(currentUserId)
    }

    private fun selectSection(section: Section) {
        // Dieu huong bottom nav hien tai dang la bat/tat 4 ScrollView chinh.
        homeScreen.visibility = if (section == Section.HOME) View.VISIBLE else View.GONE
        startScreen.visibility = if (section == Section.START) View.VISIBLE else View.GONE
        historyScreen.visibility = if (section == Section.HISTORY) View.VISIBLE else View.GONE
        profileScreen.visibility = if (section == Section.PROFILE) View.VISIBLE else View.GONE

        val primary = ContextCompat.getColor(this, R.color.runna_primary)
        val secondary = ContextCompat.getColor(this, R.color.runna_text_secondary)
        navHome.setTextColor(if (section == Section.HOME) primary else secondary)
        navStart.setTextColor(if (section == Section.START) primary else secondary)
        navHistory.setTextColor(if (section == Section.HISTORY) primary else secondary)
        navProfile.setTextColor(if (section == Section.PROFILE) primary else secondary)
    }

    private fun selectRunType(type: String) {
        selectedRunType = type
        beginRunButton.text = getString(R.string.start_type_format, type)
        styleRunType(typeEasy, type == getString(R.string.run_type_easy))
        styleRunType(typeLong, type == getString(R.string.run_type_long))
        styleRunType(typeInterval, type == getString(R.string.run_type_interval))
        styleRunType(typeWalking, type == getString(R.string.run_type_walking))
    }

    private fun styleRunType(view: TextView, selected: Boolean) {
        if (selected) {
            view.background = ContextCompat.getDrawable(this, R.drawable.bg_button_outline)
            view.setTextColor(ContextCompat.getColor(this, R.color.runna_primary))
        } else {
            view.background = ContextCompat.getDrawable(this, R.drawable.bg_card_subtle)
            view.setTextColor(ContextCompat.getColor(this, R.color.runna_text_primary))
        }
    }

    private fun fillEditProfileForm() {

        editNameInput.setText(currentUser.name)
        editEmailInput.setText(currentUser.email)
        editAgeInput.setText(currentUser.birthDate)
        editGenderInput.setText(currentUser.gender)
        editHeightInput.setText(currentUser.height)
        editWeightInput.setText(currentUser.weight)
    }

    private fun saveProfile() {

        currentUser = currentUser.copy(
            name = editNameInput.text.toString().ifBlank { currentUser.name },
            email = editEmailInput.text.toString().ifBlank { currentUser.email },
            birthDate = editAgeInput.text.toString().ifBlank { currentUser.birthDate },
            age = calculateAgeFromBirthDate(
                editAgeInput.text.toString().ifBlank { currentUser.birthDate }
            ),
            gender = editGenderInput.text.toString().ifBlank { currentUser.gender },
            height = editHeightInput.text.toString().ifBlank { currentUser.height },
            weight = editWeightInput.text.toString().ifBlank { currentUser.weight }
        )
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            // Truong hop local/demo: van cho update UI du chua co user Firebase.
            refreshProfileUi()
            editProfileOverlay.visibility = View.GONE
            appScreen.visibility = View.VISIBLE
            selectSection(Section.PROFILE)
            Toast.makeText(this, getString(R.string.profile_updated_locally), Toast.LENGTH_SHORT).show()
            return
        }


        userRepository.updateUserProfile(currentUser.copy(uid = uid),
            onSuccess = {
                refreshProfileUi()
                editProfileOverlay.visibility = View.GONE
                appScreen.visibility = View.VISIBLE
                selectSection(Section.PROFILE)
                Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
            },
            onFailure = { error ->
                Toast.makeText(this, getString(R.string.update_failed, error), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun refreshProfileUi() {
        // Ham nay la noi dong bo currentUser -> Home/Profile.
        val displayAge = currentUser.age.ifBlank { calculateAgeFromBirthDate(currentUser.birthDate) }
        homeWelcomeText.text = getString(R.string.welcome_back, currentUser.name)
        profileInfoText.text = getString(
            R.string.profile_info_format,
            currentUser.name,
            currentUser.email,
            displayAge,
            currentUser.gender,
            currentUser.height,
            currentUser.weight
        )
        recentActivityText.text = getString(R.string.recent_activity_text)
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

    private fun handleLogin() {
        val email = findViewById<EditText>(R.id.loginEmailInput).text.toString().trim()
        val password = findViewById<EditText>(R.id.loginPasswordInput).text.toString().trim()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, getString(R.string.please_enter_email_password), Toast.LENGTH_SHORT).show()
            return
        }


        authRepository.login(
            email = email,
            password = password,
            onSuccess = { uid ->
                openMainApp()
                loadUserProfile(uid)
                Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
            },
            onFailure = { error ->
                Toast.makeText(this, getString(R.string.login_failed, error), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun handleRegister() {
        val name = registerNameInput.text.toString().trim()
        val email = registerEmailInput.text.toString().trim()
        val password = registerPasswordInput.text.toString().trim()
        val confirmPassword = registerConfirmPasswordInput.text.toString().trim()

        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, getString(R.string.fill_all_register_fields), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, getString(R.string.password_min_length), Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show()
            return
        }

        // Register o day chi tao tai khoan Auth truoc.
        authRepository.register(
            email = email,
            password = password,
            onSuccess = { uid ->
                currentUser = currentUser.copy(name = name, email = email)
                pendingRegisterUid = uid
                showPersonalInfoScreen(name, email)
            },
            onFailure = { error ->
                Toast.makeText(this, getString(R.string.register_failed, error), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun handleForgotPassword() {
        val resetEmail = findViewById<EditText>(R.id.forgotPasswordEmailInput).text.toString().trim()

        if (resetEmail.isBlank()) {
            Toast.makeText(this, getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase se gui email reset password den dia chi da nhap.
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

    private fun createUserProfile(uid: String?, email: String, name: String) {
        if (uid == null) {
            Toast.makeText(this, getString(R.string.user_id_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        // Day la buoc ghi profile dau tien sau khi register xong.
        userRepository.createUserProfile(
            user = currentUser.copy(uid = uid, name = name, email = email),
            onSuccess = {
                openMainApp()
                loadUserProfile(uid)
                clearRegisterFields()
                Toast.makeText(this, getString(R.string.register_successful), Toast.LENGTH_SHORT).show()
            },
            onFailure = { error ->
                Toast.makeText(this, getString(R.string.profile_save_failed, error), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadUserProfile(uid: String?) {
        if (uid == null) return

        // Moi lan login/open app se doc profile tu Firestore, neu thieu field nao thi giu fallback local.
        userRepository.loadUserProfile(
            uid = uid,
            onSuccess = { user ->
                currentUser = user.copy(
                    name = user.name.ifBlank { currentUser.name },
                    email = user.email.ifBlank { currentUser.email },
                    age = user.age.ifBlank {
                        calculateAgeFromBirthDate(user.birthDate).ifBlank { currentUser.age }
                    },
                    birthDate = user.birthDate.ifBlank { currentUser.birthDate },
                    gender = user.gender.ifBlank { currentUser.gender },
                    height = user.height.ifBlank { currentUser.height },
                    weight = user.weight.ifBlank { currentUser.weight }
                )
                refreshProfileUi()
            },
            onFailure = { error ->
                Toast.makeText(this, getString(R.string.load_profile_failed, error), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun clearRegisterFields() {

        registerNameInput.setText("")
        registerEmailInput.setText("")
        registerPasswordInput.setText("")
        registerConfirmPasswordInput.setText("")
    }

    private fun showPersonalInfoScreen(name: String, email: String) {
        // Man nay bat buoc xuat hien sau register de user bo sung thong tin ca nhan.
        loginScreen.visibility = View.GONE
        registerScreen.visibility = View.GONE
        personalInfoScreen.visibility = View.VISIBLE
        personalNameInput.setText(name)
        personalEmailInput.setText(email)
        personalAgeInput.setText(currentUser.birthDate)
        personalGenderInput.setText(currentUser.gender)
        personalHeightInput.setText(currentUser.height)
        personalWeightInput.setText(currentUser.weight)
    }

    private fun completePersonalInfo() {
        val finalUid = pendingRegisterUid ?: authRepository.getCurrentUserId()
        if (finalUid == null) {
            Toast.makeText(this, getString(R.string.please_register_first), Toast.LENGTH_SHORT).show()
            return
        }

        // Gop du lieu tu form vao currentUser roi moi ghi Firestore.
        currentUser = currentUser.copy(
            name = personalNameInput.text.toString().trim().ifBlank { currentUser.name },
            email = personalEmailInput.text.toString().trim().ifBlank { currentUser.email },
            birthDate = personalAgeInput.text.toString().trim().ifBlank { currentUser.birthDate },
            age = calculateAgeFromBirthDate(
                personalAgeInput.text.toString().trim().ifBlank { currentUser.birthDate }
            ),
            gender = personalGenderInput.text.toString().trim().ifBlank { currentUser.gender },
            height = personalHeightInput.text.toString().trim().ifBlank { currentUser.height },
            weight = personalWeightInput.text.toString().trim().ifBlank { currentUser.weight }
        )

        createUserProfile(finalUid, currentUser.email, currentUser.name)
        pendingRegisterUid = null
    }

    private fun showGenderPicker(target: EditText) {
        val genderOptions = resources.getStringArray(R.array.gender_options)
        AlertDialog.Builder(this)
            .setItems(genderOptions) { _, which ->
                target.setText(genderOptions[which])
            }
            .show()
    }

    private fun showBirthDatePicker(target: EditText) {
        val calendar = parseBirthDate(target.text.toString()) ?: Calendar.getInstance().apply {
            add(Calendar.YEAR, -18)
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
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

    private enum class Section {
        HOME, START, HISTORY, PROFILE
    }
}
