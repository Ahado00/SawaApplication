package com.example.sawaapplication.screens.home.presentation.screens.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sawaapplication.R
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.home.presentation.vmModels.HomeViewModel
import com.example.sawaapplication.screens.notification.presentation.viewmodels.NotificationViewModel
import com.example.sawaapplication.ui.screenComponent.PostCard
import java.net.URLEncoder

@Composable
fun PostsTab(viewModel: HomeViewModel, navController: NavController) {
    val posts by viewModel.posts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val communityNames by viewModel.communityNames.collectAsState()
    val userDetails by viewModel.userDetails.collectAsState()
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    val postLikedUserId = viewModel.postLikedEvent.collectAsState().value

    // Trigger a notification whenever a post is liked by a user
    LaunchedEffect(postLikedUserId) {
        postLikedUserId?.let { likedUserId ->
            val post = posts.find { it.userId == likedUserId }
            post?.let {
                notificationViewModel.notifyLike(it)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllPosts()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

            error != null -> Text(
                text = error ?: stringResource(R.string.unknownError),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )

            else ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = integerResource(R.integer.lazyColumnPaddingTop).dp,
                        start = integerResource(R.integer.extraSmallSpace).dp,
                        end = integerResource(R.integer.extraSmallSpace).dp,
                        bottom = integerResource(R.integer.lazyColumnPaddingButton).dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(integerResource(R.integer.lazyColumnArrangement).dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts) { post ->
                        val communityName =
                            communityNames[post.communityId] ?: stringResource(R.string.unknown)
                        val (userName, userImage) = userDetails[post.userId]
                            ?: (stringResource(R.string.unknown) to "")
                        PostCard(
                            post,
                            communityName,
                            communityId = post.communityId,
                            userName,
                            userImage,
                            onClick = {
                                val imageUrl = post.imageUri
                                if (!imageUrl.isNullOrEmpty()) {
                                    val encoded = URLEncoder.encode(imageUrl, "utf-8")
                                    navController.navigate(Screen.FullscreenImage.createRoute(encoded))
                                }
                            },
                            onLikeClick = {
                                viewModel.likePost(post)
                                notificationViewModel.notifyLike(post)
                            },
                            onDeleteClick = {
                                viewModel.deletePost(post)
                            },
                            navController = navController,
                            onCommunityClick = { communityId ->
                                navController.navigate("community_screen/$communityId")
                            }
                        )

                    }
                }
        }
    }
}
