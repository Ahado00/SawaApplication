package com.example.sawaapplication.screens.authentication.presentation.vmModels

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sawaapplication.core.sharedPreferences.AuthPreferences
import com.example.sawaapplication.core.sharedPreferences.TokenProvider
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.authentication.domain.useCases.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val tokenProvider: TokenProvider,
    private val preferenceManager: AuthPreferences
) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val isLoginSuccessful = loginUseCase(email, password)

            _authState.value = isLoginSuccessful.fold(
                onSuccess = {
                    // Get token and save locally
                    val token = tokenProvider.getToken()
                    preferenceManager.saveToken(token)
                    AuthState.Authenticated
                },
                onFailure = { AuthState.Error(it.message ?: "Login failed.") }
            )
        }
    }
}

fun handleAuthStateLogin(
    authState: AuthState,
    context: Context,
     navController: NavController
) {
    when (authState) {
        is AuthState.Authenticated -> {
            Toast.makeText(context, "Logged in successfully", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }

        is AuthState.Error -> {
            Toast.makeText(context, authState.message, Toast.LENGTH_SHORT).show()
        }

        else -> Unit
    }
}