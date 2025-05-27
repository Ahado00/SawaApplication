package com.example.sawaapplication.screens.communities.presentation.screens.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sawaapplication.screens.event.presentation.screens.formatDateString
import com.example.sawaapplication.screens.event.presentation.screens.formatTimestampToTimeString
import com.example.sawaapplication.ui.screenComponent.EventCard
import com.example.sawaapplication.utils.getCityNameFromGeoPoint


@Composable
fun CommunityEventCard(
    event: com.example.sawaapplication.screens.event.domain.model.Event,
    communityName: String,
    currentUserId: String,
    navController: NavHostController,
    communityId: String,
    onJoinEvent: () -> Unit,
    onLeaveEvent: () -> Unit,
    onEditEvent: () -> Unit,
    onDeleteEvent: () -> Unit
) {
    val context = LocalContext.current
    val timeFormatted = event.time?.let { formatTimestampToTimeString(it) } ?: "No time set"
    val formattedDate = formatDateString(event.date)

    EventCard(
        image = event.imageUri,
        title = event.title,
        description = event.description,
        location = context.getCityNameFromGeoPoint(event.location),
        participants = event.memberLimit,
        joinedUsers = event.joinedUsers,
        community = communityName,
        time = timeFormatted,
        date = formattedDate,
        isEditable = event.createdBy == currentUserId,
        onEditClick = onEditEvent,
        onDeleteClick = onDeleteEvent,
        onJoinClick = {
            if (event.joinedUsers.contains(currentUserId)) {
                onLeaveEvent()
            } else {
                onJoinEvent()
            }
        },
        showCancelButton = true,
        modifier = Modifier.padding(4.dp),
        eventTimestamp = event.time,
        onClick = { navController.navigate("event_detail/$communityId/${event.id}") },
        onCommunityClick = { communityId ->
            navController.navigate("community_screen/$communityId")
        },
        communityId = event.communityId,
    )
}
