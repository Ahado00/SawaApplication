package com.example.sawaapplication.screens.notification.data.dataSources

import android.util.Log
import com.example.sawaapplication.screens.notification.domain.model.Notification
import com.example.sawaapplication.screens.post.domain.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class NotificationRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    fun fetchNotifications(listener: (List<Notification>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("Notification")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RemoteDataSource", "Error: ${error.message}")
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    val message = doc.getString("message") ?: return@mapNotNull null
                    val timestamp =
                        doc.getTimestamp("timestamp")?.toDate() ?: return@mapNotNull null
                    val userId = doc.getString("userId") ?: return@mapNotNull null
                    val isRead = doc.getBoolean("isRead") ?: false
                    Notification(doc.id, message, timestamp, userId, isRead)
                } ?: emptyList()

                listener(notifications)
            }
    }

    fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("Notification")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { it.reference.update("isRead", true) }
            }
    }

    // Sends reminders to community members about upcoming events
    fun remindCommunityMembersAboutUpcomingEvent() {
        val currentUser = auth.currentUser ?: return // Ensure user is logged in
        val currentTime = System.currentTimeMillis()

        firestore.collection("Community")
            .get()
            .addOnSuccessListener { communitySnapshot ->
                communitySnapshot.documents.forEach { communityDoc ->
                    val communityId = communityDoc.id
                    val communityName = communityDoc.getString("name") ?: "Unknown"
                    val members = communityDoc.get("members") as? List<String> ?: emptyList()

                    // Fetch all events for this community
                    firestore.collection("Community").document(communityId)
                        .collection("event")
                        .get()
                        .addOnSuccessListener { eventsSnapshot ->
                            eventsSnapshot.documents.forEach { eventDoc ->
                                val eventTitle = eventDoc.getString("title") ?: "No Title"
                                val eventTimestamp = eventDoc.getTimestamp("time")?.toDate()?.time
                                val creatorId = eventDoc.getString("creatorId")

                                if (eventTimestamp == null) {
                                    Log.d(
                                        "NotifDebug",
                                        "Event '$eventTitle' has no valid time, skipping"
                                    )
                                    return@forEach
                                }

                                val timeDiff = eventTimestamp - currentTime

                                // Check if the event is within the next 24 hours
                                if (timeDiff in 0..86400000L) {
                                    val message =
                                        "Reminder: The event '$eventTitle' in '$communityName' has one day left to start!"

                                    firestore.runTransaction { transaction ->
                                        val snapshot = transaction.get(eventDoc.reference)
                                        val reminderSent =
                                            snapshot.getBoolean("reminderSent") ?: false

                                        if (reminderSent) {
                                            Log.d(
                                                "NotifDebug",
                                                "Reminder already sent for '$eventTitle', skipping."
                                            )
                                            return@runTransaction null
                                        }

                                        Log.d(
                                            "NotifDebug",
                                            "Sending notifications for event '$eventTitle'"
                                        )

                                        // Send notifications to all members except the creator and the current user
                                        members.filter { it != creatorId }.forEach { memberId ->
                                            sendNotificationToUser(memberId, message)
                                            sendPushNotificationToUser(memberId, message)
                                        }

                                        // Mark reminder as sent in transaction
                                        transaction.update(eventDoc.reference, "reminderSent", true)
                                        null
                                    }.addOnFailureListener { e ->
                                        Log.e(
                                            "NotifDebug",
                                            "Transaction failed for '$eventTitle': ${e.message}"
                                        )
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "NotifDebug",
                                "Failed to get events for community $communityId: ${e.message}"
                            )
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("NotifDebug", "Failed to get communities: ${e.message}")
            }
    }

    // Stores in-app notification in FireStore under the "Notification" collection
    fun sendNotificationToUser(userId: String, message: String) {
        val data = mapOf(
            "message" to message,
            "timestamp" to FieldValue.serverTimestamp(),
            "userId" to userId,
            "isRead" to false
        )
        firestore.collection("Notification").add(data)
    }

    // Sends a real-time push notification via OneSignal to a user
    fun sendPushNotificationToUser(userId: String, message: String) {
        Log.d("NotifDebug", "Preparing to send push notification to $userId with message: $message")

        firestore.collection("User").document(userId).get()
            .addOnSuccessListener { doc ->
                val playerId = doc.getString("oneSignalPlayerId") // OneSignal device ID

                if (playerId.isNullOrEmpty()) {
                    Log.e("NotifDebug", "No playerId found for user $userId")
                    return@addOnSuccessListener
                }

                Log.d("NotifDebug", "Found playerId $playerId for user $userId")

                // Build OneSignal push notification payload
                val url = "https://onesignal.com/api/v1/notifications"
                val json = JSONObject().apply {
                    put("app_id", "cf902765-3bc6-4eab-84cb-307d5db55cd1")
                    put("include_player_ids", JSONArray().put(playerId))
                    put("contents", JSONObject().put("en", message))
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaType())

                // Create HTTP request using OkHttp
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader(
                        "Authorization",
                        "Bearer os_v2_app_z6icozj3yzhkxbglgb6v3nk42fcgx4eiuzxubcvzkrblrsvt2ahoweclqf3dghxpkzejgpr6ytv2hev5uzqufeabyejj4ubfntqztzi"
                    )
                    .build()

                // Send the request asynchronously
                OkHttpClient().newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        Log.e("NotifDebug", "Failed to send push: ${e.message}")
                    }

                    override fun onResponse(call: okhttp3.Call, response: Response) {
                        response.use {
                            Log.d("NotifDebug", "Push response: ${it.body?.string()}")
                        }
                    }
                })
            }
            .addOnFailureListener { e ->
                Log.e("NotifDebug", "Failed to fetch user document: ${e.message}")
            }
    }

    fun sendCommunityEventNotification(communityId: String, eventName: String) {
        val currentUser = auth.currentUser ?: return
        val db = firestore

        db.collection("Community").document(communityId)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Unknown"
                val members =
                    doc.get("members")?.let { it as? List<*> }?.mapNotNull { it as? String }
                        ?: return@addOnSuccessListener

                val msg = "A new event \"$eventName\" was created in \"$name\" community."
                members.filter { it != currentUser.uid }.forEach { memberId ->
                    sendNotificationToUser(memberId, msg)
                }
            }
    }

    fun sendLikeNotification(post: Post) {
        val likerId = post.likedBy.lastOrNull() ?: return
        val recipientId = post.userId

        firestore.collection("User").document(likerId).get().addOnSuccessListener { userDoc ->
            val likerName = userDoc.getString("name") ?: "Someone"

            firestore.collection("Community").document(post.communityId).get()
                .addOnSuccessListener { communityDoc ->
                    val communityName = communityDoc.getString("name") ?: "Unknown"
                    val message = "$likerName liked your post in '$communityName' community."
                    sendNotificationToUser(recipientId, message)
                }
        }
    }

    fun observeUnreadNotifications(listener: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("Notification")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, _ ->
                listener(snapshot?.isEmpty == false)
            }
    }

}
