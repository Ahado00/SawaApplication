package com.example.sawaapplication.screens.notification.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.sawaapplication.core.sharedPreferences.NotificationPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val notificationPreferences: NotificationPreferences
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> get() = _notifications

    private val _hasUnreadNotifications = MutableStateFlow(true)
    val hasUnreadNotifications: StateFlow<Boolean> get() = _hasUnreadNotifications

    init {
        checkUnreadStatus()
        fetchNotifications()
    }

    fun markNotificationsAsSeen() {
        val user = firebaseAuth.currentUser
        user?.let {
            FirebaseFirestore.getInstance()
                .collection("Notification")
                .whereEqualTo("userId", it.uid)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (doc in querySnapshot.documents) {
                        doc.reference.update("isRead", true)
                    }

                    notificationPreferences.markAsSeen()

                    _hasUnreadNotifications.value = false

                }
        }
    }


    fun checkUnreadStatus() {
        _hasUnreadNotifications.value = notificationPreferences.hasUnread()
    }

    fun setHasUnread() {
        notificationPreferences.setUnread()
        _hasUnreadNotifications.value = true
    }

    fun storeProfileUpdateNotification() {
        val user = firebaseAuth.currentUser
        user?.let {
//            val userRef = FirebaseFireStore.getInstance().collection("User").document(it.uid)

            // Create a notification message
            val notificationMessage = "Your profile has been updated!"

            // Store the notification
            val notificationData = mapOf(
                "message" to notificationMessage,
                "timestamp" to FieldValue.serverTimestamp(),
                "userId" to it.uid,
                "isRead" to false // unread flag
            )

            FirebaseFirestore.getInstance()
                .collection("Notification")
                .add(notificationData)
                .addOnSuccessListener {
                    Log.d("ProfileViewModel", "Notification saved successfully!")
                    setHasUnread()
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileViewModel", "Error saving notification: $e")
                }
        }
    }

    fun fetchNotifications() {
        val user = firebaseAuth.currentUser
        user?.let {
            FirebaseFirestore.getInstance()
                .collection("Notification")
                .whereEqualTo("userId", it.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("NotificationVM", "Error fetching notifications: $error")
                        return@addSnapshotListener
                    }

                    val notificationList = snapshot?.documents?.mapNotNull { doc ->
                        val message = doc.getString("message")
                        val timestamp = doc.getTimestamp("timestamp")?.toDate()
                        val userId = doc.getString("userId")
                        val isRead = doc.getBoolean("isRead") == false

                        if (message != null && timestamp != null && userId != null) {
                            Notification(doc.id, message, timestamp, userId, isRead)
                        } else null
                    } ?: emptyList()

                    _notifications.value = notificationList

                    // Update unread status based on FireStore documents
                    _hasUnreadNotifications.value = notificationList.any { !it.isRead }
                }
        }
    }

}

data class Notification(
    val id: String,
    val message: String,
    val timestamp: Date,
    val userId: String,
    val isRead: Boolean
)