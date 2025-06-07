package com.example.sawaapplication.screens.home.presentation.screens.component

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.home.presentation.vmModels.HomeViewModel
import com.example.sawaapplication.screens.post.domain.model.Comment
import com.example.sawaapplication.screens.post.domain.model.Post
import com.example.sawaapplication.ui.screenComponent.CustomConfirmationDialog
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostCard(
    post: Post,
    communityName: String = "",
    communityId: String,
    userName: String = "",
    userImage: String,
    onClick: () -> Unit,
    onLikeClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
    onCommunityClick: ((String) -> Unit)? = null,
    onCommentClick: (Post) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var isLiked by remember(post.likedBy) {
        mutableStateOf(currentUserId != null && currentUserId in post.likedBy)
    }
    val isOwnedByCurrentUser = currentUserId == post.userId
    var showDeleteDialog by remember { mutableStateOf(false) }
    val likeIconColor = if (isLiked) Color.Red else Color.Gray
    var commentText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Get the docId for this post
    val docId = viewModel.postDocumentIds.collectAsState().value[post] ?: ""

    // Always fetch comments when the PostCard appears
    LaunchedEffect(docId) {
        if (docId.isNotBlank()) {
            viewModel.fetchComments(post.communityId, docId)
        }
    }

    // Observe comments for this post
    val postComments = viewModel.postComments.collectAsState().value[docId] ?: emptyList()

    // Get user details map (userId -> Pair(name, image))
    val userDetailsMap = viewModel.userDetails.collectAsState().value

    val submitComment: () -> Unit = {
        if (commentText.isNotBlank() && currentUserId?.isNotBlank() == true) {
            val comment = Comment(
                postCreatorId = post.userId,
                commentedById = currentUserId,
                commentContent = commentText,
                createdAt = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                    .format(Date())
            )

            if (docId.isNotBlank()) {
                viewModel.addComment(post.communityId, docId, comment)
                commentText = ""
                Toast.makeText(context, "Comment added", Toast.LENGTH_SHORT).show()
                // Refresh comments immediately
                viewModel.fetchComments(post.communityId, docId)
            }
        }
    }

    val formattedDate = remember(post.createdAt) {
        runCatching {
            val parser = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
            val date = parser.parse(post.createdAt)
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(date ?: Date())
        }.getOrDefault("Unknown date")
    }

    Card(
        shape = RoundedCornerShape(integerResource(R.integer.cardRoundedCornerShape).dp),
        elevation = CardDefaults.cardElevation(integerResource(R.integer.cardElevation).dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = integerResource(R.integer.cardHorizontalPadding).dp)
            .border(
                width = 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(integerResource(R.integer.cardRoundedCornerShape).dp)
            )
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(integerResource(id = R.integer.smallSpace).dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Community tag
                Box(
                    modifier = Modifier
                        .padding(start = integerResource(R.integer.smallerSpace).dp)
                        .border(
                            width = integerResource(R.integer.cardBorder).dp,
                            color = MaterialTheme.colorScheme.onTertiary,
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
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.clickable {
                            onCommunityClick?.invoke(communityId)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(integerResource(R.integer.smallerSpace).dp))

                // User info row
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
                                    if (post.userId == currentUserId) {
                                        navController.navigate(Screen.Profile.route)
                                    } else {
                                        navController.navigate(Screen.UserAccount.createRoute(userId = post.userId))
                                    }
                                },
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(integerResource(R.integer.smallerSpace).dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                if (post.userId == currentUserId) {
                                    navController.navigate(Screen.Profile.route)
                                } else {
                                    navController.navigate(Screen.UserAccount.createRoute(userId = post.userId))
                                }
                            }
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(integerResource(R.integer.smallSpace).dp))

                // Post content
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = integerResource(id = R.integer.smallerSpace).dp)
                )

                // Optional post image
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
            }

            // Likes & actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = integerResource(R.integer.smallSpace).dp)
                ) {
                    // like button
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = likeIconColor,
                        modifier = Modifier
                            .size(integerResource(R.integer.iconSize).dp)
                            .clickable {
                                isLiked = !isLiked
                                onLikeClick(post)
                            }
                    )

                    Spacer(modifier = Modifier.width(integerResource(R.integer.smallerSpace).dp))

                    Text(
                        text = "${post.likes} ${stringResource(R.string.like)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Comment icon (no toggle, just for looks now)
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Comment,
                            contentDescription = "Comment on Post",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(integerResource(R.integer.iconSize).dp)
                        )
                    }
                    // Show delete if the post is owned by user
                    if (isOwnedByCurrentUser) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Post",
                                tint = Color.Gray,
                                modifier = Modifier.size(integerResource(R.integer.iconSize).dp)
                            )
                        }
                    }
                }
            }

            // Always show comment input and comment list below each post
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = integerResource(R.integer.cardHorizontalPadding).dp)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    placeholder = {
                        Text(text = stringResource(R.string.comment))
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        IconButton(onClick = submitComment) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send comment",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Comments list, always visible
                if (postComments.isNotEmpty()) {
                    HorizontalDivider()
                    Text(
                        text = "Comments:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    postComments.forEach { comment ->
                        val (commentUserName, commentUserImage) = userDetailsMap[comment.commentedById]
                            ?: (stringResource(R.string.unknown) to "")
                        CommentItem(
                            comment = comment,
                            userName = commentUserName,
                            userImage = commentUserImage,
                            navController = navController
                        )
                    }
                } else {
                    Text(
                        text = "No comments yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        if (showDeleteDialog) {
            CustomConfirmationDialog(
                message = stringResource(R.string.areYouSurePost),
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    showDeleteDialog = false
                    onDeleteClick(post)
                }
            )
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    userName: String,
    userImage: String,
    navController: NavController
) {

    // Format the date as "04 June 2025, 05:25"
    val formattedDate = remember(comment.createdAt) {
        runCatching {
            val parser = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val date = parser.parse(comment.createdAt)
            SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.ENGLISH).format(date ?: Date())
        }.getOrDefault(comment.createdAt)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (userImage.isNotBlank()) {
            AsyncImage(
                model = userImage,
                contentDescription = "User Profile Image",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable {
                        navController.navigate(Screen.UserAccount.createRoute(userId = comment.commentedById))
                    },
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = comment.commentContent,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}