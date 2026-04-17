package com.example.runna_runningtracker.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // Tra ve uid hien tai de app biet co session cu hay khong
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Ham nay chi check login roi hay chua ma khong doc profile
    fun hasActiveSession(): Boolean = auth.currentUser != null

    // Logout o day chi xoa session Firebase Auth
    fun signOut() = auth.signOut()

    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email.isNullOrBlank()) {
            onFailure("User session not found")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { error ->
                        onFailure(error.message ?: "Unknown update password error")
                    }
            }
            .addOnFailureListener { error ->
                onFailure(error.message ?: "Current password is incorrect")
            }
    }

    fun updateEmail(
        newEmail: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("User session not found")
            return
        }

        user.verifyBeforeUpdateEmail(newEmail)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error ->
                onFailure(error.message ?: "Unknown update email error")
            }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: (String?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Activity khong goi Firebase truc tiep ma di qua repo nay
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result -> onSuccess(result.user?.uid) }
            .addOnFailureListener { error -> onFailure(error.message ?: "Unknown login error") }
    }

    fun register(
        email: String,
        password: String,
        onSuccess: (String?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Register o day moi tao tai khoan Auth chua tao profile app
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result -> onSuccess(result.user?.uid) }
            .addOnFailureListener { error -> onFailure(error.message ?: "Unknown register error") }
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Firebase se gui mail reset toi dia chi ma user nhap
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onFailure(error.message ?: "Unknown reset error") }
    }
}
