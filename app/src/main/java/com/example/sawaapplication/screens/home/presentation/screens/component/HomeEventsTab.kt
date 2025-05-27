package com.example.sawaapplication.screens.home.presentation.screens.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.event.presentation.screens.formatDateString
import com.example.sawaapplication.screens.event.presentation.screens.formatTimestampToTimeString
import com.example.sawaapplication.screens.event.presentation.vmModels.EventViewModel
import com.example.sawaapplication.screens.home.presentation.vmModels.EventFilterType
import com.example.sawaapplication.screens.home.presentation.vmModels.HomeViewModel
import com.example.sawaapplication.ui.screenComponent.CustomConfirmationDialog
import com.example.sawaapplication.ui.screenComponent.EventCard
import com.example.sawaapplication.utils.getCityNameFromGeoPoint
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MyEventsTab(
    viewModel: HomeViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel(),
    navController: NavController
) {
    val events by viewModel.joinedEvents.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val joinResult by eventViewModel.joinResult.collectAsState()

    var showLeaveEventDialog by remember { mutableStateOf(false) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var selectedCommunityId by remember { mutableStateOf<String?>(null) }
    val communityNames by viewModel.communityNames.collectAsState() // fetch community names

    var showDeleteEventDialog by remember { mutableStateOf(false) }
    var deleteEventId by remember { mutableStateOf<String?>(null) }
    var deleteCommunityId by remember { mutableStateOf<String?>(null) }
    val filteredList = viewModel.filteredEvents

    val currentFilter by viewModel.selectedFilter.collectAsState()
    val filterItems = listOf(
        FilterChipItem(EventFilterType.DEFAULT, R.string.allEvents),
        FilterChipItem(EventFilterType.Still, R.string.joinedEvents),
        FilterChipItem(EventFilterType.Fineshed, R.string.finishedEvents),
    )

    LaunchedEffect(Unit) {
        viewModel.fetchJoinedEvents()
    }

    // Refresh the list after a successful cancel
    LaunchedEffect(joinResult) {
        if (joinResult?.isSuccess == true) {
            viewModel.fetchJoinedEvents()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        EventFilterChips(
            currentFilter = currentFilter,
            onFilterSelected = viewModel::setFilter,
            chips = filterItems,
            modifier = Modifier
                .padding(horizontal = integerResource(id = R.integer.mediumSpace).dp)
                .padding(top = integerResource(id = R.integer.homeEventTopPadding).dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                events.isEmpty() -> Text(
                    "No joined events",
                    modifier = Modifier.align(Alignment.Center)
                )


                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(filteredList) { event ->
                        val communityName = communityNames[event.communityId] ?: "Unknown Community"
                        val timeFormatted =
                            event.time?.let { formatTimestampToTimeString(it) } ?: "No time set"
                        val formattedDate = formatDateString(event.date)
                        val context = LocalContext.current

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
                            isEditable = event.createdBy == userId,
                            onJoinClick = {
                                selectedEventId = event.id
                                selectedCommunityId = event.communityId
                                showLeaveEventDialog = true
                            },
                            showCancelButton = true,
                            onClick = {
                                navController.navigate("event_detail/${event.communityId}/${event.id}")
                            },
                            eventTimestamp = event.time,
                            onEditClick = {
                                navController.navigate("edit_event/${event.communityId}/${event.id}")
                            },
                            onDeleteClick = {
                                deleteEventId = event.id
                                deleteCommunityId = event.communityId
                                showDeleteEventDialog = true
                            },
                            onCommunityClick = { communityId ->
                                navController.navigate("community_screen/$communityId")
                            },
                            communityId = event.communityId,
                        )
                    }
                }
            }
        }
        if (showDeleteEventDialog && deleteEventId != null && deleteCommunityId != null) {
            CustomConfirmationDialog(
                message = stringResource(R.string.`areYouSureُEventHome`),
                onConfirm = {
                    eventViewModel.deleteEvent(deleteCommunityId!!, deleteEventId!!)
                    showDeleteEventDialog = false
                    deleteEventId = null
                    deleteCommunityId = null
                },
                onDismiss = {
                    showDeleteEventDialog = false
                    deleteEventId = null
                    deleteCommunityId = null
                }
            )
        }

        if (showDeleteEventDialog && deleteEventId != null && deleteCommunityId != null) {
            CustomConfirmationDialog(
                message = stringResource(R.string.`areYouSureُEventHome`), // or hardcode it if not in strings.xml
                onConfirm = {
                    eventViewModel.deleteEvent(deleteCommunityId!!, deleteEventId!!)
                    showDeleteEventDialog = false
                    deleteEventId = null
                    deleteCommunityId = null
                },
                onDismiss = {
                    showDeleteEventDialog = false
                    deleteEventId = null
                    deleteCommunityId = null
                }
            )
        }

        //Dialog for confirm leaving an event
        if (showLeaveEventDialog && selectedEventId != null && selectedCommunityId != null) {
            CustomConfirmationDialog(
                message = stringResource(R.string.areYouSureEvent),
                onConfirm = {
                    eventViewModel.leaveEvent(
                        communityId = selectedCommunityId!!,
                        eventId = selectedEventId!!,
                        userId = userId
                    )
                    showLeaveEventDialog = false
                    selectedEventId = null
                    selectedCommunityId = null
                },
                onDismiss = {
                    showLeaveEventDialog = false
                    selectedEventId = null
                    selectedCommunityId = null
                }
            )
        }
    }
}
