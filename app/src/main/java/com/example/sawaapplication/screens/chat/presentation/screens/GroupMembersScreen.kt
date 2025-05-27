package com.example.sawaapplication.screens.chat.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sawaapplication.R
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.chat.presentation.vmModels.ChatViewModel
import com.example.sawaapplication.screens.communities.presentation.vmModels.CommunityViewModel

@Composable
fun GroupMembersScreen(communityId: String, navController: NavController) {
    val communityViewModel: CommunityViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()

    val communityDetails by communityViewModel.communityDetail.collectAsState()
    val communityImage = communityDetails?.image.orEmpty()
    val communityName = communityDetails?.name.orEmpty()

    val members by chatViewModel.communityMembers.collectAsState()
    val loading by chatViewModel.loading.collectAsState()
    val error by chatViewModel.error.collectAsState()
    val mediaList by chatViewModel.chatMedia.collectAsState()

    val creatorId = communityDetails?.creatorId

    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(communityId) {
        communityViewModel.fetchCommunityDetail(communityId)
        chatViewModel.fetchCommunityMembers(communityId)
        chatViewModel.observeMessages(communityId)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                        .zIndex(0f)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { navController.navigate("community_screen/$communityId") }
                    ) {
                        AsyncImage(
                            model = communityImage,
                            contentDescription = "Community",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = communityName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${members.size}" + stringResource(id = R.string.members),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        thickness = 1.dp
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.shareMedia),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (mediaList.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(mediaList) { imageUrl ->
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Chat media",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { previewImageUrl = imageUrl },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(id = R.string.noSharedMedia),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(members) { member ->
                            val isCreator = member.userId == creatorId
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(
                                            if (member.userId == chatViewModel.currentUserId) {
                                                Screen.Profile.route
                                            } else {
                                                Screen.UserAccount.createRoute(userId = member.userId)
                                            }
                                        )
                                    }

                            ) {
                                AsyncImage(
                                    model = member.image,
                                    contentDescription = member.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(12.dp))

                                Text(
                                    text = member.name ?: "Unknown",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End) {
                                    if (isCreator) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.admin),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    if (previewImageUrl != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { previewImageUrl = null }
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = previewImageUrl,
                contentDescription = "Full Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}