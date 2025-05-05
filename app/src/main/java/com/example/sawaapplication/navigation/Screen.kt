package com.example.sawaapplication.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Onboarding : Screen()

    @Serializable
    data object Login : Screen()

    @Serializable
    data object SignUp : Screen()

    @Serializable
    data object Profile : Screen()

}
