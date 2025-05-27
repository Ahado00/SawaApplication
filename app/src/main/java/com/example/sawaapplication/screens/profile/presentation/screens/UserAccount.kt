package com.example.sawaapplication.screens.profile.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.ui.screenComponent.CustomTabRow
import com.example.sawaapplication.ui.screenComponent.PostCard
import com.example.sawaapplication.screens.home.presentation.vmModels.HomeViewModel
import com.example.sawaapplication.screens.profile.presentation.vm.ProfileViewModel
import com.example.sawaapplication.ui.theme.thirdOrange
import java.net.URLEncoder

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

    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl

    LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.11f)
                                )
                            )
                        )
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)

                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer {
                                    scaleX = if (isRtl) -1f else 1f
                                }                        )

                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Image
                        Image(
                            painter = if (!selectedUser?.image.isNullOrEmpty())
                                rememberAsyncImagePainter(selectedUser!!.image)
                            else
                                painterResource(id = R.drawable.ic_launcher_background),
                            contentDescription = "Profile image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        // Name
                        Text(
                            text = selectedUser?.name ?: "Unknown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        // About Me
                        if (!selectedUser?.aboutMe.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 32.dp, vertical = 6.dp)
                                    .fillMaxWidth(0.9f),
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(2.dp),
                                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                            ) {
                                Column(Modifier.padding(12.dp)) {

                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = selectedUser?.aboutMe.orEmpty(),
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        // Badges
                        if (badgeDefs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .padding(horizontal = 32.dp)
                                    .fillMaxWidth(0.9f),
                                elevation = CardDefaults.cardElevation(2.dp),
                                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        vertical = 10.dp,
                                        horizontal = 6.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🌟 "+stringResource(R.string.badges)+" :",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = thirdOrange,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    CompactBadgeRow(
                                        definitions = badgeDefs,
                                        awarded = badgeAwarded,
                                        iconSize = 32.dp,
                                        spacing = 8.dp
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Tab row
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

            //Posts / loading/error
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
                        onClick = {
                            val imageUrl = post.imageUri
                            if (!imageUrl.isNullOrEmpty()) {
                                val encoded = URLEncoder.encode(imageUrl, "utf-8")
                                navController.navigate(Screen.FullscreenImage.createRoute(encoded))
                            }
                        },                        onLikeClick = { viewModel.likePost(post) },
                        onDeleteClick = { viewModel.deletePost(post) },
                        navController = navController,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
