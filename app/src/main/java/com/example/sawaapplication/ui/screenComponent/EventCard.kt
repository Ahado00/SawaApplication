package com.example.sawaapplication.ui.screenComponent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sawaapplication.R
import com.example.sawaapplication.ui.theme.errorColor
import com.example.sawaapplication.ui.theme.lightGray
import com.example.sawaapplication.ui.theme.thirdOrange
import com.google.firebase.Timestamp

@Composable
fun EventCard(
    image: String,
    community: String,
    title: String,
    description: String,
    location: String,
    time: String,
    date: String,
    participants: Int,
    joinedUsers: List<String>,
    onJoinClick: () -> Unit,
    showCancelButton: Boolean = false,
    eventTimestamp: Timestamp?,
    isEditable: Boolean = false,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onCommunityClick: ((String) -> Unit)? = null,
    communityId: String,

    ) {
    val joinedCount = joinedUsers.size

    fun isEventExpired(eventTime: Timestamp): Boolean {
        val eventMillis = eventTime.toDate().time
        val expiryMillis = eventMillis + 60 * 60 * 1000 // Add 1 hour
        val currentMillis = System.currentTimeMillis()

        return currentMillis > expiryMillis
    }

    fun isEventFull(participantsLimit: Int, currentJoined: Int): Boolean {
        return currentJoined >= participantsLimit
    }

    val isFull = isEventFull(participants, joinedUsers.size)

    val isExpired = remember(eventTimestamp) {
        eventTimestamp?.let { isEventExpired(it) } ?: false
    }

    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = integerResource(id = R.integer.smallerSpace).dp,
                horizontal = integerResource(id = R.integer.smallerSpace).dp
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(integerResource(id = R.integer.cardRoundedCornerShape).dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column {
            //Top image
            Box {
                AsyncImage(
                    model = image,
                    contentDescription = "Event image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(integerResource(id = R.integer.eventImageHeight).dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = integerResource(id = R.integer.cardRoundedCornerShape).dp,
                                topEnd = integerResource(id = R.integer.cardRoundedCornerShape).dp
                            )
                        )
                )
                if (isExpired) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(integerResource(id = R.integer.smallerSpace).dp),
                        containerColor = errorColor,
                    ) {
                        Text(
                            stringResource(id = R.string.expired),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                } else if (isFull) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(integerResource(id = R.integer.smallerSpace).dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            stringResource(id = R.string.full),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(integerResource(id = R.integer.smallerSpace).dp))

            Column(Modifier.padding(horizontal = integerResource(id = R.integer.mediumSpace).dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //Community tag
                    AssistChip(
                        onClick = { onCommunityClick?.invoke(communityId) },
                        label = { Text(community) },
                        leadingIcon = { Icon(Icons.Outlined.Group, contentDescription = null) },
                        shape = RoundedCornerShape(integerResource(id = R.integer.RoundedCornerShape).dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                    )
                    if (isEditable) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.edit)) },
                                    onClick = {
                                        menuExpanded = false
                                        onEditClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(id = R.string.delete),
                                            color = Color.Red
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onDeleteClick()
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(integerResource(id = R.integer.smallerSpace).dp))

                // Title & description
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(integerResource(id = R.integer.extraSmallSpace).dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(integerResource(id = R.integer.smallSpace).dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // location & time
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(integerResource(id = R.integer.mediumSpace).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfoPill(
                            icon = Icons.Outlined.LocationOn,
                            text = location,
                            maxTextWidth = integerResource(id = R.integer.eventInfoPillWidth).dp
                        )
                        InfoPill(
                            icon = Icons.Outlined.Timer,
                            text = "$date • $time",
                        )
                    }
                }

                Spacer(Modifier.height(integerResource(id = R.integer.smallSpace).dp))

                //join/leave button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    JoinButton(
                        joined = joinedCount > 0 && !isExpired && !isFull,
                        onJoinClick = onJoinClick,
                        showCancel = showCancelButton,
                        isExpired = isExpired,
                        isFull = isFull
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Participants count
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Participants",
                            tint = if (joinedUsers.size >= participants) thirdOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(integerResource(id = R.integer.homeScreenIconSize).dp)
                        )
                        Spacer(modifier = Modifier.width(integerResource(id = R.integer.extraSmallSpace).dp))

                        Text(
                            text = "$participants/${joinedUsers.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (joinedUsers.size >= participants) thirdOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(integerResource(id = R.integer.smallerSpace).dp))
            }
        }
    }
}

@Composable
private fun InfoPill(
    icon: ImageVector,
    text: String,
    maxTextWidth: Dp = Dp.Unspecified,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(integerResource(id = R.integer.cardRoundedCornerShape).dp)
            )
            .padding(
                horizontal = integerResource(id = R.integer.smallerSpace).dp,
                vertical = integerResource(id = R.integer.extraSmallSpace).dp
            )
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(integerResource(id = R.integer.eventInfoPillIcon).dp)
        )
        Spacer(Modifier.width(integerResource(id = R.integer.extraSmallSpace).dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = if (maxTextWidth != Dp.Unspecified)
                Modifier.widthIn(max = maxTextWidth)
            else
                Modifier
        )
    }
}

@Composable
fun JoinButton(
    joined: Boolean,
    onJoinClick: () -> Unit,
    showCancel: Boolean = false,
    isExpired: Boolean,
    isFull: Boolean
) {
    val (text, enabled, colors) = when {
        isExpired -> Triple(
            stringResource(id = R.string.finish),
            false,
            ButtonDefaults.buttonColors(containerColor = Color.Gray)
        )

        isFull -> Triple("Full", false, ButtonDefaults.buttonColors(containerColor = Color.Gray))
        showCancel && joined -> Triple(
            stringResource(id = R.string.leave),
            true,
            ButtonDefaults.buttonColors(containerColor = LightGray, contentColor = Color.Black),
        )

        else -> Triple(
            stringResource(id = R.string.join),
            true,
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        )
    }

    Button(
        onClick = onJoinClick,
        enabled = enabled,
        shape = RoundedCornerShape(integerResource(id = R.integer.smallRoundCorner).dp),
        colors = colors,
        modifier = Modifier.defaultMinSize(minHeight = integerResource(id = R.integer.eventButtonHeight).dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium)
    }
}
