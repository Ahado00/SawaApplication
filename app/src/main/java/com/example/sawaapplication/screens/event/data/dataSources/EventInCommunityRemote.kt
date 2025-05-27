package com.example.sawaapplication.screens.event.data.dataSources

import android.net.Uri
import android.util.Log
import androidx.compose.ui.res.stringResource
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.event.domain.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class EventInCommunityRemote @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    // Helper to get reference to the event subcollection inside a community
    private fun eventCollectionRef(communityId: String) =
        firestore.collection("Community").document(communityId).collection("event")

    /**
     * Creates a new event in a community with image upload.
     */
    suspend fun createEventInCommunity(
        communityId: String,
        event: Event,
        imageUri: Uri
    ) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            Log.e("Firebase", "User is not authenticated")
            return
        }

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("eventImages/${user.uid}_${System.currentTimeMillis()}.jpg")

        try {
            // Upload image
            storageRef.putFile(imageUri).await()

            // Get URL
            val imageUrl = storageRef.downloadUrl.await()

            // Update event with image URL
            val updatedEvent = event.copy(imageUri = imageUrl.toString())

            // Create docRef and save to Firestore
            val docRef = firestore.collection("Community")
                .document(communityId)
                .collection("event")
                .document()

            docRef.set(updatedEvent.copy(id = docRef.id), SetOptions.merge()).await()

            Log.d("Firebase", "Event created with ID: ${docRef.id}")
        } catch (e: Exception) {
            Log.e("Firebase", "Error creating event: ${e.message}")
        }
    }


    /**
     * Fetch all events from a specific community.
     */
    suspend fun fetchEventsFromCommunity(communityId: String): Result<List<Event>> {
        return try {
            val snapshot = eventCollectionRef(communityId).get().await()

            val events = snapshot.documents.mapNotNull { document ->
                document.toObject(Event::class.java)?.copy(id = document.id, communityId = communityId)
            }

            Result.success(events)
        } catch (e: Exception) {
            Log.e("EventFetch", "Error fetching events: ${e.message}")
            Result.failure(e)
        }
    }



    /**
     * Adds the current user to the joinedUsers list of an event.
     */
    suspend fun joinEvent(communityId: String, eventId: String, userId: String): Result<Unit> {
        return try {
            eventCollectionRef(communityId)
                .document(eventId)
                .update("joinedUsers", FieldValue.arrayUnion(userId))
                .await()

            // for event reminder in notification
            val eventSnap   = eventCollectionRef(communityId).document(eventId).get().await()
            val eventTime   = eventSnap.getTimestamp("time")!!
            val eventTitle  = eventSnap.getString("title")!!
            recordEventJoin(userId, eventId, eventTitle, eventTime.toDate())

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Removes the current user from the joinedUsers list of an event.
     */
    suspend fun leaveEvent(communityId: String, eventId: String, userId: String): Result<Unit> {
        return try {
            eventCollectionRef(communityId)
                .document(eventId)
                .update("joinedUsers", FieldValue.arrayRemove(userId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes an event from the Firestore database.
     */
    suspend fun deleteEvent(communityId: String, eventId: String): Result<Unit> {
        return try {
            firestore.collection("Community")
                .document(communityId)
                .collection("event")
                .document(eventId)
                .delete()
                .await()
            Log.d("RemoteDelete", "Deleted $eventId from $communityId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RemoteDelete", "Failed to delete: ${e.message}")
            Result.failure(e)
        }
    }


    /**
     * Updates specific fields of an event document in Firestore.
     */
// Updates specific fields of an event document in Firestore.
    suspend fun updateEvent(
        communityId: String,
        eventId: String,
        updatedData: Map<String, Any>
    ): Result<Unit> {
        if (communityId.isBlank() || eventId.isBlank()) {
            Log.e("Firebase", "Invalid communityId or eventId. Cannot update event.")
            return Result.failure(IllegalArgumentException("communityId and eventId must not be blank"))
        }

        return try {
            firestore.collection("Community")
                .document(communityId)
                .collection("event")
                .document(eventId)
                .update(updatedData)
                .await()

            Log.d("Firebase", "Event updated: $eventId")
            Log.d("FirestoreUpdate", "Updating $eventId with $updatedData")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firebase", "Error updating event: ${e.message}", e)
            Result.failure(e)
        }
    }

    // EventInCommunityRemoteImpl.kt (implementation)
    suspend fun getEventById(communityId: String, eventId: String): Event {
        val snapshot = firestore.collection("Community")
            .document(communityId)
            .collection("event")
            .document(eventId)
            .get()
            .await()

        return snapshot.toObject(Event::class.java)!!.copy(id = snapshot.id)
    }
    // Event Reminder
    suspend fun recordEventJoin(
        userId: String,
        eventId: String,
        eventTitle: String,
        startTime: Date
    ) {
        val reminder = mapOf(
            "userId"    to userId,
            "type"      to "event_reminder",
            "eventId"   to eventId,
            "startTime" to startTime,
            "responded" to false,
            "message"   to "\"$eventTitle\"",
            "timestamp" to FieldValue.serverTimestamp()
        )
        firestore
            .collection("Notification")
            .document(eventId)
            .set(reminder, SetOptions.merge())
            .await()
    }
}