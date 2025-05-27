package com.example.sawaapplication.screens.communities.presentation.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.communities.presentation.screens.component.CommunityEventCard
import com.example.sawaapplication.screens.communities.presentation.screens.component.CommunityFAB
import com.example.sawaapplication.screens.communities.presentation.screens.component.CommunityHeader
import com.example.sawaapplication.screens.communities.presentation.screens.component.CommunityTabs
import com.example.sawaapplication.screens.communities.presentation.screens.component.PostCard
import com.example.sawaapplication.screens.communities.presentation.vmModels.CommunityViewModel
import com.example.sawaapplication.screens.communities.presentation.vmModels.ExploreCommunityViewModel
import com.example.sawaapplication.screens.event.presentation.screens.formatDateString
import com.example.sawaapplication.screens.event.presentation.screens.formatTimestampToTimeString
import com.example.sawaapplication.utils.getCityNameFromGeoPoint
import com.example.sawaapplication.screens.event.presentation.vmModels.EventViewModel
import com.example.sawaapplication.ui.screenComponent.EventCard
import com.example.sawaapplication.screens.post.presentation.vmModels.CommunityPostsViewModel
import com.example.sawaapplication.ui.screenComponent.CustomConfirmationDialog
import com.example.sawaapplication.ui.theme.PrimaryOrange
import com.example.sawaapplication.ui.theme.white
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder

// Data class for dialog states
data class DialogState(
    val showLeaveCommunity: Boolean = false,
    val showLeaveEvent: Boolean = false,
    val showDeleteEvent: Boolean = false,
    val selectedEventId: String? = null
)

@Composable
fun CommunityScreen(
    communityId: String,
    viewModel: CommunityViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel(),
    communityPostsViewModel: CommunityPostsViewModel = hiltViewModel(),
    joinCommunityViewModel: ExploreCommunityViewModel = hiltViewModel(),
    onClick: (String) -> Unit,
    navController: NavHostController
) {
    // State Management
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    var selectedTab by remember { mutableIntStateOf(0) }
    var dialogState by remember { mutableStateOf(DialogState()) }

    // Collect States - Fixed: Use proper StateFlow collection
    val posts by communityPostsViewModel.communityPosts.collectAsState()
    val events by eventViewModel.events.collectAsState()
    val communityDetail by viewModel.communityDetail.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val hasJoinedOrLeft by joinCommunityViewModel.hasJoinedOrLeft.collectAsState()
    val isUserJoined = communityDetail?.members?.contains(currentUserId) == true

    // Create UI State
    val uiState = CommunityScreenState(
        posts = posts,
        events = events,
        communityDetail = communityDetail,
        isAdmin = isAdmin,
        hasJoinedOrLeft = hasJoinedOrLeft,
        isUserJoined = isUserJoined
    )

    // Initialize data
    LaunchedEffect(communityId) {
        Log.d("CommunityScreen", "Initializing community: $communityId")
        viewModel.fetchCommunityDetail(communityId)
        communityPostsViewModel.loadPosts(communityId)
        eventViewModel.loadEvents(communityId)
    }

    // Handle join/leave state changes
    LaunchedEffect(uiState.hasJoinedOrLeft) {
        if (uiState.hasJoinedOrLeft) {
            viewModel.fetchCommunityDetail(communityId)
            joinCommunityViewModel.resetJoinLeaveState()
        }
    }

    // Event Handlers
    val eventHandlers = CommunityEventHandlers(
        onLeaveCommunity = {
            joinCommunityViewModel.leaveCommunity(communityId, currentUserId)
            viewModel.fetchCommunityDetail(communityId)
            dialogState = dialogState.copy(showLeaveCommunity = false)
        },
        onLeaveEvent = { eventId ->
            eventViewModel.leaveEvent(communityId, eventId, currentUserId)
            dialogState = dialogState.copy(showLeaveEvent = false, selectedEventId = null)
        },
        onDeleteEvent = { eventId ->
            eventViewModel.deleteEvent(communityId, eventId)
            eventViewModel.loadEvents(communityId)
            dialogState = dialogState.copy(showDeleteEvent = false, selectedEventId = null)
        },
        onJoinCommunity = {
            joinCommunityViewModel.joinCommunity(communityId, currentUserId)
        },
        onJoinEvent = { eventId ->
            eventViewModel.joinEvent(communityId, eventId, currentUserId)
        }
    )

    // Dialogs
    CommunityDialogs(
        dialogState = dialogState,
        eventHandlers = eventHandlers,
        onDismissDialog = { dialogType ->
            dialogState = when (dialogType) {
                DialogType.LEAVE_COMMUNITY -> dialogState.copy(showLeaveCommunity = false)
                DialogType.LEAVE_EVENT -> dialogState.copy(
                    showLeaveEvent = false,
                    selectedEventId = null
                )

                DialogType.DELETE_EVENT -> dialogState.copy(
                    showDeleteEvent = false,
                    selectedEventId = null
                )
            }
        }
    )

    Scaffold(

        floatingActionButton = {
            CommunityFAB(
                selectedTab = selectedTab,
                communityId = communityId,
                navController = navController
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                CommunityHeader(
                    communityDetail = uiState.communityDetail,
                    isAdmin = uiState.isAdmin,
                    isUserJoined = uiState.isUserJoined,
                    navController = navController,
                    communityId = communityId,
                    onJoinCommunity = eventHandlers.onJoinCommunity,
                    onShowLeaveCommunityDialog = {
                        dialogState = dialogState.copy(showLeaveCommunity = true)
                    }
                )
            }

            item {
                CommunityTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // Posts Tab
                    items(uiState.posts) { post ->
                        PostCard(
                            post = post,
                            currentUserId = currentUserId,
                            onImageClick = { imageUrl ->
                                val encoded = URLEncoder.encode(imageUrl, "utf-8")
                                onClick(encoded)
                            },
                            onLikeClick = { communityPostsViewModel.likePost(it.id) },
                            navController = navController
                        )
                    }
                }

                1 -> {
                    // Events Tab
                    items(uiState.events) { event ->
                        CommunityEventCard(
                            event = event,
                            communityName = uiState.communityDetail?.name ?: "",
                            currentUserId = currentUserId,
                            navController = navController,
                            communityId = communityId,
                            onJoinEvent = {
                                eventHandlers.onJoinEvent(event.id)
                                event.time?.let { it1 ->
                                    eventViewModel.recordEventJoin(
                                        currentUserId,
                                        event.id,
                                        event.title,
                                        it1.toDate()
                                    )
                                }
                            },
                            onLeaveEvent = {
                                dialogState = dialogState.copy(
                                    showLeaveEvent = true,
                                    selectedEventId = event.id
                                )
                            },
                            onEditEvent = {
                                navController.navigate("edit_event/$communityId/${event.id}")
                            },
                            onDeleteEvent = {
                                dialogState = dialogState.copy(
                                    showDeleteEvent = true,
                                    selectedEventId = event.id
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// Data classes for better state management
data class CommunityScreenState(
    val posts: List<com.example.sawaapplication.screens.post.domain.model.PostUiModel>,
    val events: List<com.example.sawaapplication.screens.event.domain.model.Event>,
    val communityDetail: com.example.sawaapplication.screens.communities.domain.model.Community?,
    val isAdmin: Boolean,
    val hasJoinedOrLeft: Boolean,
    val isUserJoined: Boolean
)

data class CommunityEventHandlers(
    val onLeaveCommunity: () -> Unit,
    val onLeaveEvent: (String) -> Unit,
    val onDeleteEvent: (String) -> Unit,
    val onJoinCommunity: () -> Unit,
    val onJoinEvent: (String) -> Unit
)

private enum class DialogType {
    LEAVE_COMMUNITY, LEAVE_EVENT, DELETE_EVENT
}

@Composable
fun CommunityImageSection(
    communityDetail: com.example.sawaapplication.screens.communities.domain.model.Community?,
    isAdmin: Boolean,
    navController: NavHostController,
    communityId: String
) {
    Box(
        modifier = Modifier.size(integerResource(R.integer.photoBoxSize).dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        AsyncImage(
            model = communityDetail?.image,
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        if (isAdmin) {
            IconButton(
                onClick = { navController.navigate("edit_community/$communityId") },
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFFF5722), CircleShape)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Create,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun CommunityInfoSection(
    communityDetail: com.example.sawaapplication.screens.communities.domain.model.Community?
) {
    communityDetail?.let { community ->
        Text(
            text = community.name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "${community.members.size} Members",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Category display with background styling
        Text(
            text = community.category,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = integerResource(R.integer.communityDetailHorizontalPadding).dp)
                .clip(RoundedCornerShape(integerResource(R.integer.roundValue).dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(horizontal = integerResource(R.integer.extraSmallSpace).dp)
        )

        Spacer(Modifier.height(integerResource(R.integer.itemSpacerH2nd).dp))

        Text(
            text = community.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = integerResource(R.integer.communityDetailHorizontalPadding).dp)
        )
    }
}

@Composable
private fun CommunityDialogs(
    dialogState: DialogState,
    eventHandlers: CommunityEventHandlers,
    onDismissDialog: (DialogType) -> Unit
) {
    if (dialogState.showLeaveCommunity) {
        CustomConfirmationDialog(
            message = stringResource(R.string.areYouSureCommunity),
            onDismiss = { onDismissDialog(DialogType.LEAVE_COMMUNITY) },
            onConfirm = eventHandlers.onLeaveCommunity
        )
    }

    if (dialogState.showLeaveEvent) {
        dialogState.selectedEventId?.let { eventId ->
            CustomConfirmationDialog(
                message = stringResource(R.string.areYouSureEvent),
                onConfirm = { eventHandlers.onLeaveEvent(eventId) },
                onDismiss = { onDismissDialog(DialogType.LEAVE_EVENT) }
            )
        }
    }

    if (dialogState.showDeleteEvent) {
        dialogState.selectedEventId?.let { eventId ->
            CustomConfirmationDialog(
                message = stringResource(R.string.areYouSureEvent),
                onConfirm = { eventHandlers.onDeleteEvent(eventId) },
                onDismiss = { onDismissDialog(DialogType.DELETE_EVENT) }
            )
        }
    }
}