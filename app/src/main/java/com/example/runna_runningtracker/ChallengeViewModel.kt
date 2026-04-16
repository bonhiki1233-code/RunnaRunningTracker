package com.example.runna_runningtracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.runna_runningtracker.data.model.Challenge
import com.example.runna_runningtracker.data.model.ChallengeMember
import com.example.runna_runningtracker.data.model.ChallengeMemberWithChallenge
import com.example.runna_runningtracker.data.model.CompletedRun
import com.example.runna_runningtracker.data.model.CreateChallengeInput
import com.example.runna_runningtracker.data.model.LeaderboardEntry
import com.example.runna_runningtracker.data.model.UiState
import com.example.runna_runningtracker.data.repository.ChallengeRepository
import com.google.firebase.firestore.ListenerRegistration

class ChallengeViewModel : ViewModel() {

    // ViewModel giu state va goi repo de Activity khong phai cham Firestore
    private val repository = ChallengeRepository()

    private val _challengeListState = MutableLiveData<UiState<List<Challenge>>>(UiState.Idle)
    val challengeListState: LiveData<UiState<List<Challenge>>> = _challengeListState

    private val _joinedChallengesState =
        MutableLiveData<UiState<List<ChallengeMemberWithChallenge>>>(UiState.Idle)
    val joinedChallengesState: LiveData<UiState<List<ChallengeMemberWithChallenge>>> =
        _joinedChallengesState

    private val _challengeDetailsState = MutableLiveData<UiState<Challenge>>(UiState.Idle)
    val challengeDetailsState: LiveData<UiState<Challenge>> = _challengeDetailsState

    private val _membershipState = MutableLiveData<UiState<ChallengeMember?>>(UiState.Idle)
    val membershipState: LiveData<UiState<ChallengeMember?>> = _membershipState

    private val _leaderboardState =
        MutableLiveData<UiState<List<LeaderboardEntry>>>(UiState.Idle)
    val leaderboardState: LiveData<UiState<List<LeaderboardEntry>>> = _leaderboardState

    private val _actionState = MutableLiveData<UiState<String>>(UiState.Idle)
    val actionState: LiveData<UiState<String>> = _actionState

    private var publicChallengesRegistration: ListenerRegistration? = null
    private var joinedChallengesRegistration: ListenerRegistration? = null
    private var detailsRegistration: ListenerRegistration? = null
    private var membershipRegistration: ListenerRegistration? = null
    private var leaderboardRegistration: ListenerRegistration? = null

    fun loadPopularChallenges() {
        // Tab popular doc challenge public active roi sort theo participant
        _challengeListState.value = UiState.Loading
        publicChallengesRegistration?.remove()
        publicChallengesRegistration = repository.observePopularChallenges { result ->
            _challengeListState.postValue(
                result.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Unable to load challenges") }
                )
            )
        }
    }

    fun loadEndingSoonChallenges() {
        // Tab ending soon doc challenge public active roi sort theo ngay ket thuc
        _challengeListState.value = UiState.Loading
        publicChallengesRegistration?.remove()
        publicChallengesRegistration = repository.observeEndingSoonChallenges { result ->
            _challengeListState.postValue(
                result.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Unable to load challenges") }
                )
            )
        }
    }

    fun loadJoinedChallenges(userId: String) {
        // Tab my challenges doc membership truoc roi ghep nguoc challenge
        _joinedChallengesState.value = UiState.Loading
        joinedChallengesRegistration?.remove()
        joinedChallengesRegistration = repository.observeJoinedChallenges(userId) { result ->
            _joinedChallengesState.postValue(
                result.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Unable to load joined challenges") }
                )
            )
        }
    }

    fun createChallenge(input: CreateChallengeInput) {
        // Action state dung cho create join va cap nhat progress 1 lan
        _actionState.value = UiState.Loading
        repository.createChallenge(input) { result ->
            _actionState.postValue(
                result.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Unable to create challenge") }
                )
            )
        }
    }

    fun joinChallenge(challengeId: String, userId: String, displayName: String) {
        // Join can userId va ten de tao member doc va leaderboard snapshot
        _actionState.value = UiState.Loading
        repository.joinChallenge(challengeId, userId, displayName) { result ->
            _actionState.postValue(
                result.fold(
                    onSuccess = { UiState.Success("joined") },
                    onFailure = { UiState.Error(it.message ?: "Unable to join challenge") }
                )
            )
        }
    }

    fun loadChallengeDetails(challengeId: String) {
        // Man detail can doc challenge rieng de hien title goal va date
        _challengeDetailsState.value = UiState.Loading
        detailsRegistration?.remove()
        detailsRegistration = repository.observeChallengeDetails(challengeId) { result ->
            _challengeDetailsState.postValue(
                result.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Unable to load challenge") }
                )
            )
        }
    }

    fun loadMembership(challengeId: String, userId: String) {
        // Membership cho biet user da join chua va dang duoc bao nhieu km
        _membershipState.value = UiState.Loading
        membershipRegistration?.remove()
        membershipRegistration = repository.observeMembership(challengeId, userId) { result ->
            _membershipState.postValue(
                result.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Unable to load progress") }
                )
            )
        }
    }

    fun loadLeaderboard(challengeId: String, goalDistanceMeters: Long) {
        // Leaderboard duoc tinh tu challenge_members cua challenge hien tai
        _leaderboardState.value = UiState.Loading
        leaderboardRegistration?.remove()
        leaderboardRegistration =
            repository.observeLeaderboard(challengeId, goalDistanceMeters) { result ->
                _leaderboardState.postValue(
                    result.fold(
                        onSuccess = { UiState.Success(it) },
                        onFailure = { UiState.Error(it.message ?: "Unable to load leaderboard") }
                    )
                )
            }
    }

    fun applyCompletedRun(run: CompletedRun) {
        // Run chi duoc cong vao challenge sau khi da finish xong
        repository.applyCompletedRunToChallenges(run) { result ->
            _actionState.postValue(
                result.fold(
                    onSuccess = { UiState.Success("progress_updated") },
                    onFailure = { UiState.Error(it.message ?: "Unable to update challenge progress") }
                )
            )
        }
    }

    fun clearActionState() {
        // Reset state sau khi show toast de tranh lap lai event cu
        _actionState.value = UiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        // Remove listener de tranh leak va trung update khi roi man
        publicChallengesRegistration?.remove()
        joinedChallengesRegistration?.remove()
        detailsRegistration?.remove()
        membershipRegistration?.remove()
        leaderboardRegistration?.remove()
    }
}
