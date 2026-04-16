package com.example.runna_runningtracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.runna_runningtracker.data.model.CreateChallengeInput
import com.example.runna_runningtracker.data.model.UiState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateChallengeActivity : AppCompatActivity() {

    // Man tao challenge chi can 1 ViewModel de gui request len Firestore
    private val viewModel: ChallengeViewModel by viewModels()

    private lateinit var etChallengeTitle: EditText
    private lateinit var etChallengeDescription: EditText
    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var etGoalDistance: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_challenge)

        bindViews()
        bindActions()
        observeViewModel()
    }

    private fun bindViews() {
        // Form tao challenge gom title description date va goal
        etChallengeTitle = findViewById(R.id.etChallengeTitle)
        etChallengeDescription = findViewById(R.id.etChallengeDescription)
        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        etGoalDistance = findViewById(R.id.etGoalDistance)
    }

    private fun bindActions() {
        // Date duoc chon bang picker de tranh nhap sai format
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCreate).setOnClickListener { submitCreateChallenge() }
        etStartDate.setOnClickListener { showDatePicker(etStartDate) }
        etEndDate.setOnClickListener { showDatePicker(etEndDate) }
    }

    private fun observeViewModel() {
        // Tao challenge xong thi toast va dong man de quay ve list
        viewModel.actionState.observe(this) { state ->
            when (state) {
                UiState.Idle, UiState.Loading -> Unit
                is UiState.Success -> {
                    Toast.makeText(this, getString(R.string.challenge_created_success), Toast.LENGTH_SHORT).show()
                    viewModel.clearActionState()
                    finish()
                }
                is UiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearActionState()
                }
            }
        }
    }

    private fun submitCreateChallenge() {
        // Lay data form roi validate truoc khi goi repo
        val title = etChallengeTitle.text.toString().trim()
        val description = etChallengeDescription.text.toString().trim()
        val goalKm = etGoalDistance.text.toString().trim().toDoubleOrNull()
        val startDate = parseDate(etStartDate.text.toString().trim())
        val endDate = parseDate(etEndDate.text.toString().trim())
        val currentUserId = AuthSessionHelper.getCurrentUserId(this)
        val currentUserName = AuthSessionHelper.getCurrentDisplayName(this)

        // Validate theo dung rule trong plan challenge
        when {
            title.isBlank() -> {
                etChallengeTitle.error = getString(R.string.challenge_error_title_required)
                return
            }
            goalKm == null || goalKm <= 0.0 -> {
                etGoalDistance.error = getString(R.string.challenge_error_goal_required)
                return
            }
            startDate == null || endDate == null -> {
                Toast.makeText(this, getString(R.string.challenge_error_dates_required), Toast.LENGTH_SHORT).show()
                return
            }
            !endDate.after(startDate) -> {
                Toast.makeText(this, getString(R.string.challenge_error_invalid_range), Toast.LENGTH_SHORT).show()
                return
            }
            !endDate.after(Date()) -> {
                Toast.makeText(this, getString(R.string.challenge_error_end_future), Toast.LENGTH_SHORT).show()
                return
            }
            currentUserId.isNullOrBlank() -> {
                Toast.makeText(this, getString(R.string.challenge_login_required), Toast.LENGTH_SHORT).show()
                return
            }
        }

        val safeStartDate = startDate ?: return
        val safeEndDate = endDate ?: return
        val safeUserId = currentUserId ?: return

        // Goal duoc doi sang met de thong nhat voi run distance
        val input = CreateChallengeInput(
            title = title,
            description = description,
            goalDistanceMeters = (goalKm * 1000.0).toLong(),
            startAt = Timestamp(safeStartDate),
            endAt = Timestamp(safeEndDate),
            createdBy = safeUserId,
            createdByName = currentUserName
        )
        viewModel.createChallenge(input)
    }

    private fun showDatePicker(target: EditText) {
        // Neu field da co ngay thi mo lai ngay do de user sua nhanh
        val calendar = Calendar.getInstance()
        val currentValue = target.text.toString()
        parseDate(currentValue)?.let { parsed ->
            calendar.time = parsed
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                target.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selected.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun parseDate(value: String): Date? {
        // Parse text ngay thanh Date de convert sang Firebase Timestamp
        if (value.isBlank()) return null
        return runCatching {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(value)
        }.getOrNull()
    }
}
