package com.example.sawaapplication.screens.profile.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
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
import com.example.sawaapplication.screens.home.presentation.screens.component.CustomTabRow
import com.example.sawaapplication.screens.home.presentation.screens.component.PostCard
import com.example.sawaapplication.screens.home.presentation.vmModels.HomeViewModel
import com.example.sawaapplication.screens.profile.presentation.vm.ProfileViewModel

@Composable
fun UserAccount(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    userId: String
) {
    val selectedUser by profileViewModel.selectedUser.collectAsState()
    val viewModel: HomeViewModel = hiltViewModel()
    val badgeDefs by profileViewModel.viewedDefinitions.collectAsState()
    val badgeAwarded by profileViewModel.viewedAwarded.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.posts),
        stringResource(R.string.likes)
    )

    LaunchedEffect(userId) {
        profileViewModel.fetchUserById(userId)
        profileViewModel.loadUserBadges(userId)

        if (selectedTabIndex == 0) viewModel.fetchPostsByUser(userId)
        else viewModel.fetchLikedPostsByUser(userId)
    }

    val posts by viewModel.posts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val communityNames by viewModel.communityNames.collectAsState()
    val userDetails by viewModel.userDetails.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 56.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            selectedUser?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = if (!user.image.isNullOrEmpty())
                            rememberAsyncImagePainter(user.image)
                        else
                            painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Profile image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user.name ?: "Unknown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = user.aboutMe ?: "",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CompactBadgeRow(
                        definitions = badgeDefs,
                        awarded = badgeAwarded,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        iconSize = 28.dp,
                        spacing = 4.dp
                    )
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading user info...")
            }
        }

        item {
            CustomTabRow(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = {
                    selectedTabIndex = it
                    if (it == 0) viewModel.fetchPostsByUser(userId)
                    else viewModel.fetchLikedPostsByUser(userId)
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
                    onLikeClick = { viewModel.likePost(post) },
                    onDeleteClick = { viewModel.deletePost(post) },
                    navController = navController,
                    onUserImageClick = { viewModel.likePost(post) }
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
