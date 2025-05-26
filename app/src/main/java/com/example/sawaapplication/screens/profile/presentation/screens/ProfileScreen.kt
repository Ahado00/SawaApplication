package com.example.sawaapplication.screens.profile.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.sawaapplication.R
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.home.presentation.screens.component.CustomTabRow
import com.example.sawaapplication.screens.home.presentation.screens.component.PostCard
import com.example.sawaapplication.screens.home.presentation.vmModels.HomeViewModel
import com.example.sawaapplication.screens.profile.presentation.vm.ProfileViewModel

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
    val posts by homeViewModel.posts.collectAsState()
    val loading by homeViewModel.loading.collectAsState()
    val error by homeViewModel.error.collectAsState()
    val communityNames by homeViewModel.communityNames.collectAsState()
    val userDetails by homeViewModel.userDetails.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.posts),
        stringResource(R.string.likes)
    )

    val BADGE_THRESHOLDS = listOf(3, 7, 14, 21, 30)
    val nextThreshold = BADGE_THRESHOLDS.firstOrNull { it > attended } ?: BADGE_THRESHOLDS.last()
    val progress = (attended.toFloat() / nextThreshold).coerceIn(0f, 1f)

    LaunchedEffect(userId) {
        profileViewModel.loadCurrentUserId()
        profileViewModel.loadBadges(userId)
        if (selectedTabIndex == 0) {
            homeViewModel.fetchPostsByUser(userId)
        } else {
            homeViewModel.fetchLikedPostsByUser(userId)
        }
    }

    LaunchedEffect(selectedTabIndex, userId) {
        if (selectedTabIndex == 0) {
            homeViewModel.fetchPostsByUser(userId)
        } else {
            homeViewModel.fetchLikedPostsByUser(userId)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = integerResource(R.integer.profilePadding).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(modifier = Modifier.size(integerResource(R.integer.photoBoxSize).dp)) {
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
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = userName ?: "Unknown",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Text(
                text = userEmail ?: "No email",
                textAlign = TextAlign.Center,
                fontSize = integerResource(R.integer.textSize1).sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.aboutMe),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = integerResource(R.integer.textSize2).sp
            )

            Text(
                text = aboutMe ?: "",
                textAlign = TextAlign.Center,
                fontSize = integerResource(R.integer.textSize2).sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Your Event Badges",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = "🔥 Events Attended: $attended / $nextThreshold events",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            BadgeRow(badges)

            Spacer(Modifier.height(12.dp))
        }

        item {
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
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                }
            }

            error != null -> item {
                Text(
                    text = error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
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
                    onUserImageClick = { homeViewModel.likePost(post) }
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
