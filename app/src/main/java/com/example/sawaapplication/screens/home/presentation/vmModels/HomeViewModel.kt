package com.example.sawaapplication.screens.home.presentation.vmModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawaapplication.screens.event.domain.model.Event
import com.example.sawaapplication.screens.post.domain.model.Post
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _communityNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val communityNames: StateFlow<Map<String, String>> = _communityNames

    private val _userDetails = MutableStateFlow<Map<String, Pair<String, String>>>(emptyMap())
    val userDetails: StateFlow<Map<String, Pair<String, String>>> = _userDetails

    // Map Post object to its FireStore document ID
    private val _postDocumentIds = MutableStateFlow<Map<Post, String>>(emptyMap())
    val postDocumentIds: StateFlow<Map<Post, String>> = _postDocumentIds

    private val _joinedEvents = MutableStateFlow<List<Event>>(emptyList())
    val joinedEvents: StateFlow<List<Event>> = _joinedEvents

    private val _hasCancelEvents = MutableStateFlow(false)
    val hasCancelEvents: StateFlow<Boolean> = _hasCancelEvents


    private suspend fun getUserCommunityIds(userId: String): List<String> {
        return try {
            val snapshot = firestore.collection("Community").get().await()
            snapshot.documents
                .filter { doc ->
                    val members = doc.get("members") as? List<*> ?: emptyList<Any>()
                    userId in members
                }
                .map { it.id } // FireStore document ID is the communityId
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching user communities: ${e.message}")
            emptyList()
        }
    }

    fun fetchAllPosts() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val currentUserId = firebaseAuth.currentUser?.uid

                if (currentUserId == null) {
                    _error.value = "User not logged in"
                    _loading.value = false
                    return@launch
                }

                val userCommunityIds = getUserCommunityIds(currentUserId)

                if (userCommunityIds.isEmpty()) {
                    _posts.value = emptyList()
                    return@launch
                }

                val postsList = mutableListOf<Post>()
                val docIdMap = mutableMapOf<Post, String>()

                userCommunityIds.forEach { communityId ->
                    val postSnapshot = firestore
                        .collection("Community")
                        .document(communityId)
                        .collection("posts")
                        .get()
                        .await()

                    for (doc in postSnapshot.documents) {
                        val post = doc.toObject(Post::class.java)
                        if (post != null) {
                            postsList.add(post)
                            docIdMap[post] = doc.id
                        }
                    }
                }

                _posts.value = postsList
                _postDocumentIds.value = docIdMap

                val communityIds = postsList.map { it.communityId }.distinct()
                val userIds = postsList.map { it.userId }.distinct()

                fetchCommunityNames(communityIds)
                fetchUserDetails(userIds)

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }


    private suspend fun fetchCommunityNames(communityIds: List<String>) = coroutineScope {
        try {
            val namesList = communityIds.map { id ->
                async {
                    try {
                        val snapshot = firestore.collection("Community").document(id).get().await()
                        if (snapshot.exists()) {
                            val name = snapshot.getString("name") ?: "Unknown"
                            id to name
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("Firebase", "Error fetching community $id: ${e.message}")
                        null
                    }
                }
            }.awaitAll()

            val namesMap = namesList.filterNotNull().toMap()
            _communityNames.value = namesMap

        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching community names: ${e.message}")
        }
    }

    private suspend fun fetchUserDetails(userIds: List<String>) = coroutineScope {
        try {
            val detailsList = userIds.map { id ->
                async {
                    try {
                        val snapshot = firestore.collection("User").document(id).get().await()
                        if (snapshot.exists()) {
                            val name = snapshot.getString("name") ?: "Unknown"
                            val image = snapshot.getString("image") ?: ""
                            id to (name to image)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("Firebase", "Error fetching user $id: ${e.message}")
                        null
                    }
                }
            }.awaitAll()

            val detailsMap = detailsList.filterNotNull().toMap()
            _userDetails.value = detailsMap

        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching user details: ${e.message}")
        }
    }

    // Like functionality: Increase likes in FireStore
    fun likePost(post: Post) {
        viewModelScope.launch {
            try {
                val docId = postDocumentIds.value[post]
                if (docId == null) {
                    Log.e("HomeViewModel", "Document ID not found for post")
                    return@launch
                }

                val postRef = firestore
                    .collection("Community")
                    .document(post.communityId)
                    .collection("posts")
                    .document(docId)

                val snapshot = postRef.get().await()
                val currentLikes = snapshot.getLong("likes") ?: 0
                val newLikes = if (currentLikes == 1L) 0 else 1

                firestore.runTransaction { transaction ->
                    transaction.update(postRef, "likes", newLikes)
                }.await()

                val updatedPost = post.copy(likes = newLikes.toInt())
                _posts.value = _posts.value.map {
                    if (it == post) updatedPost else it
                }

                // Also update the post ID mapping to match the new object
                _postDocumentIds.value = _postDocumentIds.value.toMutableMap().apply {
                    remove(post)
                    put(updatedPost, docId)
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to like post: ${e.message}")
            }
        }
    }
    fun fetchPostsByUser(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val userCommunityIds = getUserCommunityIds(userId)

                if (userCommunityIds.isEmpty()) {
                    _posts.value = emptyList()
                    return@launch
                }

                val postsList = mutableListOf<Post>()
                val docIdMap = mutableMapOf<Post, String>()

                userCommunityIds.forEach { communityId ->
                    val postSnapshot = firestore
                        .collection("Community")
                        .document(communityId)
                        .collection("posts")
                        .whereEqualTo("userId", userId) // filter by the passed userId
                        .get()
                        .await()

                    for (doc in postSnapshot.documents) {
                        val post = doc.toObject(Post::class.java)
                        if (post != null) {
                            postsList.add(post)
                            docIdMap[post] = doc.id
                        }
                    }
                }

                _posts.value = postsList
                _postDocumentIds.value = docIdMap

                val communityIds = postsList.map { it.communityId }.distinct()
                val userIds = postsList.map { it.userId }.distinct()

                fetchCommunityNames(communityIds)
                fetchUserDetails(userIds)

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    // fetch joined events

    fun fetchJoinedEvents() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch
                val communityIds = getUserCommunityIds(userId)

                val joinedEventsList = mutableListOf<Event>()

                for (communityId in communityIds) {
                    val eventsSnapshot = firestore.collection("Community")
                        .document(communityId)
                        .collection("event")
                        .get()
                        .await()

                    for (doc in eventsSnapshot.documents) {
                        val event = doc.toObject(Event::class.java)
                        if (event != null && userId in event.joinedUsers) {
                            joinedEventsList.add(event.copy(id = doc.id, communityId = communityId))
                        }
                    }
                }

                _joinedEvents.value = joinedEventsList

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching joined events: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun resetCancelButton() {
        _hasCancelEvents.value = false
    }

}