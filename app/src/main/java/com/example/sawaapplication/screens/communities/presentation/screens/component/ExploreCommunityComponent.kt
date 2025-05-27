package com.example.sawaapplication.screens.communities.presentation.screens.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.sawaapplication.screens.communities.domain.model.Community

@Composable
fun ExploreCommunityCardList(
    communities: List<Community>,
    currentUserId: String,
    listState: LazyListState,
    onCommunityClick: (String) -> Unit,
    onJoinClick: (String) -> Unit,
    onLeaveClick: (String) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()) {
        items(communities, key = { it.id }) { community ->
            val isJoined = currentUserId in community.members

            CommunityCard(
                communityName = community.name,
                communityMember = community.members.size,
                communityImage = rememberAsyncImagePainter(community.image),
                communityCategory = community.category,
                joinButton = {
                    JoinButton(
                        isJoined = isJoined,
                        onClick = {
                            if (isJoined) onLeaveClick(community.id)
                            else onJoinClick(community.id)
                        }
                    )
                },
                onClick = {
                    onCommunityClick(community.id)
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 4.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun CommunityCard(
    communityName: String,
    communityMember : Int,
    communityImage: Painter,
    communityCategory: String,
    joinButton : @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Community Image
            Image(
                painter = communityImage,
                contentDescription = "Community page",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(20.dp))

            // Community Content (center)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = communityName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${communityMember} Members",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = communityCategory,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)

                )

            }
            joinButton()
        }

    }

}

@Composable
fun JoinButton(
    isJoined: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isJoined) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary, // Orange
            contentColor = if (isJoined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        border = if (isJoined) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(60),
        contentPadding = PaddingValues(horizontal = 8.dp),
        elevation = null
    ) {
        Icon(
            imageVector = if (isJoined) Icons.Default.CheckCircle else Icons.Default.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isJoined) "Joined" else "Join",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

