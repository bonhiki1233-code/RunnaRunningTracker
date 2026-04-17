package com.example.runna_runningtracker.data.repository

import com.example.runna_runningtracker.data.model.User
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.firestore.SetOptions

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun createUserProfile(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Tao profile lan dau sau khi register va complete info
        firestore.collection(User.COLLECTION_USERS).document(user.uid)
            .set(user.toMap())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onFailure(error.message ?: "Unknown create profile error") }
    }

    fun updateUserProfile(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Merge de update tung field profile ma khong de mat field cu
        firestore.collection(User.COLLECTION_USERS).document(user.uid)
            .set(user.toMap(), SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onFailure(error.message ?: "Unknown update profile error") }
    }

    fun loadUserProfile(

        uid: String,
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Moi lan login hoac mo app lai thi se doc profile tu Firestore
        firestore.collection(User.COLLECTION_USERS).document(uid)
            .get()
            .addOnSuccessListener { document ->
                // Chua co profile thi tra ve user rong de flow tu xu ly tiep
                if (!document.exists()) {
                    onSuccess(User(uid = uid))
                    return@addOnSuccessListener
                }
                onSuccess(User.fromMap(uid, document.data))
            }
            .addOnFailureListener { error -> onFailure(error.message ?: "Unknown load profile error") }
    }

    fun getUser(
        uid: String,
        callback: (User?) -> Unit
    ) {
        // Ham nay doc nhanh 1 user khi chi can callback don gian
        firestore.collection(User.COLLECTION_USERS)
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document != null && document.data != null) {
                    val user = User.fromMap(uid, document.data)
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun updateUserEmail(
        uid: String,
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection(User.COLLECTION_USERS).document(uid)
            .set(mapOf(User.KEY_EMAIL to email), SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error ->
                onFailure(error.message ?: "Unknown update email error")
            }
    }
}
