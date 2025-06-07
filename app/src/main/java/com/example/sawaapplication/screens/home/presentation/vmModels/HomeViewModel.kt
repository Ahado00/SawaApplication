package com.example.sawaapplication.screens.home.presentation.vmModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawaapplication.screens.event.domain.model.Event
import com.example.sawaapplication.screens.event.domain.useCases.GetAllEventInCommunity
import com.example.sawaapplication.screens.home.domain.model.EventFilterType
import com.example.sawaapplication.screens.home.domain.useCases.AddCommentUseCase
import com.example.sawaapplication.screens.home.domain.useCases.DeletePostUseCase
import com.example.sawaapplication.screens.home.domain.useCases.FetchAllPostsUseCase
import com.example.sawaapplication.screens.home.domain.useCases.FetchCommentsUseCase
import com.example.sawaapplication.screens.home.domain.useCases.FetchCommunityNamesUseCase
import com.example.sawaapplication.screens.home.domain.useCases.FetchJoinedEventsUseCase
import com.example.sawaapplication.screens.home.domain.useCases.FetchLikedPostsByUserUseCase
import com.example.sawaapplication.screens.home.domain.useCases.FetchPostsByUserUseCase
import com.example.sawaapplication.screens.home.domain.useCases.FetchUserDetailsUseCase
import com.example.sawaapplication.screens.home.domain.useCases.LikePostUseCase
import com.example.sawaapplication.screens.post.domain.model.Comment
import com.example.sawaapplication.screens.post.domain.model.Post
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val getAllEventInCommunity: GetAllEventInCommunity,
    private val fetchAllPostsUseCase: FetchAllPostsUseCase,
    private val fetchCommunityNamesUseCase: FetchCommunityNamesUseCase,
    private val fetchUserDetailsUseCase: FetchUserDetailsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val fetchJoinedEventsUseCase: FetchJoinedEventsUseCase,
    private val fetchPostsByUserUseCase: FetchPostsByUserUseCase,
    private val fetchLikedPostsByUserUseCase: FetchLikedPostsByUserUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val fetchCommentsUseCase: FetchCommentsUseCase
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _deletePostResult = MutableStateFlow<Result<Unit>?>(null)
    val deletePostResult: StateFlow<Result<Unit>?> = _deletePostResult

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _communityNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val communityNames: StateFlow<Map<String, String>> = _communityNames

    private val _userDetails = MutableStateFlow<Map<String, Pair<String, String>>>(emptyMap())
    val userDetails: StateFlow<Map<String, Pair<String, String>>> = _userDetails

    private val _postDocumentIds = MutableStateFlow<Map<Post, String>>(emptyMap())
    val postDocumentIds: StateFlow<Map<Post, String>> = _postDocumentIds

    private val _joinedEvents = MutableStateFlow<List<Event>>(emptyList())
    val joinedEvents: StateFlow<List<Event>> = _joinedEvents

    private val _hasCancelEvents = MutableStateFlow(false)
    val hasCancelEvents: StateFlow<Boolean> = _hasCancelEvents

    private val _postLikedEvent = MutableStateFlow<String?>(null)
    val postLikedEvent: StateFlow<String?> = _postLikedEvent

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _postComments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val postComments: StateFlow<Map<String, List<Comment>>> = _postComments

    fun getEventById(eventId: String): Event? {
        return _events.value.find { it.id == eventId }
    }

    private fun loadCommunityNames(ids: List<String>) {
        viewModelScope.launch {
            val names = fetchCommunityNamesUseCase(ids)
            _communityNames.value = names
        }
    }

    private fun loadUserDetails(ids: List<String>) {
        viewModelScope.launch {
            val details = fetchUserDetailsUseCase(ids)
            _userDetails.value = details
        }
    }

    fun loadAllPosts() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val (postsList, docIdMap) = fetchAllPostsUseCase()

                val communityIds = postsList.map { it.communityId }.distinct()
                val userIds = postsList.map { it.userId }.distinct()

                val namesDeferred = async { fetchCommunityNamesUseCase(communityIds) }
                val detailsDeferred = async { fetchUserDetailsUseCase(userIds) }

                val names = namesDeferred.await()
                val details = detailsDeferred.await()

                _communityNames.value = names
                _userDetails.value = details

                _posts.value = postsList
                _postDocumentIds.value = docIdMap
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun likePost(post: Post) {
        viewModelScope.launch {
            val docId = _postDocumentIds.value[post]
            if (docId == null) {
                Log.e("HomeViewModel", "Document ID not found for post")
                return@launch
            }

            val (updatedPost, likedUserId) = likePostUseCase(post, docId)

            updatedPost?.let {
                _posts.value = _posts.value.map { existing ->
                    if (existing == post) it else existing
                }
                _postDocumentIds.value = _postDocumentIds.value.toMutableMap().apply {
                    remove(post)
                    put(it, docId)
                }
            }

            likedUserId?.let {
                _postLikedEvent.emit(it)
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            _deletePostResult.value = null

            val result = runCatching {
                val docId = _postDocumentIds.value[post]
                if (docId.isNullOrEmpty()) {
                    Log.e("HomeViewModel", "Document ID not found for post")
                    throw Exception("Document ID not found for post")
                }
                deletePostUseCase(post, docId)
                _posts.value = _posts.value.filter { it != post }
                _postDocumentIds.value = _postDocumentIds.value - post
            }
            _deletePostResult.value = result
        }
    }

    fun clearDeletePostResult() {
        _deletePostResult.value = null
    }

    fun fetchJoinedEvents() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch
                val eventsList = fetchJoinedEventsUseCase(userId)
                _joinedEvents.value = eventsList

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching joined events: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchPostsByUser(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {

                val (postsList, docIdMap) = fetchPostsByUserUseCase(userId)

                _posts.value = postsList
                _postDocumentIds.value = docIdMap

                val communityIds = postsList.map { it.communityId }.distinct()
                val userIds = postsList.map { it.userId }.distinct()

                loadCommunityNames(communityIds)
                loadUserDetails(userIds)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchLikedPostsByUser(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val (postsList, docIdMap) = fetchLikedPostsByUserUseCase(userId)

                _posts.value = postsList
                _postDocumentIds.value = docIdMap

                val communityIds = postsList.map { it.communityId }.distinct()
                val userIds = postsList.map { it.userId }.distinct()

                loadCommunityNames(communityIds)
                loadUserDetails(userIds)

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadEvents(communityId: String) {
        viewModelScope.launch {
            val result = getAllEventInCommunity(communityId)
            result.onSuccess { fetchedEvents ->
                _events.value = fetchedEvents
            }.onFailure {
                Log.e("HomeViewModel", "Failed to fetch events: ${it.message}")
            }
        }
    }

    private val _selectedFilter = MutableStateFlow<EventFilterType>(EventFilterType.DEFAULT)
    val selectedFilter: StateFlow<EventFilterType> = _selectedFilter

    fun setFilter(filter: EventFilterType) {
        _selectedFilter.value = filter
    }

    val filteredEvents: List<Event>
        get() {
            val now = System.currentTimeMillis()
            return when (selectedFilter.value) {
                EventFilterType.Finished ->
                    joinedEvents.value.filter { (it.time?.toDate()?.time ?: 0L) < now }

                EventFilterType.Still ->
                    joinedEvents.value.filter { (it.time?.toDate()?.time ?: Long.MAX_VALUE) > now }

                EventFilterType.DEFAULT ->
                    joinedEvents.value
            }.sortedBy { it.time?.toDate()?.time ?: Long.MAX_VALUE }
        }

    fun addComment(communityId: String, postId: String, comment: Comment) {
        viewModelScope.launch {
            try {
                addCommentUseCase(communityId, postId, comment)
            } catch (e: Exception) {
                _error.value = "Failed to add comment: ${e.message}"
            }
        }
    }

    fun fetchComments(communityId: String, postId: String) {
        viewModelScope.launch {
            val comments = fetchCommentsUseCase(communityId, postId)
            _postComments.update { old ->
                old + (postId to comments)
            }
        }
    }

}