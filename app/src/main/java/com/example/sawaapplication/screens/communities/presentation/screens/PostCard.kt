package com.example.sawaapplication.screens.communities.presentation.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sawaapplication.R
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.post.domain.model.PostUiModel
import com.example.sawaapplication.ui.theme.black
import com.example.sawaapplication.ui.theme.white
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostCard(
    post: PostUiModel,
    currentUserId: String,
    onImageClick: (String) -> Unit,
    onLikeClick: (PostUiModel) -> Unit,
    canLike: Boolean = true,
    navController: NavController
) {
    val isLiked = currentUserId in post.likedBy

    val formattedDate = remember(post.createdAt) {
        formatPostDate(post.createdAt)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(integerResource(R.integer.cardRoundedCornerShape).dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(integerResource(R.integer.postCardElevation).dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // User row
            UserHeaderSection(
                post = post,
                currentUserId = currentUserId,
                formattedDate = formattedDate,
                navController = navController
            )

            Spacer(Modifier.height(8.dp))

            // Post content
            PostContentSection(
                post = post,
                onImageClick = onImageClick
            )

            // Likes section
            LikesSection(
                post = post,
                isLiked = isLiked,
                canLike = canLike,
                onLikeClick = onLikeClick
            )
        }
    }
}

@Composable
private fun UserHeaderSection(
    post: PostUiModel,
    currentUserId: String,
    formattedDate: String,
    navController: NavController
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (post.userAvatarUrl.isNotBlank()) {
            AsyncImage(
                model = post.userAvatarUrl,
                contentDescription = "User Profile Image",
                modifier = Modifier
                    .size(integerResource(R.integer.asyncImageSize).dp)
                    .clip(CircleShape)
                    .clickable {
                        if(post.userId == currentUserId){
                            navController.navigate(Screen.Profile.route)
                        }else{
                            navController.navigate(Screen.UserAccount.createRoute(userId = post.userId))
                        }
                    },
            )
        }
        Spacer(modifier = Modifier.width(integerResource(R.integer.smallerSpace).dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = post.username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.UserAccount.createRoute(userId = post.userId))
                }
            )
            Text(
                text = "${stringResource(R.string.postedOn)} $formattedDate",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun PostContentSection(
    post: PostUiModel,
    onImageClick: (String) -> Unit
) {
    // Post text content
    if (post.content.isNotBlank()) {
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
    }

    // Post image
    if (post.postImageUrl.isNotBlank()) {
        AsyncImage(
            model = post.postImageUrl,
            contentDescription = "Post image",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onImageClick(post.postImageUrl) },
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LikesSection(
    post: PostUiModel,
    isLiked: Boolean,
    canLike: Boolean,
    onLikeClick: (PostUiModel) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (canLike) {
            // Show like button for community members
            IconButton(onClick = { onLikeClick(post) }) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    tint = if (isLiked) Color.Red else Color.Gray
                )
            }
        } else {
            // Show locked icon for non-members
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Join community to like",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Join to like",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        // Always show like count
        Text(
            text = if (post.likes == 1) "1 Like" else "${post.likes} Likes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Helper function extracted for better readability
private fun formatPostDate(createdAt: String): String {
    return try {
        val parser = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val date = parser.parse(createdAt)
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        "Unknown date"
    }
}