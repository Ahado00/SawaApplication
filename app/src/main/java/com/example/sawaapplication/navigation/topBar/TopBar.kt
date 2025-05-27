package com.example.sawaapplication.navigation.topBar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.ArrowCircleLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sawaapplication.R
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.notification.presentation.viewmodels.NotificationViewModel
import com.example.sawaapplication.screens.profile.presentation.vm.ProfileViewModel

@Composable
fun TopBar(
    title: String,
    showNotificationIcon: Boolean = false,
    showLogoCenter: Boolean = false,
    imageUrl: String? = null,
    hasUnread: Boolean = false,
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    navIcon: @Composable (() -> Unit)? = null,
) {

    val profileViewModel: ProfileViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        profileViewModel.refreshUserData()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = integerResource(id = R.integer.mediumSpace).dp)
            .padding(
                top = 45.dp,
                bottom = integerResource(id = R.integer.smallSpace).dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (navIcon != null) {
                    navIcon()
                } else {
                    Image(
                        painter = imageUrl?.let { rememberAsyncImagePainter(it) }
                            ?: painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Profile image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(integerResource(id = R.integer.topBarIconSize).dp)
                            .clickable { onProfileClick() }
                    )
                }
            }

            if (showNotificationIcon) {
                IconButton(onClick = onNotificationClick) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (hasUnread) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .background(Color.Red, CircleShape)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(integerResource(id = R.integer.topBarIconSize).dp))
            }
        }

        if (showLogoCenter) {
            Image(
                painter = painterResource(id = R.drawable.applogo),
                contentDescription = "App Logo",
                modifier = Modifier.size(integerResource(id = R.integer.topBarIconSize).dp)
            )
        } else {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun getTopBar(
    currentRoute: String?,
    navController: NavController,
    imageUrl: String?,
    notificationViewModel: NotificationViewModel = hiltViewModel()
): (@Composable () -> Unit)? {

    val hasUnread = notificationViewModel.hasUnreadNotifications.collectAsState().value
    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl

    return when (currentRoute) {
        Screen.Community.route -> {
            {
                TopBar(
                    title = stringResource(R.string.myCommunities),
                    imageUrl = imageUrl,
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }
        }

        Screen.Explore.route -> {
            {
                TopBar(
                    title = stringResource(R.string.exploreCommunity),
                    imageUrl = imageUrl,
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }
        }

        Screen.Home.route -> {
            {
                TopBar(
                    title = "",
                    showNotificationIcon = true,
                    showLogoCenter = true,
                    imageUrl = imageUrl,
                    hasUnread = hasUnread,
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onNotificationClick = {
                        notificationViewModel.markAsRead()
                        navController.navigate(Screen.Notification.route)
                    }
                )
            }
        }

        Screen.Notification.route -> {
            {
                TopBar(
                    title = stringResource(R.string.notification),
                    imageUrl = imageUrl,
                    navIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .graphicsLayer {
                                        scaleX = if (isRtl) -1f else 1f
                                    }
                            )
                        }
                    })

            }

        }

        Screen.EditProfile.route -> {
            {
                TopBar(
                    title = stringResource(R.string.editProfile),
                    imageUrl = imageUrl,
                    navIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .graphicsLayer {
                                        scaleX = if (isRtl) -1f else 1f
                                    }
                            )
                        }
                    },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }
        }

        Screen.Chats.route -> {
            {
                TopBar(
                    title = stringResource(R.string.chats),
                    showNotificationIcon = true,
                    imageUrl = imageUrl,
                    hasUnread = hasUnread,
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onNotificationClick = {
                        notificationViewModel.markAsRead()
                        navController.navigate(Screen.Notification.route)
                    }
                )
            }
        }

        Screen.GroupMembers.route -> {
            {
                TopBar(
                    title = stringResource(R.string.groupMembers),
                    navIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .graphicsLayer {
                                        scaleX = if (isRtl) -1f else 1f
                                    }
                            )
                        }
                    }
                )
            }
        }
        "community_screen/{communityId}" -> {
            {
                TopBar(
                    title ="",
                    navIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .graphicsLayer {
                                        scaleX = if (isRtl) -1f else 1f
                                    }
                            )
                        }
                    }
                )
            }
        }

        else -> null
    }
}
