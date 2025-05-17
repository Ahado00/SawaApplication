package com.example.sawaapplication.screens.notification.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.sawaapplication.core.sharedPreferences.NotificationPreferences
import com.example.sawaapplication.screens.notification.domain.model.Notification
import com.example.sawaapplication.screens.post.domain.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        observeUnreadNotifications()
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

    // send notification for the user when they updated the profile
    fun storeProfileUpdateNotification() {
        val user = firebaseAuth.currentUser
        user?.let {
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
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileViewModel", "Error saving notification: $e")
                }
        }
    }

    // send notification for the creator of the event
    fun storeEventCreatedNotification(eventName: String) {
        val user = firebaseAuth.currentUser
        user?.let {
            val notificationMessage = "You created the event: $eventName"

            val notificationData = mapOf(
                "message" to notificationMessage,
                "timestamp" to FieldValue.serverTimestamp(),
                "userId" to it.uid,
                "isRead" to false
            )

            FirebaseFirestore.getInstance()
                .collection("Notification")
                .add(notificationData)
                .addOnSuccessListener {
                    Log.d("NotificationViewModel", "Event creation notification saved.")
                }
                .addOnFailureListener { e ->
                    Log.e("NotificationViewModel", "Failed to save event notification: $e")
                }
        }
    }

    // fetch the notification in the notification screen
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

    // send notification for the members of the community to notify them about the event
    fun notifyCommunityMembersOfNewEvent(communityId: String, eventName: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Get Community document
        db.collection("Community").document(communityId)
            .get()
            .addOnSuccessListener { document ->
                // Get the community name and members
                val communityName = document.getString("name") ?: "Unknown Community"
                val members =
                    document.get("members") as? List<String> ?: return@addOnSuccessListener

                // This is the message of the event
                val message =
                    "A new event \"$eventName\" was created in the \"$communityName\" community."

                // Iterate through members and store notifications
                members.forEach { memberId ->
                    if (memberId != currentUserId) { // Avoid notifying the creator
                        // Create the notification data as a map
                        val notificationData = mapOf(
                            "message" to message,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "userId" to memberId,
                            "isRead" to false // unread flag
                        )

                        // Store the notification data in FireStore
                        db.collection("Notification")
                            .add(notificationData)
                            .addOnSuccessListener {
                                Log.d("NotificationVM", "Notification sent to $memberId")
                            }
                            .addOnFailureListener { e ->
                                Log.e("NotificationVM", "Failed to send to $memberId: $e")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationVM", "Failed to get community members: $e")
            }
    }

    // send notification to the creator of the post to notify them that someone liked the post
    fun sendLikeNotification(post: Post) {
        val recipientUserId = post.userId // The creator of the post
        val likedByUserId =
            post.likedBy.lastOrNull() // Get the last user who liked the post (or you can change to handle more users)

        // Fetch the name of the user who liked the post
        likedByUserId?.let { userId ->
            FirebaseFirestore.getInstance()
                .collection("User") // Assuming you have a Users collection where you store user info
                .document(userId)
                .get()
                .addOnSuccessListener { userDocument ->
                    val likerName = userDocument.getString("name")
                        ?: "Someone" // Get the user's name (or fallback to "Someone")

                    // Fetch the community name
                    FirebaseFirestore.getInstance()
                        .collection("Community") // Assuming you have a Community collection where community data is stored
                        .document(post.communityId) // Use the communityId from the post
                        .get()
                        .addOnSuccessListener { communityDocument ->
                            val communityName = communityDocument.getString("name")
                                ?: "Unknown Community" // Get the community name

                            // Create the notification message
                            val message =
                                "$likerName liked your post in the '$communityName' community."

                            // Prepare the notification data
                            val notificationData = mapOf(
                                "message" to message,
                                "timestamp" to FieldValue.serverTimestamp(),
                                "userId" to recipientUserId,
                                "isRead" to false // unread flag
                            )

                            // Store the notification data in Firestore
                            FirebaseFirestore.getInstance()
                                .collection("Notification")
                                .add(notificationData)
                                .addOnSuccessListener {
                                    Log.d(
                                        "NotificationViewModel",
                                        "Like notification sent to $recipientUserId"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e(
                                        "NotificationViewModel",
                                        "Failed to send like notification: $e"
                                    )
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("NotificationViewModel", "Failed to fetch community info: $e")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("NotificationViewModel", "Failed to fetch liker info: $e")
                }
        }
    }

    private fun observeUnreadNotifications() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Notification")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, _ ->
                _hasUnreadNotifications.value = snapshot?.isEmpty == false
            }
    }

}