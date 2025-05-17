package com.example.sawaapplication.screens.home.presentation.screens.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.post.domain.model.Post
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostCard(
    post: Post,
    communityName: String = "",
    userName: String = "",
    userImage: String,
    onClick: () -> Unit,
    onLikeClick: (Post) -> Unit,
    onUserImageClick: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // State to track if the post is liked

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var isLiked by remember(post.likedBy) {
        mutableStateOf(currentUserId != null && currentUserId in post.likedBy)
    }


    // Set icon color based on like state
    val likeIconColor = if (isLiked) Color.Red else Color.Gray

    val formattedDate = remember(post.createdAt) {
        try {
            val parser = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
            val date = parser.parse(post.createdAt)
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            formatter.format(date ?: Date())
        } catch (e: Exception) {
            "Unknown date"
        }
    }

    Card(
        shape = RoundedCornerShape(integerResource(R.integer.cardRoundedCornerShape).dp),
        elevation = CardDefaults.cardElevation(integerResource(R.integer.cardElevation).dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = integerResource(R.integer.cardHorizontalPadding).dp)
    ) {
        Column(modifier = Modifier.padding(integerResource(R.integer.padding).dp)) {

            Box(
                modifier = Modifier
                    .padding(start = integerResource(R.integer.smallerSpace).dp)
                    .border(
                        width = integerResource(R.integer.cardBorder).dp,
                        color = Color.Gray,
                        shape = RoundedCornerShape(integerResource(R.integer.boxRoundedCornerShape).dp)
                    )
                    .padding(
                        horizontal = integerResource(R.integer.smallerSpace).dp,
                        vertical = integerResource(R.integer.extraSmallSpace).dp
                    )
            ) {
                Text(
                    text = communityName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(integerResource(R.integer.smallerSpace).dp))

            // Row with user image + name + date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (userImage.isNotBlank()) {
                    AsyncImage(
                        model = userImage,
                        contentDescription = "User Profile Image",
                        modifier = Modifier
                            .size(integerResource(R.integer.asyncImageSize).dp)
                            .clip(CircleShape)
                            .clickable {
                                navController.navigate(Screen.UserAccount.createRoute(userId = post.userId))
                            }

                    )
                }
                Spacer(modifier = Modifier.width(integerResource(R.integer.smallerSpace).dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
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

            Spacer(modifier = Modifier.height(integerResource(R.integer.smallSpace).dp))

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge
            )

            // Post image (optional)
            if (post.imageUri.isNotBlank()) {
                Spacer(modifier = Modifier.height(integerResource(R.integer.smallSpace).dp))
                AsyncImage(
                    model = post.imageUri,
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(integerResource(R.integer.postAsyncImageSize).dp)
                        .clip(RoundedCornerShape(integerResource(R.integer.postImageRoundedCornerShape).dp))
                )
            }

            Spacer(modifier = Modifier.height(integerResource(R.integer.smallSpace).dp))

            // Likes count
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${post.likes} ${stringResource(R.string.like)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = integerResource(R.integer.smallerSpace).dp)
                )

                IconButton(onClick = {
                    // Toggle the like status
                    isLiked = !isLiked
                    onLikeClick(post) // Notify the viewModel to update the like count in FireStore
                }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = likeIconColor // Change the tint based on like state
                    )
                }
            }
        }
    }
}