package com.example.runna_runningtracker

import com.example.runna_runningtracker.data.model.CompletedRun
import com.example.runna_runningtracker.data.repository.ChallengeRepository

class ChallengeProgressService(
    private val repository: ChallengeRepository = ChallengeRepository()
) {
    fun applyCompletedRunToChallenges(
        run: CompletedRun,
        onResult: (Result<Unit>) -> Unit
    ) {
        // Service nay la cau noi giua Summary va logic cong progress challenge
        repository.applyCompletedRunToChallenges(run, onResult)
    }
}
