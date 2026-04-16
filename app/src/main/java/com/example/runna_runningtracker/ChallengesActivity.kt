package com.example.runna_runningtracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
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
import com.example.runna_runningtracker.data.model.ChallengeMemberWithChallenge
import com.example.runna_runningtracker.data.model.UiState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class ChallengesActivity : AppCompatActivity() {

    private enum class ChallengeTab { POPULAR, ENDING_SOON, MY_CHALLENGES }

    // Activity render UI con data va state se do ViewModel quan ly
    private val viewModel: ChallengeViewModel by viewModels()

    private lateinit var tabPopular: LinearLayout
    private lateinit var tabEndingSoon: LinearLayout
    private lateinit var tabMyChallenges: LinearLayout
    private lateinit var tabPopularIcon: ImageView
    private lateinit var tabEndingSoonIcon: ImageView
    private lateinit var tabMyChallengesIcon: ImageView
    private lateinit var tabPopularText: TextView
    private lateinit var tabEndingSoonText: TextView
    private lateinit var tabMyChallengesText: TextView
    private lateinit var tvChallengesHint: TextView
    private lateinit var progressChallenges: ProgressBar
    private lateinit var tvChallengesEmpty: TextView
    private lateinit var challengesListContainer: LinearLayout

    private var currentTab = ChallengeTab.POPULAR
    private var currentUserId: String? = null
    private var currentUserName: String = "Runner"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenges)

        NavigationHelper.setupBottomNav(this, NavigationHelper.ActiveTab.CHALLENGES)

        currentUserId = AuthSessionHelper.getCurrentUserId(this)
        currentUserName = AuthSessionHelper.getCurrentDisplayName(this)

        bindViews()
        bindActions()
        observeViewModel()

        // Vao man thi mac dinh load tab Popular truoc
        selectTab(ChallengeTab.POPULAR)
    }

    override fun onResume() {
        super.onResume()
        if (::challengesListContainer.isInitialized) {
            refreshCurrentTab()
        }
    }

    private fun bindViews() {
        // Gom view cua tab header va list dong ben duoi
        tabPopular = findViewById(R.id.tabPopular)
        tabEndingSoon = findViewById(R.id.tabEndingSoon)
        tabMyChallenges = findViewById(R.id.tabMyChallenges)
        tabPopularIcon = findViewById(R.id.tabPopularIcon)
        tabEndingSoonIcon = findViewById(R.id.tabEndingSoonIcon)
        tabMyChallengesIcon = findViewById(R.id.tabMyChallengesIcon)
        tabPopularText = findViewById(R.id.tabPopularText)
        tabEndingSoonText = findViewById(R.id.tabEndingSoonText)
        tabMyChallengesText = findViewById(R.id.tabMyChallengesText)
        tvChallengesHint = findViewById(R.id.tvChallengesHint)
        progressChallenges = findViewById(R.id.progressChallenges)
        tvChallengesEmpty = findViewById(R.id.tvChallengesEmpty)
        challengesListContainer = findViewById(R.id.challengesListContainer)
    }

    private fun bindActions() {
        // Nut cong se sang man tao challenge moi
        findViewById<ImageView>(R.id.btnAddChallenge).setOnClickListener {
            startActivity(Intent(this, CreateChallengeActivity::class.java))
        }

        tabPopular.setOnClickListener { selectTab(ChallengeTab.POPULAR) }
        tabEndingSoon.setOnClickListener { selectTab(ChallengeTab.ENDING_SOON) }
        tabMyChallenges.setOnClickListener { selectTab(ChallengeTab.MY_CHALLENGES) }
    }

    private fun observeViewModel() {
        // 2 tab public dung chung challengeListState
        viewModel.challengeListState.observe(this) { state ->
            if (currentTab == ChallengeTab.MY_CHALLENGES) return@observe

            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> showLoading(true)
                is UiState.Success -> {
                    showLoading(false)
                    renderChallengeList(state.data)
                }
                is UiState.Error -> {
                    showLoading(false)
                    showEmpty(state.message)
                }
            }
        }

        // Tab my challenges co state rieng vi can progress cua member
        viewModel.joinedChallengesState.observe(this) { state ->
            if (currentTab != ChallengeTab.MY_CHALLENGES) return@observe

            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> showLoading(true)
                is UiState.Success -> {
                    showLoading(false)
                    renderJoinedChallenges(state.data)
                }
                is UiState.Error -> {
                    showLoading(false)
                    showEmpty(state.message)
                }
            }
        }

        // Action state xu ly create join va cac thao tac 1 lan
        viewModel.actionState.observe(this) { state ->
            when (state) {
                UiState.Idle, UiState.Loading -> Unit
                is UiState.Success -> {
                    Toast.makeText(this, getString(R.string.challenge_action_success), Toast.LENGTH_SHORT).show()
                    refreshCurrentTab()
                    viewModel.clearActionState()
                }
                is UiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearActionState()
                }
            }
        }
    }

    private fun selectTab(tab: ChallengeTab) {
        // Moi lan doi tab thi doi style va load lai data dung tab do
        currentTab = tab
        updateTabStyles()
        refreshCurrentTab()
    }

    private fun updateTabStyles() {
        // Tab duoc chon se doi mau background text va icon
        val activeColor = ContextCompat.getColor(this, R.color.white)
        val inactiveColor = ContextCompat.getColor(this, R.color.runna_text_secondary)

        tabPopular.setBackgroundResource(if (currentTab == ChallengeTab.POPULAR) R.drawable.bg_tab_selected else R.drawable.bg_tab_unselected)
        tabEndingSoon.setBackgroundResource(if (currentTab == ChallengeTab.ENDING_SOON) R.drawable.bg_tab_selected else R.drawable.bg_tab_unselected)
        tabMyChallenges.setBackgroundResource(if (currentTab == ChallengeTab.MY_CHALLENGES) R.drawable.bg_tab_selected else R.drawable.bg_tab_unselected)

        tabPopularText.setTextColor(if (currentTab == ChallengeTab.POPULAR) activeColor else inactiveColor)
        tabEndingSoonText.setTextColor(if (currentTab == ChallengeTab.ENDING_SOON) activeColor else inactiveColor)
        tabMyChallengesText.setTextColor(if (currentTab == ChallengeTab.MY_CHALLENGES) activeColor else inactiveColor)

        tabPopularIcon.setColorFilter(if (currentTab == ChallengeTab.POPULAR) activeColor else inactiveColor)
        tabEndingSoonIcon.setColorFilter(if (currentTab == ChallengeTab.ENDING_SOON) activeColor else inactiveColor)
        tabMyChallengesIcon.setColorFilter(if (currentTab == ChallengeTab.MY_CHALLENGES) activeColor else ContextCompat.getColor(this, R.color.runna_primary))

        tvChallengesHint.text = when (currentTab) {
            ChallengeTab.POPULAR -> getString(R.string.challenges_hint_popular)
            ChallengeTab.ENDING_SOON -> getString(R.string.challenges_hint_ending)
            ChallengeTab.MY_CHALLENGES -> getString(R.string.challenges_hint_joined)
        }
    }

    private fun refreshCurrentTab() {
        // Dieu huong data theo tab dang duoc user xem
        when (currentTab) {
            ChallengeTab.POPULAR -> viewModel.loadPopularChallenges()
            ChallengeTab.ENDING_SOON -> viewModel.loadEndingSoonChallenges()
            ChallengeTab.MY_CHALLENGES -> {
                val userId = currentUserId
                if (userId.isNullOrBlank()) {
                    showLoading(false)
                    showEmpty(getString(R.string.challenge_login_required))
                } else {
                    viewModel.loadJoinedChallenges(userId)
                }
            }
        }
    }

    private fun renderChallengeList(items: List<Challenge>) {
        // Render list public challenge cho Popular va Ending Soon
        challengesListContainer.removeAllViews()
        if (items.isEmpty()) {
            showEmpty(getString(R.string.challenges_empty))
            return
        }

        tvChallengesEmpty.visibility = View.GONE
        items.forEach { challenge ->
            challengesListContainer.addView(
                createChallengeCard(challenge = challenge, joined = false, memberProgressMeters = null)
            )
        }
    }

    private fun renderJoinedChallenges(items: List<ChallengeMemberWithChallenge>) {
        // Render list da join kem progress hien tai cua user
        challengesListContainer.removeAllViews()
        if (items.isEmpty()) {
            showEmpty(getString(R.string.challenges_joined_empty))
            return
        }

        tvChallengesEmpty.visibility = View.GONE
        items.forEach { item ->
            challengesListContainer.addView(
                createChallengeCard(
                    challenge = item.challenge,
                    joined = true,
                    memberProgressMeters = item.member.progressDistanceMeters,
                    completed = item.member.isCompleted
                )
            )
        }
    }

    private fun createChallengeCard(
        challenge: Challenge,
        joined: Boolean,
        memberProgressMeters: Long?,
        completed: Boolean = false
    ): View {
        // Moi card duoc bind lai tu data Firestore truoc khi add vao list
        val card = LayoutInflater.from(this).inflate(
            R.layout.item_challenge_card,
            challengesListContainer,
            false
        )

        val tvType = card.findViewById<TextView>(R.id.tvChallengeType)
        val tvTitle = card.findViewById<TextView>(R.id.tvChallengeTitle)
        val tvDates = card.findViewById<TextView>(R.id.tvChallengeDates)
        val tvGoal = card.findViewById<TextView>(R.id.tvChallengeGoal)
        val tvParticipants = card.findViewById<TextView>(R.id.tvChallengeParticipants)
        val btnJoin = card.findViewById<TextView>(R.id.btnJoinChallenge)

        tvType.text = getString(R.string.challenge_distance)
        tvTitle.text = challenge.title
        tvDates.text = formatDateRange(challenge.startAt, challenge.endAt)

        val goalKm = challenge.goalDistanceMeters / 1000.0
        tvGoal.text = if (memberProgressMeters != null) {
            val progressKm = memberProgressMeters / 1000.0
            getString(R.string.challenge_progress_compact, progressKm, goalKm)
        } else {
            getString(R.string.challenge_goal_run_format, goalKm)
        }
        tvParticipants.text = challenge.participantCount.toString()

        if (joined) {
            // Da join thi khoa nut de tranh join lai
            btnJoin.text = if (completed) getString(R.string.completed) else getString(R.string.joined)
            btnJoin.background = ContextCompat.getDrawable(this, R.drawable.bg_chip_gray)
            btnJoin.setTextColor(ContextCompat.getColor(this, R.color.runna_text_secondary))
            btnJoin.isEnabled = false
        } else {
            // Chua join thi bam nut de tao membership tren Firestore
            btnJoin.text = getString(R.string.join)
            btnJoin.background = ContextCompat.getDrawable(this, R.drawable.bg_button_primary)
            btnJoin.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnJoin.isEnabled = true
            btnJoin.setOnClickListener {
                val userId = currentUserId
                if (userId.isNullOrBlank()) {
                    Toast.makeText(this, getString(R.string.challenge_login_required), Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.joinChallenge(challenge.id, userId, currentUserName)
                }
            }
        }

        // Bam card thi sang man detail de xem progress va leaderboard
        card.setOnClickListener {
            startActivity(
                Intent(this, ChallengeDetailsActivity::class.java)
                    .putExtra(ChallengeDetailsActivity.EXTRA_CHALLENGE_ID, challenge.id)
            )
        }
        return card
    }

    private fun showLoading(show: Boolean) {
        // Loading thi an list cu de user thay ro dang cho data moi
        progressChallenges.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            tvChallengesEmpty.visibility = View.GONE
            challengesListContainer.removeAllViews()
        }
    }

    private fun showEmpty(message: String) {
        // Empty va error deu dung cung 1 text de thong bao
        challengesListContainer.removeAllViews()
        tvChallengesEmpty.text = message
        tvChallengesEmpty.visibility = View.VISIBLE
    }

    private fun formatDateRange(startAt: Timestamp?, endAt: Timestamp?): String {
        // Date tren card duoc format gon de de doc trong list
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        val start = startAt?.toDate()?.let(formatter::format) ?: "--"
        val end = endAt?.toDate()?.let(formatter::format) ?: "--"
        return getString(R.string.challenge_date_range_format, start, end)
    }
}
