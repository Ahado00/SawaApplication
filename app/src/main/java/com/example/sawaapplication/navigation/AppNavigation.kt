package com.example.sawaapplication.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sawaapplication.screens.onboarding.presentation.screens.OnBoardingScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
) {
    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Onboarding,
            modifier = Modifier.padding(padding)
        ) {
            composable<Screen.Onboarding> {
                OnBoardingScreen(navController)
            }

        }

    }
}