package com.example.sawaapplication.navigation

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.sawaapplication.core.sharedPreferences.AuthPreferences
import com.example.sawaapplication.navigation.bottomBar.CustomBottomBar
import com.example.sawaapplication.navigation.topBar.getTopBar
import com.example.sawaapplication.screens.authentication.presentation.screens.LoginScreen
import com.example.sawaapplication.screens.authentication.presentation.screens.ResetPasswordScreen
import com.example.sawaapplication.screens.authentication.presentation.screens.SignUpScreen
import com.example.sawaapplication.screens.authentication.presentation.screens.SplashScreen
import com.example.sawaapplication.screens.chat.presentation.screens.ChatScreen
import com.example.sawaapplication.screens.chat.presentation.screens.GroupMembersScreen
import com.example.sawaapplication.screens.chat.presentation.screens.ViewChatsScreen
import com.example.sawaapplication.screens.communities.presentation.screens.CommunityScreen
import com.example.sawaapplication.screens.communities.presentation.screens.EditCommunityScreen
import com.example.sawaapplication.screens.communities.presentation.screens.ExploreScreen
import com.example.sawaapplication.screens.home.presentation.screens.HomeScreen
import com.example.sawaapplication.screens.notification.presentation.screens.NotificationScreen
import com.example.sawaapplication.screens.communities.presentation.screens.NewCommunity
import com.example.sawaapplication.screens.communities.presentation.screens.MyCommunitiesScreen
import com.example.sawaapplication.screens.event.presentation.screens.CreateNewEventScreen
import com.example.sawaapplication.screens.event.presentation.screens.EditEventScreen
import com.example.sawaapplication.screens.event.presentation.screens.EventDetailScreen
import com.example.sawaapplication.screens.onboarding.presentation.screens.OnBoardingScreen
import com.example.sawaapplication.screens.post.presentation.screens.CreatePostScreen
import com.example.sawaapplication.screens.profile.presentation.screens.EditProfileScreen
import com.example.sawaapplication.screens.profile.presentation.screens.ProfileScreen
import com.example.sawaapplication.screens.profile.presentation.screens.UserAccount
import com.example.sawaapplication.screens.profile.presentation.vm.ProfileViewModel
import java.net.URLDecoder
import com.example.sawaapplication.screens.post.presentation.screens.FullscreenImageScreen


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigation(
    navController: NavHostController,
    isDarkTheme: Boolean,
    changeAppTheme: () -> Unit
) {
    var tokenState by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val authPreferences = remember { AuthPreferences(context) }

    LaunchedEffect(Unit) {
        tokenState = authPreferences.getToken()
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val selectedIndex = bottomBarScreens.indexOfFirst { screen ->
        screen.route == currentRoute
    }

    val showBottomBar = bottomBarScreens.any { it.route == currentRoute }

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val imageUrl by profileViewModel.profileImageUrl.collectAsState()

    Scaffold(
        topBar = {
            getTopBar(currentRoute, navController, imageUrl)?.invoke()
        },

        bottomBar = {
            if (showBottomBar) {
                CustomBottomBar(
                    selectedIndex = bottomBarScreens.indexOfFirst { screen ->
                        screen.route == currentRoute
                    }.coerceAtLeast(0), // fallback to index 0 if not found
                    onItemSelected = { selectedIndex ->
                        val selectedScreen = bottomBarScreens[selectedIndex]
                        navController.navigate(selectedScreen.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (tokenState.isNullOrEmpty()) Screen.SplashScreen.route else Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Screen.SplashScreen.route) {
                SplashScreen(navController)
            }
            composable(Screen.Onboarding.route) {
                OnBoardingScreen(
                    navController, changeAppTheme = changeAppTheme
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(navController)
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(navController)
            }
            composable(Screen.ForgotPass.route) {
                ResetPasswordScreen(navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }
            composable(Screen.NewCommunity.route) {
                NewCommunity(navController)
            }
            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Explore.route) {
                ExploreScreen(navController)
            }
            composable(Screen.Notification.route) {
                NotificationScreen()
            }
            composable(Screen.Community.route) {
                MyCommunitiesScreen(navController)
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    navController = navController,
                    changeAppTheme = changeAppTheme
                )
            }

            // ViewChatsScreen for listing chats
            composable(
                route = Screen.Chats.route,
                arguments = listOf(navArgument("communityId") { type = NavType.StringType })
            ) { backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
                ViewChatsScreen(
                    navController = navController,
                )
            }

            // ChatScreen for a specific community
            composable(
                route = "chat/{communityId}",
                arguments = listOf(navArgument("communityId") { type = NavType.StringType })
            ) { backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
                ChatScreen(
                    communityId = communityId,
                    navController = navController
                )
            }

            // 1) Community detail
            composable(
                route = "community_screen/{communityId}",
                arguments = listOf(navArgument("communityId") { type = NavType.StringType })
            ) {
                val communityId = it.arguments!!.getString("communityId")!!
                CommunityScreen(
                    communityId = communityId,
                    onClick = { imageUrl ->

                        navController.navigate(Screen.FullscreenImage.createRoute(imageUrl))
                    },
                    navController = navController
                )
            }

            composable("edit_community/{communityId}") { backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
                EditCommunityScreen(communityId = communityId, navController = navController)
            }


// 2) Full-screen by postId
            composable(
                route = Screen.FullscreenImage.route,
                arguments = listOf(navArgument("imageUrl") { type = NavType.StringType })
            ) { backStack ->
                val encoded = backStack.arguments!!.getString("imageUrl")!!
                val decoded = URLDecoder.decode(encoded, "utf-8")

                FullscreenImageScreen(
                    imageUrl = decoded,
                    onDismiss = { navController.popBackStack() }
                )
            }




            composable("create_event/{communityId}") { backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
                CreateNewEventScreen(navController, communityId)
            }


            composable("create_post/{communityId}") { backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
                CreatePostScreen(navController, communityId)
            }


            composable("groupMembers/{communityId}") { backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
                Log.d("DEBUG", "Navigation received communityId: $communityId")
                GroupMembersScreen(
                    communityId = communityId,
                    navController = navController
                )
            }

            composable(
                route = Screen.UserAccount.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                userId?.let {
                    UserAccount(
                        navController = navController,
                        profileViewModel = hiltViewModel(),
                        userId = it
                    )
                }
            }

            composable("event_detail/{communityId}/{eventId}") { backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId")
                val eventId = backStackEntry.arguments?.getString("eventId")
                if (communityId != null && eventId != null) {
                    EventDetailScreen(
                        communityId = communityId,
                        eventId = eventId,
                        navController = navController
                    )
                }
            }
            composable(
                "edit_event/{communityId}/{eventId}",
                arguments = listOf(
                    navArgument("communityId") { type = NavType.StringType },
                    navArgument("eventId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val communityId =
                    backStackEntry.arguments?.getString("communityId") ?: return@composable
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable

                EditEventScreen(
                    navController = navController,
                    eventId = eventId,
                    communityId = communityId
                )
            }


        }
    }
}

val bottomBarScreens = listOf(
    Screen.Home,
    Screen.Explore,
    Screen.Community,
    Screen.Chats
)
