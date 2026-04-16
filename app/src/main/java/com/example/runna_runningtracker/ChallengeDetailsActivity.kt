package com.example.runna_runningtracker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.runna_runningtracker.data.model.Challenge
import com.example.runna_runningtracker.data.model.ChallengeMember
import com.example.runna_runningtracker.data.model.LeaderboardEntry
import com.example.runna_runningtracker.data.model.UiState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class ChallengeDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHALLENGE_ID = "extra_challenge_id"
    }

    private val viewModel: ChallengeViewModel by viewModels()

    // currentChallenge va currentMembership giu state hien tai cua man detail
    private var challengeId: String = ""
    private var currentChallenge: Challenge? = null
    private var currentMembership: ChallengeMember? = null
    private var currentUserId: String? = null
    private var currentUserName: String = "Runner"

    private lateinit var tvDetailType: TextView
    private lateinit var tvDetailTitle: TextView
    private lateinit var tvDetailDescription: TextView
    private lateinit var tvDetailDates: TextView
    private lateinit var tvDetailGoal: TextView
    private lateinit var tvDetailParticipants: TextView
    private lateinit var tvDetailDaysLeft: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgressValue: TextView
    private lateinit var tvProgressRemaining: TextView
    private lateinit var btnJoinChallenge: TextView
    private lateinit var leaderboardContainer: LinearLayout
    private lateinit var tvLeaderboardEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_details)

        // challengeId la khoa de man nay biet can doc challenge nao
        challengeId = intent.getStringExtra(EXTRA_CHALLENGE_ID).orEmpty()
        currentUserId = AuthSessionHelper.getCurrentUserId(this)
        currentUserName = AuthSessionHelper.getCurrentDisplayName(this)

        if (challengeId.isBlank()) {
            Toast.makeText(this, getString(R.string.challenge_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        bindActions()
        observeViewModel()

        // Vao man detail thi load challenge truoc roi moi load membership
        viewModel.loadChallengeDetails(challengeId)
        currentUserId?.let { viewModel.loadMembership(challengeId, it) }
    }

    private fun bindViews() {
        // Gom view cua card thong tin challenge progress va leaderboard
        tvDetailType = findViewById(R.id.tvDetailType)
        tvDetailTitle = findViewById(R.id.tvDetailTitle)
        tvDetailDescription = findViewById(R.id.tvDetailDescription)
        tvDetailDates = findViewById(R.id.tvDetailDates)
        tvDetailGoal = findViewById(R.id.tvDetailGoal)
        tvDetailParticipants = findViewById(R.id.tvDetailParticipants)
        tvDetailDaysLeft = findViewById(R.id.tvDetailDaysLeft)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        progressBar = findViewById(R.id.progressBar)
        tvProgressValue = findViewById(R.id.tvProgressValue)
        tvProgressRemaining = findViewById(R.id.tvProgressRemaining)
        btnJoinChallenge = findViewById(R.id.btnJoinChallenge)
        leaderboardContainer = findViewById(R.id.leaderboardContainer)
        tvLeaderboardEmpty = findViewById(R.id.tvLeaderboardEmpty)
    }

    private fun bindActions() {
        // User chi join duoc khi dang login va chua co membership
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        btnJoinChallenge.setOnClickListener {
            val userId = currentUserId
            val challenge = currentChallenge
            if (challenge == null || userId.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.challenge_login_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentMembership == null) {
                viewModel.joinChallenge(challenge.id, userId, currentUserName)
            }
        }
    }

    private fun observeViewModel() {
        // Challenge detail state dung cho title desc goal participant
        viewModel.challengeDetailsState.observe(this) { state ->
            when (state) {
                UiState.Idle, UiState.Loading -> Unit
                is UiState.Success -> {
                    // Co detail roi moi tinh duoc leaderboard theo goal
                    currentChallenge = state.data
                    bindChallenge(state.data)
                    viewModel.loadLeaderboard(state.data.id, state.data.goalDistanceMeters)
                }
                is UiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Membership state cho biet user da join chua va da chay duoc bao nhieu
        viewModel.membershipState.observe(this) { state ->
            when (state) {
                UiState.Idle, UiState.Loading -> Unit
                is UiState.Success -> {
                    currentMembership = state.data
                    bindMembership(state.data, currentChallenge)
                }
                is UiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Leaderboard state chi render top nguoi dang dan dau
        viewModel.leaderboardState.observe(this) { state ->
            when (state) {
                UiState.Idle, UiState.Loading -> Unit
                is UiState.Success -> bindLeaderboard(state.data)
                is UiState.Error -> {
                    tvLeaderboardEmpty.visibility = View.VISIBLE
                    tvLeaderboardEmpty.text = state.message
                    leaderboardContainer.removeAllViews()
                }
            }
        }

        // Join thanh cong thi reload lai membership va detail de cap nhat man
        viewModel.actionState.observe(this) { state ->
            when (state) {
                UiState.Idle, UiState.Loading -> Unit
                is UiState.Success -> {
                    Toast.makeText(this, getString(R.string.challenge_action_success), Toast.LENGTH_SHORT).show()
                    currentUserId?.let { viewModel.loadMembership(challengeId, it) }
                    viewModel.loadChallengeDetails(challengeId)
                    viewModel.clearActionState()
                }
                is UiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearActionState()
                }
            }
        }
    }

    private fun bindChallenge(challenge: Challenge) {
        // Bind thong tin challenge chung len card phia tren
        tvDetailType.text = getString(R.string.challenge_distance)
        tvDetailTitle.text = challenge.title
        tvDetailDescription.text = challenge.description
        tvDetailDates.text = formatDateRange(challenge.startAt, challenge.endAt)
        tvDetailGoal.text = getString(
            R.string.challenge_goal_plain_format,
            challenge.goalDistanceMeters / 1000.0
        )
        tvDetailParticipants.text = getString(
            R.string.challenge_participants_format,
            challenge.participantCount
        )
        tvDetailDaysLeft.text = getString(
            R.string.challenge_days_left_format,
            calculateDaysLeft(challenge)
        )
        bindMembership(currentMembership, challenge)
    }

    private fun bindMembership(member: ChallengeMember?, challenge: Challenge?) {
        // Progress card tinh tu member progress va goal cua challenge
        val challengeData = challenge ?: return
        val goalMeters = challengeData.goalDistanceMeters.toDouble().coerceAtLeast(1.0)
        val progressMeters = member?.progressDistanceMeters?.toDouble() ?: 0.0
        val percent = ((progressMeters / goalMeters) * 100.0).coerceIn(0.0, 100.0)
        val progressKm = progressMeters / 1000.0
        val goalKm = goalMeters / 1000.0
        val remainingKm = (goalKm - progressKm).coerceAtLeast(0.0)

        tvProgressPercent.text = getString(R.string.challenge_percent_format, percent)
        progressBar.progress = percent.toInt()
        tvProgressValue.text = getString(R.string.challenge_progress_format, progressKm, goalKm)
        tvProgressRemaining.text = getString(R.string.challenge_remaining_format, remainingKm)

        if (member == null) {
            // Chua join thi hien nut join de user tham gia ngay tai detail
            btnJoinChallenge.text = getString(R.string.join)
            btnJoinChallenge.background = ContextCompat.getDrawable(this, R.drawable.bg_button_primary)
            btnJoinChallenge.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnJoinChallenge.isEnabled = true
        } else {
            // Da join thi doi nut thanh joined hoac completed va khoa lai
            btnJoinChallenge.text = if (member.isCompleted) {
                getString(R.string.completed)
            } else {
                getString(R.string.joined)
            }
            btnJoinChallenge.background = ContextCompat.getDrawable(this, R.drawable.bg_chip_gray)
            btnJoinChallenge.setTextColor(ContextCompat.getColor(this, R.color.runna_text_secondary))
            btnJoinChallenge.isEnabled = false
        }
    }

    private fun bindLeaderboard(entries: List<LeaderboardEntry>) {
        // Moi lan co data moi thi xoa list cu roi ve lai top 10
        leaderboardContainer.removeAllViews()
        if (entries.isEmpty()) {
            tvLeaderboardEmpty.visibility = View.VISIBLE
            return
        }

        tvLeaderboardEmpty.visibility = View.GONE
        entries.forEachIndexed { index, entry ->
            leaderboardContainer.addView(createLeaderboardRow(entry))
            if (index != entries.lastIndex) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    ).also { it.topMargin = 12.dp; it.bottomMargin = 12.dp }
                    setBackgroundColor(ContextCompat.getColor(this@ChallengeDetailsActivity, android.R.color.darker_gray))
                }
                leaderboardContainer.addView(divider)
            }
        }
    }

    private fun createLeaderboardRow(entry: LeaderboardEntry): View {
        // Tao tung dong bang code de de bind rank ten va phan tram
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val rankView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(32.dp, 32.dp)
            gravity = android.view.Gravity.CENTER
            background = ContextCompat.getDrawable(this@ChallengeDetailsActivity, R.drawable.bg_circle_orange)
            text = entry.rank.toString()
            setTextColor(ContextCompat.getColor(this@ChallengeDetailsActivity, R.color.white))
        }
        row.addView(rankView)

        val avatarPlaceholder = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(40.dp, 40.dp).also { it.marginStart = 12.dp }
            background = ContextCompat.getDrawable(this@ChallengeDetailsActivity, R.drawable.bg_circle_orange)
            backgroundTintList = ContextCompat.getColorStateList(this@ChallengeDetailsActivity, android.R.color.darker_gray)
        }
        row.addView(avatarPlaceholder)

        val textGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .also { it.marginStart = 12.dp }
        }

        textGroup.addView(TextView(this).apply {
            text = entry.displayName
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@ChallengeDetailsActivity, R.color.runna_text_primary))
        })
        textGroup.addView(TextView(this).apply {
            text = getString(R.string.challenge_goal_plain_format, entry.progressDistanceMeters / 1000.0)
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@ChallengeDetailsActivity, R.color.runna_text_secondary))
        })
        row.addView(textGroup)

        row.addView(TextView(this).apply {
            text = getString(R.string.challenge_percent_format, entry.progressPercent)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@ChallengeDetailsActivity, R.color.runna_text_primary))
        })

        return row
    }

    private fun formatDateRange(startAt: Timestamp?, endAt: Timestamp?): String {
        // Detail dung format dai hon list de user xem ro thoi gian
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val start = startAt?.toDate()?.let(formatter::format) ?: "--"
        val end = endAt?.toDate()?.let(formatter::format) ?: "--"
        return getString(R.string.challenge_date_range_long_format, start, end)
    }

    private fun calculateDaysLeft(challenge: Challenge): Long {
        // Tinh so ngay con lai de hien chip days left
        val end = challenge.endAt?.toDate() ?: return 0
        val diff = end.time - System.currentTimeMillis()
        return (diff / (1000L * 60L * 60L * 24L)).coerceAtLeast(0L)
    }

    private val Int.dp: Int
        // Helper nay doi dp sang px khi tao view bang code
        get() = (this * resources.displayMetrics.density).toInt()
}
