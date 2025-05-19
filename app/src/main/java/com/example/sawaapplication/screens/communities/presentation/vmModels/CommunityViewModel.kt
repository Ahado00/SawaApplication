package com.example.sawaapplication.screens.communities.presentation.vmModels

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.sawaapplication.screens.communities.domain.useCases.CreateCommunityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.sawaapplication.screens.communities.domain.model.Community
import com.example.sawaapplication.screens.communities.domain.useCases.GetUserCreatedCommunitiesUseCase
import kotlinx.coroutines.Job
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sawaapplication.core.permissions.PermissionHandler
import com.example.sawaapplication.screens.communities.domain.useCases.GetCommunityByIdUseCase
import com.example.sawaapplication.screens.post.domain.model.Post
import com.example.sawaapplication.screens.post.domain.model.PostUiModel
import com.example.sawaapplication.screens.post.domain.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val createCommunityUseCase: CreateCommunityUseCase,
    private val getUserCreatedCommunitiesUseCase: GetUserCreatedCommunitiesUseCase,
    private val getCommunityByIdUseCase: GetCommunityByIdUseCase,
    private val postRepository: PostRepository,
    private val permissionHandler: PermissionHandler,
    firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private var job: Job? = null

    // UI-bound form fields
    var imageUri by mutableStateOf<Uri?>(null)
    var name by mutableStateOf("")
    var description by mutableStateOf("")

    // Current user ID from Firebase
    val currentUserId = firebaseAuth.currentUser?.uid ?: ""

    // Holds the details of a single community for the detail screen
    private val _communityDetail = MutableStateFlow<Community?>(null)
    val communityDetail: StateFlow<Community?> = _communityDetail

    // Tracks whether the community creation was successful
    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    // Stores the list of communities created by the current user
    private val _createdCommunities = MutableStateFlow<List<Community>>(emptyList())
    val createdCommunities: StateFlow<List<Community>> = _createdCommunities

    // Loading and error states for UI feedback
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    //Fetching the posts in side community
    private val _communityPosts = MutableStateFlow<List<PostUiModel>>(emptyList())
    val communityPosts: StateFlow<List<PostUiModel>> = _communityPosts






    // Creates a new community, uploads its image, and updates state
    fun createCommunity(
        name: String,
        description: String,
        imageUri: Uri?,
        currentUserId: String
    ) {
        if (_loading.value) return
        job = viewModelScope.launch {
            _loading.value = true
            try {
                val result = createCommunityUseCase(name, description, imageUri, currentUserId)
                result.onSuccess {
                    fetchCreatedCommunities(currentUserId) // Refresh list after successful creation
                    _success.value = true
                }.onFailure {
                    _error.value = "Failed to create community: ${it.message}"
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun shouldRequestLocation() = permissionHandler.shouldRequestLocationPermission()
    fun markLocationPermissionRequested() = permissionHandler.markLocationPermissionRequested()

    fun shouldRequestPhoto() = permissionHandler.shouldRequestPhotoPermission()
    fun markPhotoPermissionRequested() = permissionHandler.markPhotoPermissionRequested()


    // Fetches communities where the current user is a member
    fun fetchCreatedCommunities(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val result = getUserCreatedCommunitiesUseCase(userId)
                result.onSuccess { communities ->
                    _createdCommunities.value = communities
                    _error.value = null
                }.onFailure { exception ->
                    _error.value = "Failed to fetch communities: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Fetches the full detail of a single community by its ID
    fun fetchCommunityDetail(communityId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = getCommunityByIdUseCase(communityId)
            result.onSuccess { community ->
                Log.d("DEBUG", "Fetching community with ID: $communityId")
                _communityDetail.value = community
            }.onFailure { exception ->
                _error.value = "Failed to load community detail: ${exception.message}"
            }
            _loading.value = false
        }
    }
    fun fetchPostsForCommunity(communityId: String) {
        viewModelScope.launch {
            val result = postRepository.getPostsForCommunity(communityId)
            result.onSuccess { posts ->
                val mapped = posts.map { post ->
                    PostUiModel(
                        id = post.id,
                        username = post.username,         // Use userId as username placeholder
                        userAvatarUrl = post.userAvatarUrl,             // TODO: Replace with real profile image
                        postImageUrl = post.postImageUrl,
                        content = post.content
                    )
                }
                _communityPosts.value = mapped
            }.onFailure {
                Log.e("CommunityViewModel", "Failed to fetch posts: ${it.message}")
            }
        }
    }


    // Cleans up any running jobs when the ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}




