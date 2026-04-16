package com.example.runna_runningtracker.data.repository

import com.example.runna_runningtracker.data.model.Challenge
import com.example.runna_runningtracker.data.model.ChallengeMember
import com.example.runna_runningtracker.data.model.ChallengeMemberWithChallenge
import com.example.runna_runningtracker.data.model.CompletedRun
import com.example.runna_runningtracker.data.model.CreateChallengeInput
import com.example.runna_runningtracker.data.model.LeaderboardEntry
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ChallengeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // 4 collection chinh cua feature challenges
    private val challengesCollection = firestore.collection(Challenge.COLLECTION)
    private val membersCollection = firestore.collection(ChallengeMember.COLLECTION)
    private val runsCollection = firestore.collection(CompletedRun.COLLECTION)
    private val challengeRunAppliesCollection = firestore.collection("challenge_run_applies")

    fun observePopularChallenges(
        onResult: (Result<List<Challenge>>) -> Unit
    ): ListenerRegistration {
        // Popular chi lay challenge public active
        return challengesCollection
            .whereEqualTo(Challenge.FIELD_VISIBILITY, Challenge.VISIBILITY_PUBLIC)
            .whereEqualTo(Challenge.FIELD_STATUS, Challenge.STATUS_ACTIVE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                // Sort o client de giam nguy co can Firestore index khi demo
                val items = value?.documents.orEmpty()
                    .map(Challenge::fromDocument)
                    .sortedByDescending { it.participantCount }
                onResult(Result.success(items))
            }
    }

    fun observeEndingSoonChallenges(
        onResult: (Result<List<Challenge>>) -> Unit
    ): ListenerRegistration {
        // Ending soon chi giu challenge chua het han
        return challengesCollection
            .whereEqualTo(Challenge.FIELD_VISIBILITY, Challenge.VISIBILITY_PUBLIC)
            .whereEqualTo(Challenge.FIELD_STATUS, Challenge.STATUS_ACTIVE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val now = Timestamp.now()
                val items = value?.documents.orEmpty()
                    .map(Challenge::fromDocument)
                    .filter { challenge -> challenge.endAt?.toDate()?.after(now.toDate()) == true }
                    .sortedBy { it.endAt }
                onResult(Result.success(items))
            }
    }

    fun observeJoinedChallenges(
        userId: String,
        onResult: (Result<List<ChallengeMemberWithChallenge>>) -> Unit
    ): ListenerRegistration {
        // Doc membership truoc vi moi user co the join nhieu challenge
        return membersCollection
            .whereEqualTo(ChallengeMember.FIELD_USER_ID, userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val memberDocs = value?.documents.orEmpty()
                if (memberDocs.isEmpty()) {
                    onResult(Result.success(emptyList()))
                    return@addSnapshotListener
                }

                val members = memberDocs.map(ChallengeMember::fromDocument)
                val results = mutableListOf<ChallengeMemberWithChallenge>()
                var remaining = members.size
                var delivered = false

                members.forEach { member ->
                    // Moi membership can doc them challenge de co title date goal
                    challengesCollection.document(member.challengeId).get()
                        .addOnSuccessListener { challengeDoc ->
                            if (challengeDoc.exists()) {
                                results.add(
                                    ChallengeMemberWithChallenge(
                                        member = member,
                                        challenge = Challenge.fromDocument(challengeDoc)
                                    )
                                )
                            }
                        }
                        .addOnFailureListener { ex ->
                            if (!delivered) {
                                delivered = true
                                onResult(Result.failure(ex))
                            }
                        }
                        .addOnCompleteListener {
                            remaining -= 1
                            if (!delivered && remaining == 0) {
                                val ordered = results.sortedByDescending { it.member.joinedAt }
                                onResult(Result.success(ordered))
                            }
                        }
                }
            }
    }

    fun observeChallengeDetails(
        challengeId: String,
        onResult: (Result<Challenge>) -> Unit
    ): ListenerRegistration {
        // Detail man chi can nghe 1 challenge doc theo id
        return challengesCollection.document(challengeId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }
                if (value == null || !value.exists()) {
                    onResult(Result.failure(IllegalStateException("Challenge not found")))
                    return@addSnapshotListener
                }
                onResult(Result.success(Challenge.fromDocument(value)))
            }
    }

    fun observeMembership(
        challengeId: String,
        userId: String,
        onResult: (Result<ChallengeMember?>) -> Unit
    ): ListenerRegistration {
        // 1 user trong 1 challenge se co 1 doc duy nhat theo challengeId userId
        val docId = ChallengeMember.documentId(challengeId, userId)
        return membersCollection.document(docId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }
                if (value == null || !value.exists()) {
                    onResult(Result.success(null))
                    return@addSnapshotListener
                }
                onResult(Result.success(ChallengeMember.fromDocument(value)))
            }
    }

    fun observeLeaderboard(
        challengeId: String,
        goalDistanceMeters: Long,
        onResult: (Result<List<LeaderboardEntry>>) -> Unit
    ): ListenerRegistration {
        // Leaderboard lay tu progress cua member thay vi doc lai tung run
        return membersCollection
            .whereEqualTo(ChallengeMember.FIELD_CHALLENGE_ID, challengeId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val entries = value?.documents.orEmpty()
                    .map(ChallengeMember::fromDocument)
                    .sortedByDescending { it.progressDistanceMeters }
                    .take(10)
                    .mapIndexed { index, member ->
                        val percent = if (goalDistanceMeters > 0) {
                            (member.progressDistanceMeters.toDouble() / goalDistanceMeters.toDouble()) * 100.0
                        } else {
                            0.0
                        }
                        LeaderboardEntry(
                            rank = index + 1,
                            userId = member.userId,
                            displayName = member.displayNameSnapshot.ifBlank { "Runner" },
                            progressDistanceMeters = member.progressDistanceMeters,
                            progressPercent = percent.coerceAtMost(100.0)
                        )
                    }
                onResult(Result.success(entries))
            }
    }

    fun createChallenge(
        input: CreateChallengeInput,
        onResult: (Result<String>) -> Unit
    ) {
        // Tao challenge moi va auto join creator trong cung 1 batch
        val now = Timestamp.now()
        val challengeRef = challengesCollection.document()
        val memberRef = membersCollection.document(
            ChallengeMember.documentId(challengeRef.id, input.createdBy)
        )

        val challenge = Challenge(
            id = challengeRef.id,
            title = input.title.trim(),
            description = input.description.trim(),
            goalDistanceMeters = input.goalDistanceMeters,
            startAt = input.startAt,
            endAt = input.endAt,
            createdBy = input.createdBy,
            createdByName = input.createdByName,
            visibility = Challenge.VISIBILITY_PUBLIC,
            status = Challenge.STATUS_ACTIVE,
            participantCount = 1,
            createdAt = now,
            updatedAt = now
        )

        val creatorMembership = ChallengeMember(
            challengeId = challengeRef.id,
            userId = input.createdBy,
            joinedAt = now,
            progressDistanceMeters = 0L,
            isCompleted = false,
            completedAt = null,
            lastRunAppliedAt = null,
            displayNameSnapshot = input.createdByName
        )

        firestore.runBatch { batch ->
            batch.set(challengeRef, challenge.toMap())
            batch.set(memberRef, creatorMembership.toMap())
        }.addOnSuccessListener {
            onResult(Result.success(challengeRef.id))
        }.addOnFailureListener { ex ->
            onResult(Result.failure(ex))
        }
    }

    fun joinChallenge(
        challengeId: String,
        userId: String,
        displayName: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        // Join dung transaction de vua check vua tang participant cho an toan
        val challengeRef = challengesCollection.document(challengeId)
        val memberRef = membersCollection.document(ChallengeMember.documentId(challengeId, userId))

        firestore.runTransaction { transaction ->
            val challengeDoc = transaction.get(challengeRef)
            if (!challengeDoc.exists()) {
                throw IllegalStateException("Challenge not found")
            }

            val challenge = Challenge.fromDocument(challengeDoc)
            val now = Timestamp.now()

            // Chi cho join challenge public active va chua qua han
            if (challenge.visibility != Challenge.VISIBILITY_PUBLIC) {
                throw IllegalStateException("Challenge is not public")
            }
            if (challenge.status != Challenge.STATUS_ACTIVE) {
                throw IllegalStateException("Challenge is not active")
            }
            if (challenge.endAt != null && !challenge.endAt.toDate().after(now.toDate())) {
                throw IllegalStateException("Challenge already ended")
            }

            val memberDoc = transaction.get(memberRef)
            if (memberDoc.exists()) {
                // Chan join trung vi 1 user chi duoc co 1 membership
                throw IllegalStateException("You already joined this challenge")
            }

            val membership = ChallengeMember(
                challengeId = challengeId,
                userId = userId,
                joinedAt = now,
                progressDistanceMeters = 0L,
                isCompleted = false,
                completedAt = null,
                lastRunAppliedAt = null,
                displayNameSnapshot = displayName
            )

            transaction.set(memberRef, membership.toMap())
            transaction.update(
                challengeRef,
                mapOf(
                    Challenge.FIELD_PARTICIPANT_COUNT to challenge.participantCount + 1,
                    Challenge.FIELD_UPDATED_AT to now
                )
            )
        }.addOnSuccessListener {
            onResult(Result.success(Unit))
        }.addOnFailureListener { ex ->
            onResult(Result.failure(ex))
        }
    }

    fun applyCompletedRunToChallenges(
        run: CompletedRun,
        onResult: (Result<Unit>) -> Unit
    ) {
        // Luu run truoc roi moi dem no vao cac challenge user dang tham gia
        runsCollection.document(run.runId)
            .set(run.toMap())
            .addOnSuccessListener {
                membersCollection
                    .whereEqualTo(ChallengeMember.FIELD_USER_ID, run.userId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        processRunAgainstMemberships(run, snapshot.documents, onResult)
                    }
                    .addOnFailureListener { ex ->
                        onResult(Result.failure(ex))
                    }
            }
            .addOnFailureListener { ex ->
                onResult(Result.failure(ex))
            }
    }

    private fun processRunAgainstMemberships(
        run: CompletedRun,
        memberDocs: List<DocumentSnapshot>,
        onResult: (Result<Unit>) -> Unit
    ) {
        // Khong co membership nao thi run nay khong can cong cho challenge
        if (memberDocs.isEmpty()) {
            onResult(Result.success(Unit))
            return
        }

        val pending = memberDocs.toMutableList()
        var delivered = false

        pending.forEach { memberDoc ->
            // Moi membership se duoc xu ly rieng de kiem tra run hop le hay khong
            val member = ChallengeMember.fromDocument(memberDoc)
            val challengeRef = challengesCollection.document(member.challengeId)
            val memberRef = membersCollection.document(memberDoc.id)
            val applyRef = challengeRunAppliesCollection.document(
                "${member.challengeId}_${member.userId}_${run.runId}"
            )

            firestore.runTransaction { transaction ->
                val appliedDoc = transaction.get(applyRef)
                if (appliedDoc.exists()) {
                    // Neu run nay da cong truoc do thi bo qua de tranh double count
                    return@runTransaction
                }

                val challengeDoc = transaction.get(challengeRef)
                if (!challengeDoc.exists()) {
                    return@runTransaction
                }

                val challenge = Challenge.fromDocument(challengeDoc)
                val runEndedAt = run.endedAt.toDate()
                val startsOk = challenge.startAt?.toDate()?.let { !runEndedAt.before(it) } ?: true
                val endsOk = challenge.endAt?.toDate()?.let { !runEndedAt.after(it) } ?: true
                // Chi cong run nam trong thoi gian challenge va challenge con active
                if (challenge.status != Challenge.STATUS_ACTIVE || !startsOk || !endsOk) {
                    return@runTransaction
                }

                val newProgress = member.progressDistanceMeters + run.distanceMeters
                val completed = newProgress >= challenge.goalDistanceMeters

                // Update progress xong thi dong dau run nay da duoc ap dung
                transaction.update(
                    memberRef,
                    mapOf(
                        ChallengeMember.FIELD_PROGRESS_DISTANCE_METERS to newProgress,
                        ChallengeMember.FIELD_IS_COMPLETED to completed,
                        ChallengeMember.FIELD_COMPLETED_AT to if (completed) run.endedAt else null,
                        ChallengeMember.FIELD_LAST_RUN_APPLIED_AT to run.endedAt
                    )
                )
                transaction.set(
                    applyRef,
                    mapOf(
                        "challengeId" to member.challengeId,
                        "userId" to member.userId,
                        "runId" to run.runId,
                        "appliedAt" to Timestamp.now()
                    )
                )
            }.addOnFailureListener { ex ->
                if (!delivered) {
                    delivered = true
                    onResult(Result.failure(ex))
                }
            }.addOnCompleteListener {
                pending.remove(memberDoc)
                if (!delivered && pending.isEmpty()) {
                    onResult(Result.success(Unit))
                }
            }
        }
    }
}
