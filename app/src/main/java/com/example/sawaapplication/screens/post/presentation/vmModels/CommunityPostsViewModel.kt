package com.example.sawaapplication.screens.post.presentation.vmModels

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawaapplication.core.permissions.PermissionHandler
import com.example.sawaapplication.screens.post.domain.model.Post
import com.example.sawaapplication.screens.post.domain.model.PostUiModel
import com.example.sawaapplication.screens.post.domain.useCases.CreatePostUseCase
import com.example.sawaapplication.screens.post.domain.useCases.GetAllPostsUseCase
import com.example.sawaapplication.screens.post.domain.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CommunityPostsViewModel @Inject constructor(
    private val createPostUseCase: CreatePostUseCase,
    private val permissionHandler: PermissionHandler,
    private val getAllPostsUseCase: GetAllPostsUseCase,
    private val postRepository: PostRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // UI State for post creation
    private var _communityId by mutableStateOf<String?>("")
    val currentCommunityId: String? get() = _communityId

    private var _content by mutableStateOf("")
    val currentContent: String get() = _content

    private var _imageUri by mutableStateOf<Uri?>(null)
    val currentImageUri: Uri? get() = _imageUri

    // Loading state
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // Posts state - FIXED: Use proper asStateFlow() approach
    private val _communityPosts = MutableStateFlow<List<PostUiModel>>(emptyList())
    val communityPosts: StateFlow<List<PostUiModel>> = _communityPosts.asStateFlow()

    // FIXED: Added uid property from Dev branch
    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Success state for post operations
    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    // Post creation loading state (separate from posts loading)
    private val _creatingPost = MutableStateFlow(false)
    val creatingPost: StateFlow<Boolean> = _creatingPost.asStateFlow()

    // Real-time listener
    private var postsListener: ListenerRegistration? = null

    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

    companion object {
        private const val TAG = "CommunityPostsViewModel"
        private const val DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy"
    }

    // Post Creation Methods
    fun updateContent(newContent: String) {
        _content = newContent
        clearError()
    }

    fun updateImageUri(newImageUri: Uri?) {
        _imageUri = newImageUri
        clearError()
    }

    fun updateCommunityId(newCommunityId: String?) {
        _communityId = newCommunityId
    }

    suspend fun createPost(communityId: String): Boolean {
        return try {
            validatePostCreation(communityId)
            _creatingPost.value = true
            _error.value = null
            _success.value = false

            val post = createPostObject(communityId)
            createPostUseCase(communityId, post, _imageUri)

            resetForm()
            fetchPostsForCommunity(communityId)
            _success.value = true
            Log.d(TAG, "Post created successfully")
            true
        } catch (e: Exception) {
            val errorMessage = "Failed to create post: ${e.message}"
            Log.e(TAG, errorMessage, e)
            _error.value = errorMessage
            false
        } finally {
            _creatingPost.value = false
        }
    }

    private fun validatePostCreation(communityId: String) {
        if (communityId.isBlank()) {
            throw IllegalArgumentException("Community ID cannot be blank")
        }
        if (_content.trim().isBlank() && _imageUri == null) {
            throw IllegalArgumentException("Post must have content or image")
        }
    }

    private fun createPostObject(communityId: String): Post {
        return Post(
            content = _content.trim(),
            createdAt = Date().toString(),
            imageUri = "", // Will be updated in use case
            userId = currentUserId,
            communityId = communityId
        )
    }

    private fun resetForm() {
        _content = ""
        _imageUri = null
    }

    // Post Loading Methods - Multiple approaches for flexibility
    fun loadPosts(communityId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                Log.d(TAG, "Loading posts for community: $communityId")

                val posts = fetchPostsFromFirestore(communityId)
                val enrichedPosts = enrichPostsWithUserInfo(posts) // ✅ NEW: Enrich with user info
                val sortedPosts = sortPostsByDate(enrichedPosts)

                _communityPosts.value = sortedPosts
                Log.d(TAG, "Successfully loaded ${sortedPosts.size} posts with user info")

            } catch (e: Exception) {
                val errorMessage = "Failed to load posts: ${e.message}"
                Log.e(TAG, errorMessage, e)
                _error.value = errorMessage
                _communityPosts.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    // Alternative method using PostRepository (for compatibility) - RECOMMENDED
    fun fetchPostsForCommunity(communityId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                Log.d(TAG, "Fetching posts for community via repository: $communityId")

                val result = postRepository.getPostsForCommunity(communityId)
                result.onSuccess { posts ->
                    val sortedPosts = posts
                        .sortedByDescending {
                            try {
                                SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).parse(it.createdAt)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        .map { post ->
                            PostUiModel(
                                id = post.id,
                                username = post.username,
                                userAvatarUrl = post.userAvatarUrl,
                                postImageUrl = post.postImageUrl,
                                content = post.content,
                                likes = post.likes,
                                likedBy = post.likedBy,
                                userId = post.userId,
                                communityId = post.communityId,
                                createdAt = post.createdAt
                            )
                        }
                    _communityPosts.value = sortedPosts
                    Log.d(TAG, "Successfully fetched ${sortedPosts.size} posts via repository")
                }.onFailure { throwable ->
                    val errorMessage = "Failed to fetch posts: ${throwable.message}"
                    Log.e(TAG, errorMessage, throwable)
                    _error.value = errorMessage
                }
            } catch (e: Exception) {
                val errorMessage = "Unexpected error fetching posts: ${e.message}"
                Log.e(TAG, errorMessage, e)
                _error.value = errorMessage
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun fetchPostsFromFirestore(communityId: String): List<PostUiModel> {
        val postSnapshot = firestore
            .collection("Community")
            .document(communityId)
            .collection("posts")
            .get()
            .await()

        Log.d(TAG, "Firestore returned ${postSnapshot.documents.size} documents")

        return postSnapshot.documents.mapNotNull { doc ->
            doc.data?.let { data ->
                mapFirestoreDataToPostUiModel(doc.id, data, communityId)
            }
        }
    }

    private fun mapFirestoreDataToPostUiModel(
        documentId: String,
        data: Map<String, Any>,
        communityId: String
    ): PostUiModel {
        return PostUiModel(
            id = documentId,
            username = data["username"] as? String ?: "Unknown User", // Will be enriched later
            userAvatarUrl = data["userAvatarUrl"] as? String ?: "", // Will be enriched later
            postImageUrl = data["imageUri"] as? String ?: "",
            content = data["content"] as? String ?: "",
            likes = (data["likes"] as? Long)?.toInt() ?: 0,
            likedBy = data["likedBy"] as? List<String> ?: emptyList(),
            userId = data["userId"] as? String ?: "",
            communityId = data["communityId"] as? String ?: communityId,
            createdAt = data["createdAt"] as? String ?: ""
        )
    }


    private suspend fun enrichPostsWithUserInfo(posts: List<PostUiModel>): List<PostUiModel> {
        return posts.map { post ->
            if (post.username != "Unknown User" && post.username.isNotEmpty()) {
                // Already has user info
                post
            } else {
                // Fetch user info from User collection (same as PostsInCommunityRemote)
                try {
                    val userDoc = firestore.collection("User")
                        .document(post.userId)
                        .get()
                        .await()

                    if (userDoc.exists()) {
                        val userData = userDoc.data
                        post.copy(
                            username = userData?.get("name") as? String
                                ?: userData?.get("username") as? String
                                ?: userData?.get("displayName") as? String
                                ?: "User ${post.userId.take(6)}",
                            userAvatarUrl = userData?.get("image") as? String
                                ?: userData?.get("avatarUrl") as? String
                                ?: userData?.get("profileImageUrl") as? String
                                ?: ""
                        )
                    } else {
                        // User document doesn't exist, use default
                        post.copy(
                            username = "User ${post.userId.take(6)}",
                            userAvatarUrl = ""
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch user info for ${post.userId}: ${e.message}")
                    post.copy(
                        username = "User ${post.userId.take(6)}",
                        userAvatarUrl = ""
                    )
                }
            }
        }
    }

    private fun sortPostsByDate(posts: List<PostUiModel>): List<PostUiModel> {
        return posts.sortedByDescending { post ->
            try {
                SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH).parse(post.createdAt)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse date: ${post.createdAt}")
                null
            }
        }
    }

    // EXACT COPY OF YOUR WORKING LIKE FUNCTIONALITY - COMPLETELY SEPARATE
    // Enhanced Like/Unlike Methods with Transaction Support
    fun likePost(postId: String) {
        viewModelScope.launch {
            try {
                val userId = currentUserId
                if (userId.isEmpty()) {
                    Log.e(TAG, "User not authenticated")
                    _error.value = "User not authenticated"
                    return@launch
                }

                val postIndex = findPostIndex(postId)
                if (postIndex == -1) {
                    Log.e(TAG, "Post not found with ID: $postId")
                    _error.value = "Post not found"
                    return@launch
                }

                val post = _communityPosts.value[postIndex]

                // Use transaction for atomic updates
                updatePostLikeWithTransaction(post, userId)

                Log.d(TAG, "Post $postId like toggled successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle like for post $postId: ${e.message}", e)
                _error.value = "Failed to update like"
            }
        }
    }

    // Alternative method that takes a PostUiModel (for backward compatibility)
    fun likePost(post: PostUiModel) {
        likePost(post.id)
    }

    private suspend fun updatePostLikeWithTransaction(post: PostUiModel, userId: String) {
        val postRef = firestore
            .collection("Community")
            .document(post.communityId)
            .collection("posts")
            .document(post.id)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()
            val isLiked = userId in likedBy
            val newLikedBy = if (isLiked) likedBy - userId else likedBy + userId
            val newLikes = newLikedBy.size

            transaction.update(postRef, mapOf(
                "likes" to newLikes,
                "likedBy" to newLikedBy
            ))
        }.await()

        // Update local state
        _communityPosts.update { posts ->
            posts.map {
                if (it.id == post.id) {
                    val isCurrentlyLiked = userId in it.likedBy
                    it.copy(
                        likes = if (isCurrentlyLiked) it.likes - 1 else it.likes + 1,
                        likedBy = if (isCurrentlyLiked)
                            it.likedBy - userId
                        else
                            it.likedBy + userId
                    )
                } else it
            }
        }
    }

    private fun findPostIndex(postId: String): Int {
        return _communityPosts.value.indexOfFirst { it.id == postId }
    }

    // Fallback method using direct updates (if transaction fails)
    private suspend fun updatePostLikeInFirestore(
        post: PostUiModel,
        userId: String,
        isCurrentlyLiked: Boolean
    ) {
        val postRef = firestore
            .collection("Community")
            .document(post.communityId)
            .collection("posts")
            .document(post.id)

        val updates = if (isCurrentlyLiked) {
            mapOf(
                "likes" to (post.likes - 1).coerceAtLeast(0),
                "likedBy" to post.likedBy.filter { it != userId }
            )
        } else {
            mapOf(
                "likes" to post.likes + 1,
                "likedBy" to post.likedBy + userId
            )
        }

        postRef.update(updates).await()
    }

    private fun updatePostLikeLocally(
        post: PostUiModel,
        userId: String,
        isCurrentlyLiked: Boolean
    ): PostUiModel {
        return if (isCurrentlyLiked) {
            post.copy(
                likes = (post.likes - 1).coerceAtLeast(0),
                likedBy = post.likedBy.filter { it != userId }
            )
        } else {
            post.copy(
                likes = post.likes + 1,
                likedBy = post.likedBy + userId
            )
        }
    }
    // END OF EXACT COPY OF YOUR WORKING LIKE FUNCTIONALITY

    // Post Management Methods
    fun deletePost(postId: String, communityId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true

                // Remove from Firestore
                firestore
                    .collection("Community")
                    .document(communityId)
                    .collection("posts")
                    .document(postId)
                    .delete()
                    .await()

                // Remove from local state
                _communityPosts.update { posts ->
                    posts.filter { it.id != postId }
                }

                Log.d(TAG, "Post $postId deleted successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete post $postId: ${e.message}", e)
                _error.value = "Failed to delete post"
            } finally {
                _loading.value = false
            }
        }
    }

    // FIXED: Keep utility methods from sub-task branch - they provide useful functionality
    fun getUserPosts(userId: String, communityId: String): List<PostUiModel> {
        return _communityPosts.value.filter { it.userId == userId }
    }

    fun getPostById(postId: String): PostUiModel? {
        return _communityPosts.value.find { it.id == postId }
    }

    fun getPostsCount(): Int {
        return _communityPosts.value.size
    }

    fun getLikedPostsByUser(userId: String): List<PostUiModel> {
        return _communityPosts.value.filter { it.likedBy.contains(userId) }
    }

    // Permission Methods - FIXED: Use explicit approach
    fun shouldRequestPhoto(): Boolean = permissionHandler.shouldRequestPhotoPermission()
    fun markPhotoPermissionRequested() = permissionHandler.markPhotoPermissionRequested()

    // State Management Methods
    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _success.value = false
    }

    fun clearPosts() {
        _communityPosts.value = emptyList()
    }

    // Refresh Method
    fun refresh(communityId: String) {
        fetchPostsForCommunity(communityId)
    }

    fun refreshWithRepository(communityId: String) {
        fetchPostsForCommunity(communityId)
    }

    // Real-time listener for posts (optional)
    fun startListeningToPosts(communityId: String) {
        stopListeningToPosts() // Stop any existing listener

        postsListener = firestore
            .collection("Community")
            .document(communityId)
            .collection("posts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to posts: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.let { _ ->

                    fetchPostsForCommunity(communityId)
                    Log.d(TAG, "Real-time update triggered refresh via repository")
                }
            }
    }

    fun stopListeningToPosts() {
        postsListener?.remove()
        postsListener = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningToPosts()
    }
}