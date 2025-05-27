package com.example.sawaapplication.screens.profile.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawaapplication.screens.authentication.data.dataSources.remote.FirebaseAuthDataSource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri
import com.example.sawaapplication.core.permissions.PermissionHandler
import com.example.sawaapplication.screens.profile.domain.model.Badge
import com.example.sawaapplication.screens.profile.domain.model.User
import com.example.sawaapplication.screens.profile.domain.useCases.FetchAboutMeUseCase
import com.example.sawaapplication.screens.profile.domain.useCases.FetchImageUseCase
import com.example.sawaapplication.screens.profile.domain.useCases.FetchUserByIdUseCase
import com.example.sawaapplication.screens.profile.domain.useCases.FetchUserNameUseCase
import com.example.sawaapplication.screens.profile.domain.useCases.GetAttendedEventUseCase
import com.example.sawaapplication.screens.profile.domain.useCases.GetAwardedBadges
import com.example.sawaapplication.screens.profile.domain.useCases.GetBadgeDefinitionsUseCase
import com.example.sawaapplication.screens.profile.domain.useCases.GrantBadgeUseCase
import com.example.sawaapplication.screens.profile.domain.useCases.UploadProfileImageUseCase
import com.google.firebase.firestore.FirebaseFirestore


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val permissionHandler: PermissionHandler,
    private val fetchAboutMeUseCase: FetchAboutMeUseCase,
    private val fetchUserNameUseCase: FetchUserNameUseCase,
    private val fetchImageUseCase: FetchImageUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase,
    private val fetchUserByIdUseCase: FetchUserByIdUseCase,
    private val grantBadgeUseCase: GrantBadgeUseCase,
    private val getAttendedEventUseCase: GetAttendedEventUseCase,
    private val getBadgeDefinitionsUseCase: GetBadgeDefinitionsUseCase,
    private val getAwardedBadges: GetAwardedBadges,
) : ViewModel() {

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> get() = _userName

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> get() = _userEmail

    private val _aboutMe = MutableStateFlow<String?>(null)
    val aboutMe: StateFlow<String?> get() = _aboutMe

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> get() = _profileImageUrl

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser

    private val _isUploading = MutableStateFlow(false)

    private val _uploadError = MutableStateFlow<String?>(null)

    private val _attendedCount = MutableStateFlow(0)
    val attendedCount: StateFlow<Int> = _attendedCount

    // For Current User profile
    private val _badgeDefinitions   = MutableStateFlow<List<Badge>>(emptyList())
    val badgeDefinitions: StateFlow<List<Badge>> = _badgeDefinitions

    private val _awardedBadges      = MutableStateFlow<List<Badge>>(emptyList())
    val awardedBadges: StateFlow<List<Badge>> = _awardedBadges

    //for viewed user profile
    private val _viewedDefinitions  = MutableStateFlow<List<Badge>>(emptyList())
    val viewedDefinitions: StateFlow<List<Badge>> = _viewedDefinitions

    private val _viewedAwarded      = MutableStateFlow<List<Badge>>(emptyList())
    val viewedAwarded: StateFlow<List<Badge>> = _viewedAwarded


    init {
        getUserData()
        loadCurrentUserId()
    }

    fun loadCurrentUserId() {
        val id = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        _currentUserId.value = id
    }

    private fun getUserData() {
        val user = firebaseAuth.currentUser
        _userName.value = user?.displayName
        _userEmail.value = user?.email
        user?.let {

            fetchAboutMe(it.uid)
            fetchUserName(it.uid)
            fetchProfileImageUrl(it.uid)
        }
    }

    fun updateAboutMe(newAboutMe: String) {
        viewModelScope.launch {
            firebaseAuthDataSource.updateUserInfo(newAboutMe)
            _aboutMe.value = newAboutMe
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            firebaseAuthDataSource.updateUserName(newName)
            _userName.value = newName
        }
    }
    private fun fetchAboutMe(userId: String) {
        viewModelScope.launch {
            val about =fetchAboutMeUseCase(userId)
            _aboutMe.value = about
        }
    }
    private fun fetchUserName(userId: String) {
        viewModelScope.launch {
            val name = fetchUserNameUseCase(userId)
            _userName.value = name
        }
    }

    private fun fetchProfileImageUrl(userId: String) {
        viewModelScope.launch {
            val imageUrl = fetchImageUseCase(userId)
            _profileImageUrl.value = imageUrl
        }
    }

    fun uploadProfileImage(
        uri: Uri,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadError.value = null

            val success = uploadProfileImageUseCase(uri)

            _isUploading.value = false

            if (success) {
                onSuccess()
            } else {
                _uploadError.value = "Failed to upload image"
                onFailure()
            }
        }
    }
    fun fetchUserById(userId: String) {
        viewModelScope.launch {
            val user = fetchUserByIdUseCase(userId)
            _selectedUser.value = user
        }
    }
    fun shouldRequestPhoto() = permissionHandler.shouldRequestPhotoPermission()
    fun markPhotoPermissionRequested() = permissionHandler.markPhotoPermissionRequested()

    //User Event Attendance Badges
    fun loadBadges(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _badgeDefinitions.value = getBadgeDefinitionsUseCase()
            // fetch count
            val attended = getAttendedEventUseCase(userId)
            _attendedCount.value = attended.size
            // award badges
            grantBadgeUseCase(userId)
            // update UI
            _awardedBadges.value = getAwardedBadges(userId)
        }
    }

    fun loadUserBadges(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _viewedDefinitions.value = getBadgeDefinitionsUseCase()
            _viewedAwarded.value     = getAwardedBadges(userId)
        }
    }

    fun refreshUserData() {
        loadCurrentUserId()
        getUserData()
    }
}