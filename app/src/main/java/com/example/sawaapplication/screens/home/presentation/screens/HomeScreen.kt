package com.example.sawaapplication.screens.home.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.sawaapplication.screens.event.presentation.screens.EventFilterChips
import com.example.sawaapplication.screens.event.presentation.screens.FilterChipItem
import com.example.sawaapplication.screens.event.presentation.screens.formatDateString
import com.example.sawaapplication.screens.event.presentation.screens.formatTimestampToTimeString
import com.example.sawaapplication.screens.event.presentation.vmModels.EventViewModel
import com.example.sawaapplication.screens.home.domain.model.EventFilterType
import com.example.sawaapplication.screens.home.presentation.screens.component.CustomTabRow
import com.example.sawaapplication.screens.home.presentation.screens.component.EventCard
import com.example.sawaapplication.screens.home.presentation.screens.component.PostCard
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
            1 -> MyEventsTab(navController = navController)
        }

        // Top transparent tab row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.TopCenter)
                .zIndex(1f)
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
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
    val context = LocalContext.current

    val posts by viewModel.posts.collectAsState()
    val postLikedUserId = viewModel.postLikedEvent.collectAsState().value

    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val communityNames by viewModel.communityNames.collectAsState()
    val userDetails by viewModel.userDetails.collectAsState()

    val notificationViewModel: NotificationViewModel = hiltViewModel()

    val deleteResult by viewModel.deletePostResult.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllPostsOnce()
    }

    // Trigger a notification whenever a post is liked by a user
    LaunchedEffect(postLikedUserId) {
        postLikedUserId?.let { likedUserId ->
            val post = posts.find { it.userId == likedUserId }
            post?.let {
                notificationViewModel.notifyLike(it)
            }
        }
    }

    LaunchedEffect(deleteResult) {
        deleteResult?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show()
            }
            viewModel.clearDeletePostResult()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

            error != null -> Text(
                text = error ?: stringResource(R.string.unknownError),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
            posts.isEmpty() -> Text(
                text = stringResource(R.string.no_posts_available),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )

            else ->
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = integerResource(R.integer.lazyColumnPaddingTop).dp,
                        start = integerResource(R.integer.extraSmallSpace).dp,
                        end = integerResource(R.integer.extraSmallSpace).dp,
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
                            onClick = {
                                val imageUrl = post.imageUri
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
                            onCommunityClick = { communityId ->
                                navController.navigate("community_screen/$communityId")
                            },
                            onCommentClick = {
                                navController.navigate("post_comments/${post.id}")
                            }
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
    val context = LocalContext.current

    val events by viewModel.joinedEvents.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val joinResult by eventViewModel.joinResult.collectAsState()
    val deleteResult by eventViewModel.deleteResult.collectAsState()

    // Fetch community names
    var showLeaveEventDialog by remember { mutableStateOf(false) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var selectedCommunityId by remember { mutableStateOf<String?>(null) }
    val communityNames by viewModel.communityNames.collectAsState() // fetch community names

    var showDeleteEventDialog by remember { mutableStateOf(false) }
    var deleteEventId by remember { mutableStateOf<String?>(null) }
    var deleteCommunityId by remember { mutableStateOf<String?>(null) }

    val currentFilter by viewModel.selectedFilter.collectAsState()
    val filteredList = viewModel.filteredEvents

    val filterItems = listOf(
        FilterChipItem(EventFilterType.DEFAULT, R.string.allEvents),
        FilterChipItem(EventFilterType.Still, R.string.joinedEvents),
        FilterChipItem(EventFilterType.Finished, R.string.finishedEvents),
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

    // Show a toast when delete event
    LaunchedEffect(deleteResult) {
        deleteResult?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show()
                viewModel.fetchJoinedEvents()
            } else {
                Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show()
            }
            eventViewModel.clearDeleteResult()
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
                            joined = true,
                            canJoinEvents = true,
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

        // Dialog for confirm leaving an event
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