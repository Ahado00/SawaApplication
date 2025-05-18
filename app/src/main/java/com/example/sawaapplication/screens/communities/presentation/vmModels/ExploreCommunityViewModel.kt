package com.example.sawaapplication.screens.communities.presentation.vmModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.sawaapplication.screens.communities.domain.model.Community
import com.example.sawaapplication.screens.communities.domain.useCases.JoinCommunityUseCase
import com.example.sawaapplication.screens.communities.domain.useCases.LeaveCommunityUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

@HiltViewModel
class ExploreCommunityViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    val currentUserId = firebaseAuth.currentUser?.uid ?: ""

    @Inject
    lateinit var leaveCommunityUseCase: LeaveCommunityUseCase

    var searchText by mutableStateOf("")
    var communities by mutableStateOf<List<Community>>(emptyList())

    private val _hasJoinedOrLeft = MutableStateFlow(false)
    val hasJoinedOrLeft: StateFlow<Boolean> = _hasJoinedOrLeft

    private val _error = mutableStateOf<String?>(null)
    val error: String? get() = _error.value

    var selectedFilter by mutableStateOf(CommunityFilterType.DEFAULT)

    init {
        fetchCommunities()
    }

    private fun fetchCommunities() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("Community").get().await()
                communities = snapshot.documents.mapNotNull { document ->
                    try {
                        val community = Community(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            description = document.getString("description") ?: "",
                            image = document.getString("image") ?: "",
                            creatorId = document.getString("creatorId") ?: "",
                            members = document.get("members") as? List<String> ?: emptyList(),
                            createdAt = document.get("createdAt")?.toString() ?: "",
                            updatedAt = document.get("updatedAt")?.toString() ?: ""
                        )
                        community
                    } catch (e: Exception) {
                        Log.e(
                            "ExploreCommunityVM",
                            "Error parsing community data for document: ${document.id}",
                            e
                        )
                        null
                    }
                }

                communities = communities.sortedBy { it.createdAt.toLongOrNull() ?: Long.MAX_VALUE }

            } catch (e: Exception) {
                Log.e("ExploreCommunityVM", "Error fetching communities", e)
            }
        }
    }

    @Inject
    lateinit var joinCommunityUseCase: JoinCommunityUseCase

    fun joinCommunity(communityId: String, userId: String) {
        viewModelScope.launch {
            val result = joinCommunityUseCase(communityId, userId)
            result.onSuccess {
                fetchCommunities() // just refresh the list
                _hasJoinedOrLeft.value = true
            }.onFailure {
                _error.value = "Failed to join community: ${it.message}"
            }
        }
    }

    fun leaveCommunity(communityId: String, userId: String) {
        viewModelScope.launch {
            val result = leaveCommunityUseCase(communityId, userId)
            result.onSuccess {
                fetchCommunities() // Refresh data
                _hasJoinedOrLeft.value = true
            }.onFailure {
                _error.value = "Failed to leave community: ${it.message}"
            }
        }
    }

    fun resetJoinLeaveState() {
        _hasJoinedOrLeft.value = false
    }

    fun onSearchTextChange(newText: String) {
        searchText = newText
    }

    val filteredCommunities: List<Community>
        get() {
            val baseList = if (searchText.isBlank()) communities
            else communities.filter { it.name.contains(searchText, ignoreCase = true) }

            return when (selectedFilter) {
                CommunityFilterType.MOST_POPULAR ->
                    baseList.sortedByDescending { it.members.size }

                CommunityFilterType.MOST_RECENT ->
                    baseList.sortedByDescending { it.createdAt.toLongOrNull() ?: 0L }

                CommunityFilterType.DEFAULT ->
                    baseList.sortedBy { it.createdAt.toLongOrNull() ?: Long.MAX_VALUE }
            }
        }

}

enum class CommunityFilterType {
    DEFAULT,
    MOST_POPULAR,
    MOST_RECENT
}