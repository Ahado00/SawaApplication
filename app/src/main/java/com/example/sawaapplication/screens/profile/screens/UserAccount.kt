package com.example.sawaapplication.screens.profile.screens

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
import com.example.sawaapplication.screens.profile.vm.ProfileViewModel

@Composable
fun UserAccount(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    userId: String
) {
    val selectedUser by profileViewModel.selectedUser.collectAsState()
    val viewModel: HomeViewModel = hiltViewModel()

    LaunchedEffect(userId) {
        profileViewModel.fetchUserById(userId)
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Posts", "Likes")

    Column(modifier = Modifier.fillMaxSize()) {
        // Show user info if available
        selectedUser?.let { user ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
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
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Loading user info...")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tab Row
        CustomTabRow(
            tabs = tabs,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it }
        )

        when (selectedTabIndex) {
            0 -> MyPostsTab(viewModel, navController,userId)
            1 -> PostsTabLike(viewModel, navController,userId)
        }
    }
}

@Composable
fun MyPostsTab(viewModel: HomeViewModel,navController: NavController,userId: String) {
    val posts by viewModel.posts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val communityNames by viewModel.communityNames.collectAsState()
    val userDetails by viewModel.userDetails.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.fetchPostsByUser(userId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

            error != null -> Text(
                text = error ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )

            else ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 72.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 56.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts) { post ->
                        val communityName = communityNames[post.communityId] ?: "Unknown"
                        val (userName, userImage) = userDetails[post.userId] ?: ("Unknown" to "")
                        PostCard(
                            post,
                            communityName,
                            userName,
                            userImage,
                            onClick = {},
                            onLikeClick = { viewModel.likePost(post) } ,
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
}

@Composable
fun PostsTabLike(viewModel: HomeViewModel,navController: NavController,userId: String) {
    val posts by viewModel.posts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val communityNames by viewModel.communityNames.collectAsState()
    val userDetails by viewModel.userDetails.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.fetchLikedPostsByUser(userId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

            error != null -> Text(
                text = error ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )

            else ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 72.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 56.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts) { post ->
                        val communityName = communityNames[post.communityId] ?: "Unknown"
                        val (userName, userImage) = userDetails[post.userId] ?: ("Unknown" to "")
                        PostCard(
                            post,
                            communityName,
                            userName,
                            userImage,
                            onClick = {},
                            onLikeClick = { viewModel.likePost(post) } ,
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
}
