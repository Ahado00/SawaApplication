package com.example.sawaapplication.navigation

import kotlinx.serialization.Serializable
import java.net.URLEncoder

@Serializable
sealed class Screen(val route: String) {
    @Serializable data object SplashScreen : Screen("splash_screen")
    @Serializable data object Onboarding : Screen("onboarding")
    @Serializable data object Login : Screen("login")
    @Serializable data object SignUp : Screen("signup")
    @Serializable data object ForgotPass : Screen("forgot_pass")
    @Serializable data object Home : Screen("home")
    @Serializable data object Explore : Screen("explore")
    @Serializable data object Notification : Screen("notification")
    @Serializable data object Community : Screen("community")
    @Serializable data object Profile : Screen("profile")
    @Serializable data object NewCommunity : Screen("new_community")
    @Serializable data object NewPost : Screen("")
    @Serializable data object EditProfile : Screen("editProfile")
    @Serializable data object Chats : Screen("chats/{communityId}")

    @Serializable data object GroupMembers : Screen("groupMembers/{communityId}")

    @Serializable
    data object UserAccount : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }

    @Serializable
    data object FullscreenImage : Screen("fullscreen/{imageUrl}") {
        fun createRoute(imageUrl: String): String =
            "fullscreen/${URLEncoder.encode(imageUrl, "utf-8")}"
    }
    //    @Serializable
//    data object CreateNewEvent : Screen("create_event/{communityId}") {
//        fun passCommunityId(communityId: String): String {
//            return "create_event/$communityId"
//        }
    //  }
}
