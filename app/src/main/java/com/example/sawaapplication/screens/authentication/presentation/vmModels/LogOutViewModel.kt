package com.example.sawaapplication.screens.authentication.presentation.vmModels

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.sawaapplication.core.sharedPreferences.AuthPreferences
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.authentication.domain.useCases.LogOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LogOutViewModel @Inject constructor(
    private val logOutUseCase: LogOutUseCase,
    private val preferenceManager: AuthPreferences,

): ViewModel(){

     fun preformLogOut(navController: NavController){
        logOutUseCase.invoke()
        preferenceManager.clearToken()
        navController.navigate(Screen.Login.route){
            popUpTo(0) { inclusive = true } // This clears the whole backstack
            launchSingleTop = true          // Prevents duplicate LoginScreen
        }
    }

}