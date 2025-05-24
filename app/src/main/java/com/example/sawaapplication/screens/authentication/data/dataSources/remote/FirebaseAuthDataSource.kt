package com.example.sawaapplication.screens.authentication.data.dataSources.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.onesignal.OneSignal
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun signUp(name: String, email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = firebaseAuth.currentUser
        user?.let {
            val playerId = OneSignal.User.pushSubscription.id
            val userData = mapOf(
                "uid" to it.uid,
                "email" to email,
                "name" to name,
                "createdAt" to FieldValue.serverTimestamp(),
                "aboutMe" to "",
                "image" to "",
                "updatedAt" to "",
                "oneSignalPlayerId" to playerId
            )
            FirebaseFirestore.getInstance()
                .collection("User")
                .document(it.uid)
                .set(userData)
                .await()
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = firebaseAuth.currentUser

        if (user != null) {
            updateOneSignalPlayerId()
        }

        return user != null
    }

    private suspend fun updateOneSignalPlayerId() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val playerId = OneSignal.User.pushSubscription.id

        if (!playerId.isNullOrBlank()) {
            FirebaseFirestore.getInstance()
                .collection("User")
                .document(user.uid)
                .update("oneSignalPlayerId", playerId)
                .await() // use suspend here for consistency
            Log.d("OneSignal", "Updated playerId in FireStore: $playerId")
        }
    }

    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    fun logOut() {
        firebaseAuth.signOut()
    }

    suspend fun updateUserInfo(
        newAboutMe: String
    ) {
        val user = firebaseAuth.currentUser
        user?.let {
            val userRef = FirebaseFirestore
                .getInstance()
                .collection("User")
                .document(it.uid)
            userRef.update(
                mapOf(
                    "aboutMe" to newAboutMe,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

    }

    suspend fun updateUserName(
        newName: String
    ) {
        val user = firebaseAuth.currentUser
        user?.let {
            val userRef = FirebaseFirestore
                .getInstance()
                .collection("User")
                .document(it.uid)
            userRef.update(
                mapOf(
                    "name" to newName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

    }

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
}
