package com.example.sawaapplication.screens.communities.presentation.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.post.domain.model.PostUiModel
import com.example.sawaapplication.ui.theme.Gray
import com.example.sawaapplication.ui.theme.black
import com.example.sawaapplication.ui.theme.white

@Composable
fun PostCard(post: PostUiModel,  onImageClick: (String) -> Unit) {
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(21) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = white),
        elevation = CardDefaults.cardElevation(integerResource(R.integer.postCardElevation).dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 1) Avatar + Username
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.userAvatarUrl,
                    contentDescription = "${post.username} avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = post.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = black
                )
            }

            Spacer(Modifier.height(8.dp))

            // 2) Text content (if any)
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = black,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }
            // 3) Image (if any)
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


            // 4) Like button + count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    isLiked = !isLiked
                    likeCount += if (isLiked) 1 else -1
                }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.error else Gray
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "$likeCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray
                )
            }
        }
    }
}
