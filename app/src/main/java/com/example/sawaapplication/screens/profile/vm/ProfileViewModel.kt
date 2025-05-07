package com.example.sawaapplication.screens.profile.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawaapplication.screens.authentication.data.dataSources.remote.FirebaseAuthDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : ViewModel() {

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> get() = _userName

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> get() = _userEmail

    private val _aboutMe = MutableStateFlow<String?>(null)
    val aboutMe: StateFlow<String?> get() = _aboutMe

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> get() = _profileImageUrl

    init {
        getUserData()
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

    fun updateAboutMe(newAboutMe: String){
        viewModelScope.launch {
            firebaseAuthDataSource.updateUserInfo(newAboutMe)
            _aboutMe.value = newAboutMe
        }
    }
    fun updateName(newName: String){
        viewModelScope.launch {
            firebaseAuthDataSource.updateUserName(newName)
            _userName.value = newName
        }
    }

    private fun fetchAboutMe(userId : String) {
        val userRef = FirebaseFirestore.getInstance().collection("User").document(userId)

        // Start a real-time Firestore listener on the user document to track 'aboutMe' updates
        userRef.addSnapshotListener { documentSnapshot, error ->

            if (error != null) {
                Log.e("ProfileViewModel", "Firestore error: ", error)
                return@addSnapshotListener
            }

            //  When the document exists and is not null, extract the 'aboutMe' field
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val about = documentSnapshot.getString("aboutMe")

                //  Update the StateFlow with the latest value from Firestore
                // This triggers recomposition in the UI if it's collecting aboutMe
                _aboutMe.value = about
            }
        }
    }

    private fun fetchUserName(userId: String) {
        val userRef = FirebaseFirestore.getInstance().collection("User").document(userId)

        // Changed from .get() to addSnapshotListener for real-time Firestore updates
        // Now directly updates _userName.value when Firestore document changes
        userRef.addSnapshotListener { documentSnapshot, error ->
            if (error != null) {
                Log.e("ProfileViewModel", "Firestore error (fetchUserName): ", error)
                return@addSnapshotListener // early return if Firestore listener throws an error
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                val name = documentSnapshot.getString("name")
                _userName.value = name //  update StateFlow to notify UI of real-time name change
            }
        }
    }

    private fun fetchProfileImageUrl(userId: String) {
        val userRef = FirebaseFirestore.getInstance().collection("User").document(userId)
        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                _profileImageUrl.value = document.getString("image")
            }
        }
    }

    fun uploadProfileImage(uri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = firebaseAuth.currentUser ?: return
        val storageRef = FirebaseStorage.getInstance().reference
            .child("profileImages/${user.uid}.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    FirebaseFirestore.getInstance()
                        .collection("User")
                        .document(user.uid)
                        .update("image", downloadUri.toString())
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { onFailure(it) }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

}