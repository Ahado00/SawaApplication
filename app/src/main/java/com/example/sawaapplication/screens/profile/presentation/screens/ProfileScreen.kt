package com.example.sawaapplication.screens.profile.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sawaapplication.R
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.home.presentation.vmModels.HomeViewModel
import com.example.sawaapplication.screens.profile.presentation.vm.ProfileViewModel
import com.example.sawaapplication.ui.screenComponent.CustomTabRow
import com.example.sawaapplication.ui.screenComponent.PostCard

@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val userName by profileViewModel.userName.collectAsState()
    val userEmail by profileViewModel.userEmail.collectAsState()
    val aboutMe by profileViewModel.aboutMe.collectAsState()
    val imageUrl by profileViewModel.profileImageUrl.collectAsState()
    val badges by profileViewModel.awardedBadges.collectAsState()
    val attended by profileViewModel.attendedCount.collectAsState()
    val userId by profileViewModel.currentUserId.collectAsState()
    val userDetails by homeViewModel.userDetails.collectAsState()
    val loading by homeViewModel.loading.collectAsState()
    val error by homeViewModel.error.collectAsState()
    val communityNames by homeViewModel.communityNames.collectAsState()
    val posts by homeViewModel.posts.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.posts),
        stringResource(R.string.likes)
    )

    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl

    // BADGE & PROGRESS CALC
    val BADGE_THRESHOLDS = listOf(3, 7, 14, 21, 30)
    val nextThreshold = BADGE_THRESHOLDS.firstOrNull { it > attended } ?: BADGE_THRESHOLDS.last()
    val progress = (attended.toFloat() / nextThreshold).coerceIn(0f, 1f)

    LaunchedEffect(userId, selectedTabIndex) {
        profileViewModel.loadCurrentUserId()
        profileViewModel.loadBadges(userId)
        if (selectedTabIndex == 0) {
            homeViewModel.fetchPostsByUser(userId)
        } else {
            homeViewModel.fetchLikedPostsByUser(userId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .zIndex(1f)
        ) {
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = integerResource(R.integer.profilePadding).dp)
                .zIndex(0f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(32.dp))

                // PROFILE IMAGE & EDIT BUTTON
                Box(
                    modifier = Modifier
                        .size(integerResource(R.integer.photoBoxSize).dp)
                ) {
                    Image(
                        painter = if (!imageUrl.isNullOrEmpty())
                            rememberAsyncImagePainter(imageUrl)
                        else
                            painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Profile image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(integerResource(R.integer.photoBoxSize).dp)

                    )

                    IconButton(
                        onClick = { navController.navigate(Screen.EditProfile.route) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(
                                color = Color(0xFFFF5722),
                                shape = RoundedCornerShape(48.dp)
                            )
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.background
                        )
                    }
                }
                    Column(
                        modifier = Modifier
                            .padding(vertical = 20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = userName ?: "Unknown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = userEmail ?: "No email",
                            fontSize = integerResource(R.integer.textSize1).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.aboutMe),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = integerResource(R.integer.textSize2).sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = aboutMe ?: "",
                            fontSize = integerResource(R.integer.textSize2).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }


                Spacer(Modifier.height(10.dp))

                // BADGES & STATS
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )

                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 18.dp, horizontal = 8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.your_event_badges),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🔥 ${stringResource(R.string.events_attended)}: $attended / $nextThreshold",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(4.dp)),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Badges Row
                        BadgeRow(badges)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            item {
                // TAB ROW
                CustomTabRow(
                    tabs = tabs,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = {
                        selectedTabIndex = it
                        if (it == 0) homeViewModel.fetchPostsByUser(userId)
                        else homeViewModel.fetchLikedPostsByUser(userId)
                    }
                )
            }
            when {
                loading -> item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                    }
                }

                error != null -> item {
                    Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                else -> items(posts) { post ->
                    val communityName = communityNames[post.communityId] ?: "Unknown"
                    val (userName, userImage) = userDetails[post.userId] ?: ("Unknown" to "")
                    PostCard(
                        post = post,
                        communityName = communityName,
                        communityId = post.communityId,
                        userName = userName,
                        userImage = userImage,
                        onClick = {},
                        onLikeClick = { homeViewModel.likePost(post) },
                        onDeleteClick = { homeViewModel.deletePost(post) },
                        navController = navController,
                        Modifier
                            .padding(
                                top = integerResource(R.integer.smallSpace).dp,
                                bottom = integerResource(R.integer.smallSpace).dp
                            ),
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
