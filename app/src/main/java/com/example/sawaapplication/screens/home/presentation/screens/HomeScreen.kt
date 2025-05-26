package com.example.sawaapplication.screens.home.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sawaapplication.R
import com.example.sawaapplication.navigation.Screen
import com.example.sawaapplication.screens.event.presentation.screens.formatDateString
import com.example.sawaapplication.screens.event.presentation.screens.formatTimestampToTimeString
import com.example.sawaapplication.screens.event.presentation.vmModels.EventViewModel
import com.example.sawaapplication.screens.home.presentation.screens.component.CustomTabRow
import com.example.sawaapplication.screens.home.presentation.screens.component.EventCard
import com.example.sawaapplication.screens.home.presentation.screens.component.PostCard
import com.example.sawaapplication.screens.home.presentation.vmModels.EventFilterType
import com.example.sawaapplication.screens.home.presentation.vmModels.HomeViewModel
import com.example.sawaapplication.screens.notification.presentation.viewmodels.NotificationViewModel
import com.example.sawaapplication.ui.screenComponent.CustomConfirmationDialog
import com.example.sawaapplication.utils.getCityNameFromGeoPoint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.reporting.MessagingClientEvent
import java.net.URLEncoder

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.posts), stringResource(R.string.events))
    var eventToDelete by remember { mutableStateOf<MessagingClientEvent.Event?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        when (selectedTabIndex) {
            0 -> PostsTab(viewModel, navController)
            1 -> MyEventsTab(navController = navController) // implement if needed
        }

        // Top transparent tab row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.TopCenter)
                .zIndex(1f)
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
        ) {
            CustomTabRow(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )
        }
    }
}

@Composable
fun PostsTab(viewModel: HomeViewModel, navController: NavController) {
    val posts by viewModel.posts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val communityNames by viewModel.communityNames.collectAsState()
    val userDetails by viewModel.userDetails.collectAsState()
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    val postLikedUserId = viewModel.postLikedEvent.collectAsState().value

    // Trigger a notification whenever a post is liked by a user
    LaunchedEffect(postLikedUserId) {
        postLikedUserId?.let { likedUserId ->
            val post = posts.find { it.userId == likedUserId }
            post?.let {
                notificationViewModel.notifyLike(it)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllPosts()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

            error != null -> Text(
                text = error ?: stringResource(R.string.unknownError),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )

            else ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = integerResource(R.integer.lazyColumnPaddingTop).dp,
                        start = integerResource(R.integer.lazyColumnPaddingStartEnd).dp,
                        end = integerResource(R.integer.lazyColumnPaddingStartEnd).dp,
                        bottom = integerResource(R.integer.lazyColumnPaddingButton).dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(integerResource(R.integer.lazyColumnArrangement).dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts) { post ->
                        val communityName =
                            communityNames[post.communityId] ?: stringResource(R.string.unknown)
                        val (userName, userImage) = userDetails[post.userId]
                            ?: (stringResource(R.string.unknown) to "")
                        PostCard(
                            post,
                            communityName,
                            communityId = post.communityId,
                            userName,
                            userImage,
                            onClick = {  // ✅ FIXED: No parameters, get image from post object
                                val imageUrl = post.imageUri // or post.postImageUrl - check your Post model
                                if (!imageUrl.isNullOrEmpty()) {
                                    val encoded = URLEncoder.encode(imageUrl, "utf-8")
                                    navController.navigate(Screen.FullscreenImage.createRoute(encoded))
                                }
                            },
                            onLikeClick = {
                                viewModel.likePost(post)
                                notificationViewModel.notifyLike(post)
                            },
                            onDeleteClick = {
                                viewModel.deletePost(post)
                            },
                            navController = navController,
                            onUserImageClick = { viewModel.likePost(post) },
                            onCommunityClick = { communityId ->
                                navController.navigate("community_screen/$communityId")
                            }
                        )

                        HorizontalDivider(
                            thickness = integerResource(R.integer.lazyColumnHorizontalDividerThickness).dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = integerResource(R.integer.smallerSpace).dp)
                        )
                    }
                }
        }
    }
}

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

    // Fetch community names

    var showLeaveEventDialog by remember { mutableStateOf(false) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var selectedCommunityId by remember { mutableStateOf<String?>(null) }
    val communityNames by viewModel.communityNames.collectAsState() // fetch community names


    var showDeleteEventDialog by remember { mutableStateOf(false) }
    var deleteEventId by remember { mutableStateOf<String?>(null) }
    var deleteCommunityId by remember { mutableStateOf<String?>(null) }
    val filteredList = viewModel.filteredEvents
    var isFilterMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchJoinedEvents()
    }

    // Refresh the list after a successful cancel
    LaunchedEffect(joinResult) {
        if (joinResult?.isSuccess == true) {
            viewModel.fetchJoinedEvents()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
                .zIndex(1f)
        ) {
            IconButton(onClick = { isFilterMenuExpanded = true }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }

            DropdownMenu(
                expanded = isFilterMenuExpanded,
                onDismissRequest = { isFilterMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Events") },
                    onClick = {
                        viewModel.setFilter(EventFilterType.DEFAULT)
                        isFilterMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Join Events") },
                    onClick = {
                        viewModel.setFilter(EventFilterType.Still)
                        isFilterMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Finished Events") },
                    onClick = {
                        viewModel.setFilter(EventFilterType.Fineshed)
                        isFilterMenuExpanded = false
                    }
                )
            }
        }
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

            events.isEmpty() -> Text(
                "No joined events",
                modifier = Modifier.align(Alignment.Center)
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 72.dp, bottom = 56.dp)
            ) {
                items(filteredList) { event ->
                    val communityName = communityNames[event.communityId] ?: "Unknown Community"
                    val timeFormatted = event.time?.let { formatTimestampToTimeString(it) } ?: "No time set"
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
                        joined = event.joinedUsers.contains(userId),
                        onJoinClick = {
                            selectedEventId = event.id
                            selectedCommunityId = event.communityId
                            showLeaveEventDialog = true
                        },
                        showCancelButton = true,
                        onClick = {
                            navController.navigate("event_detail/${event.communityId}/${event.id}")
                        },
                        eventTimestamp = event.time, // ← Just a comma here
                        onEditClick = {
                            navController.navigate("edit_event/${event.communityId}/${event.id}")
                        },
                        onDeleteClick = {
                            deleteEventId = event.id
                            deleteCommunityId = event.communityId
                            showDeleteEventDialog = true
                        }
                    )
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
