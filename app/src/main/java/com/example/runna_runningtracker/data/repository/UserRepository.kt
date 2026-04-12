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
        // Tao profile lan dau sau khi user register xong va dien xong thong tin ca nhan.
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
        // Merge de cap nhat tung field profile ma khong ghi de toan bo document.
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
        // Moi lan login/open app, Activity se goi ham nay de dong bo profile tu Firestore.
        firestore.collection(User.COLLECTION_USERS).document(uid)
            .get()
            .addOnSuccessListener { document ->
                // Neu user moi chua co profile thi tra ve User rong de he thong quyet dinh
                if (!document.exists()) {
                    onSuccess(User(uid = uid))
                    return@addOnSuccessListener
                }
                onSuccess(User.fromMap(uid, document.data))
            }
            .addOnFailureListener { error -> onFailure(error.message ?: "Unknown load profile error") }
    }fun getUser(
        uid: String,
        callback: (User?) -> Unit
    ) {
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
}
