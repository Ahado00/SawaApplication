package com.example.sawaapplication.screens.chat.presentation.screens.chatComponent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.chat.domain.model.ChatUserInfo

@Composable
fun ChatMembersHeader(
    communityId: String,
    communityName: String,
    members: List<ChatUserInfo>,
    onClick: () -> Unit,
    onNavigateToCommunity: (String) -> Unit
) {
    Text(
        text = communityName,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onNavigateToCommunity(communityId) }
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy((-12).dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(members.take(5)) { member ->
            AsyncImage(
                model = member.image,
                contentDescription = member.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .clickable { onClick() },
                contentScale = ContentScale.Crop
            )
        }

        item {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .clickable { onClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${members.size}"+ stringResource(id = R.string.members),
                    color = MaterialTheme.colorScheme.background,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
}