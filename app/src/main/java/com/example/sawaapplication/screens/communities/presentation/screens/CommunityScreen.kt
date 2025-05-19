package com.example.sawaapplication.screens.communities.presentation.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.communities.presentation.vmModels.CommunityViewModel
import com.example.sawaapplication.screens.communities.presentation.vmModels.ExploreCommunityViewModel
import com.example.sawaapplication.screens.event.presentation.screens.formatDateString
import com.example.sawaapplication.screens.event.presentation.screens.formatTimestampToTimeString
import com.example.sawaapplication.screens.event.presentation.screens.getCityNameFromGeoPoint
import com.example.sawaapplication.screens.event.presentation.vmModels.FetchEventViewModel
import com.example.sawaapplication.screens.home.presentation.screens.component.EventCard
import com.example.sawaapplication.screens.post.domain.model.PostUiModel
import com.example.sawaapplication.ui.screenComponent.CustomConfirmationDialog
import com.example.sawaapplication.ui.theme.Gray
import com.example.sawaapplication.ui.theme.PrimaryOrange
import com.example.sawaapplication.ui.theme.black
import com.example.sawaapplication.ui.theme.white
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder



data class CommunityUiState(
    val logoUrl: String,
    val communityName: String,
    val membersCount: String,
    val communityDescription: String,
    val posts: List<PostUiModel>
)

//private val FakeCommunityUiState = CommunityUiState(
//    logoUrl = "",
//    communityName = "Saudi Innovation",
//    membersCount = "2.5M",
//    communityDescription = "This community fosters innovation across Saudi Arabia...",
//    posts = listOf(
//        PostUiModel(
//            "@mohammed1",
//            userAvatarUrl = "https://i.pravatar.cc/150?img=1",
//            postImageUrl = ""
//        ),
//        PostUiModel(
//            "@ahmed2",
//            userAvatarUrl = "https://i.pravatar.cc/150?img=2",
//            postImageUrl = "https://images.unsplash.com/photo-1593642634367-d91a135587b5"
//        )
//    )
//)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    communityId: String,
    viewModel: CommunityViewModel = hiltViewModel(),
    eventViewModel: FetchEventViewModel = hiltViewModel(),
    joinCommunityViewModel: ExploreCommunityViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onClick: (String) -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current
    val fetchEventViewModel: FetchEventViewModel = hiltViewModel()
//    val uiState = FakeCommunityUiState
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.posts), stringResource(R.string.events))

    val posts by viewModel.communityPosts.collectAsState()
    var joinedevent by remember { mutableStateOf(false) }// we need to get the dynamic initial value
    var joined by remember { mutableStateOf(false) }// we need to get the dynamic initial value
    val communityDetail by viewModel.communityDetail.collectAsState()
    //val events by eventViewModel.events.collectAsState()
    val isUserJoined = communityDetail?.members?.contains(userId) == true
    val hasJoinedOrLeft by joinCommunityViewModel.hasJoinedOrLeft.collectAsState()

    var showLeaveCommunityDialog by remember { mutableStateOf(false) }

    var showLeaveEventDialog by remember { mutableStateOf(false) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }

    val events by fetchEventViewModel.events.collectAsState()

    LaunchedEffect(communityId) {
        Log.d("DEBUG", "CommunityScreen launched with id: $communityId")
        viewModel.fetchCommunityDetail(communityId)
        viewModel.fetchPostsForCommunity(communityId)
        fetchEventViewModel.loadEvents(communityId)
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        viewModel.fetchCommunityDetail(communityId)
    }


    LaunchedEffect(hasJoinedOrLeft) {
        if (hasJoinedOrLeft) {
            viewModel.fetchCommunityDetail(communityId)
            // Reset the flag so it doesn't re-trigger
            joinCommunityViewModel.resetJoinLeaveState()
        }
    }

    //Dialog for confirm leaving an event
    if (showLeaveEventDialog && selectedEventId != null) {
        CustomConfirmationDialog(
            message = stringResource(R.string.areYouSureEvent),
            onConfirm = {
                eventViewModel.leaveEvent(
                    communityId = communityId,
                    eventId = selectedEventId!!,
                    userId = userId
                )
                showLeaveEventDialog = false
                selectedEventId = null
            },
            onDismiss = {
                showLeaveEventDialog = false
                selectedEventId = null
            }
        )
    }

    if (showLeaveCommunityDialog){
        CustomConfirmationDialog(
            message = stringResource(R.string.areYouSureCommunity),
            onDismiss = {
                showLeaveCommunityDialog = false
            },
            onConfirm = {
                joinCommunityViewModel.leaveCommunity(communityId, userId)
                viewModel.fetchCommunityDetail(communityId)
                showLeaveCommunityDialog = false
            },
        )

    }
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {},
                windowInsets = WindowInsets(integerResource(R.integer.zero))
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> {
                    // FAB for Posts tab
                    FloatingActionButton(
                        onClick = { navController.navigate("create_post/$communityId") },
                        modifier = Modifier.size(integerResource(R.integer.floatingActionButtonSize).dp),
                        shape = CircleShape,
                        containerColor = PrimaryOrange,
                        contentColor = white,
                        elevation = FloatingActionButtonDefaults.elevation(integerResource(R.integer.floatingActionButtonElevation).dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Add Post")
                    }
                }

                1 -> {
                    // FAB for Events tab
                    FloatingActionButton(
                        onClick = { navController.navigate("create_event/$communityId") },
                        modifier = Modifier.size(integerResource(R.integer.floatingActionButtonSize).dp),
                        shape = CircleShape,
                        containerColor = PrimaryOrange,
                        contentColor = white,
                        elevation = FloatingActionButtonDefaults.elevation(integerResource(R.integer.floatingActionButtonElevation).dp)
                    ) {
                        Icon(Icons.Default.Event, contentDescription = "Add Event")
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = WindowInsets(integerResource(R.integer.zero))
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(integerResource(R.integer.lazyColumnSpacedBy).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(integerResource(R.integer.itemSpacerH).dp))
                AsyncImage(
                    model = communityDetail?.image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(integerResource(R.integer.itemSpacerSize).dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(integerResource(R.integer.itemSpacerH2nd).dp))
                communityDetail?.let {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = black
                    )
                }
                Text(
                    text = "${communityDetail?.members?.size ?: 0} Members",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray
                )
                Spacer(Modifier.height(integerResource(R.integer.itemSpacerH2nd).dp))
                communityDetail?.let {
                    Text(
                        text = it.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = integerResource(R.integer.communityDetailHorizontalPadding).dp)
                    )
                }
                Spacer(Modifier.height(integerResource(R.integer.itemSpacerH).dp))

                if (isUserJoined) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(integerResource(R.integer.padding).dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        //Un-joined button
                        OutlinedButton(
                            onClick = {
                                //Leave Community
                                showLeaveCommunityDialog=true
//                                joinCommunityViewModel.leaveCommunity(communityId, userId)
//                                viewModel.fetchCommunityDetail(communityId)
                            },
                            shape = RoundedCornerShape(integerResource(R.integer.roundedCornerShapeCircle)),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            border = BorderStroke(
                                integerResource(R.integer.buttonStroke).dp,
                                PrimaryOrange
                            ),
                            contentPadding = PaddingValues(
                                horizontal = integerResource(R.integer.buttonPaddingH).dp,
                                vertical = integerResource(R.integer.buttonPaddingV).dp
                            ),
                            elevation = ButtonDefaults.buttonElevation(integerResource(R.integer.buttonElevation).dp),
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentSize()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.unjoind),
                                contentDescription = "nu-join icon",
                                tint = PrimaryOrange,
                                modifier = Modifier
                                    .size(integerResource(R.integer.iconSize).dp),
                            )
                            Spacer(Modifier.width(integerResource(R.integer.itemSpacerH3ed).dp))
                            Text(stringResource(R.string.joined))
                        }

                        //Go to chat button
                        OutlinedButton(
                            onClick = { navController.navigate("chat/${communityId}") },
                            shape = RoundedCornerShape(integerResource(R.integer.roundedCornerShapeCircle)),
                            border = BorderStroke(
                                integerResource(R.integer.buttonStroke).dp,
                                PrimaryOrange
                            ),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            contentPadding = PaddingValues(
                                horizontal = integerResource(R.integer.buttonPaddingH).dp,
                                integerResource(R.integer.buttonPaddingV).dp
                            ),
                            elevation = ButtonDefaults.buttonElevation(integerResource(R.integer.buttonElevation).dp),
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentSize()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.chats),
                                contentDescription = "chat icon",
                                tint = PrimaryOrange,
                                modifier = Modifier
                                    .size(integerResource(R.integer.iconSize).dp),
                            )
                            Spacer(Modifier.width(integerResource(R.integer.itemSpacerH3ed).dp))
                            Text(stringResource(R.string.chat))
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            joinCommunityViewModel.joinCommunity(communityId, userId)
                            viewModel.fetchCommunityDetail(communityId)
                        },
                        shape = RoundedCornerShape(integerResource(R.integer.roundedCornerShapeCircle)),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        contentPadding = PaddingValues(
                            horizontal = integerResource(R.integer.buttonPaddingH).dp,
                            vertical = integerResource(R.integer.buttonPaddingV).dp
                        ),
                        elevation = ButtonDefaults.buttonElevation(integerResource(R.integer.buttonElevation).dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(integerResource(R.integer.itemSpacerH3ed).dp))
                        Text(
                            stringResource(R.string.joinCommunity),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(Modifier.height(integerResource(R.integer.itemSpacerH).dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = white,
                    indicator = { positions ->
                        TabRowDefaults.Indicator(
                            Modifier
                                .tabIndicatorOffset(positions[selectedTab])
                                .height(integerResource(R.integer.tabRowHeight).dp),
                            color = PrimaryOrange
                        )
                    }
                ) {
                    tabs.forEachIndexed { i, title ->
                        Tab(
                            selected = selectedTab == i,
                            onClick = { selectedTab = i },
                            text = {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selectedTab == i) black else Gray
                                )
                            }
                        )
                    }
                }
                Spacer(Modifier.height(integerResource(R.integer.itemSpacerH3ed).dp))
            }

            if (selectedTab == 0) {
                items(posts) { post ->
                    PostCard(
                        post = PostUiModel(
                            id = post.id,
                            username      = post.username,
                            userAvatarUrl = post.userAvatarUrl,
                            postImageUrl  = post.postImageUrl,
                            content       = post.content
                        ),
                        onImageClick = { imageUrl ->
                            Log.d("FULLSCREEN", "Image URL passed: $imageUrl")
                            val encoded = URLEncoder.encode(imageUrl, "utf-8")
                            onClick(encoded) // this triggers navigation to fullscreen screen
                        }
                    )

                }
            } else {
                items(events) { event ->
                    communityDetail?.let {
                        val timeFormatted = event.time?.let { formatTimestampToTimeString(it) } ?: "No time set"
                        val formattedDate = formatDateString(event.date)

                        EventCard(
                            image = event.imageUri,
                            title = event.title,
                            description = event.description,
                            location = context.getCityNameFromGeoPoint(event.location),
                            participants = event.memberLimit,
                            joinedUsers = event.joinedUsers,
                            community = it.name,
                            time = timeFormatted,
                            date = formattedDate,
                            joined = event.joinedUsers.contains(userId),
                            onJoinClick = {
                                if (event.joinedUsers.contains(userId)) {
                                    selectedEventId = event.id
                                    showLeaveEventDialog = true
//                                    eventViewModel.leaveEvent(
//                                        communityId = communityId,
//                                        eventId = event.id,
//                                        userId = userId
//                                    )
                                } else {
                                    eventViewModel.joinEvent(
                                        communityId = communityId,
                                        eventId = event.id,
                                        userId = userId
                                    )
                                }
                            },
                            showCancelButton = true,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}