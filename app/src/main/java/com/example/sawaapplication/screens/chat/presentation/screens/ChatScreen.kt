package com.example.sawaapplication.screens.chat.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sawaapplication.screens.chat.presentation.screens.chatComponent.ChatBubble
import com.example.sawaapplication.screens.chat.presentation.screens.chatComponent.ChatInputBar
import com.example.sawaapplication.screens.chat.presentation.screens.chatComponent.ChatMembersHeader
import com.example.sawaapplication.screens.chat.presentation.screens.chatComponent.ChatTopBarBanner
import com.example.sawaapplication.screens.chat.presentation.screens.chatComponent.DateHeader
import com.example.sawaapplication.screens.chat.presentation.screens.chatComponent.ImagePreviewOverlay
import com.example.sawaapplication.screens.chat.presentation.vmModels.ChatViewModel
import com.example.sawaapplication.screens.communities.presentation.vmModels.CommunityViewModel
import kotlinx.coroutines.delay

@Composable
fun ChatScreen(
    communityId: String,
    navController: NavController
) {
    val communityViewModel: CommunityViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()

    val communityDetails by communityViewModel.communityDetail.collectAsState()
    val communityImage = communityDetails?.image.orEmpty()
    val communityName = communityDetails?.name.orEmpty()

    val currentUserId = chatViewModel.currentUserId
    val messages by chatViewModel.messages.collectAsState()
    val uiMessages by chatViewModel.uiMessages.collectAsState()
    val senderInfoMap by chatViewModel.senderInfo.collectAsState()
    val communityMembers by chatViewModel.communityMembers.collectAsState()
    val loading by chatViewModel.loading.collectAsState()
    val error by chatViewModel.error.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        selectedImageUri = it
    }

    // Data fetching
    LaunchedEffect(communityId) {
        communityViewModel.fetchCommunityDetail(communityId)
        chatViewModel.fetchCommunityMembers(communityId)
        chatViewModel.observeMessages(communityId)
    }

    // Auto-scroll on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    // Pre-fetch sender info
    LaunchedEffect(uiMessages) {
        uiMessages
            .filter { it.type == ChatViewModel.MessageType.MESSAGE }
            .mapNotNull { it.message?.senderId }
            .distinct()
            .forEach { chatViewModel.fetchSenderInfo(it) }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    Scaffold(
        bottomBar = {
            ChatInputBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                selectedImageUri = selectedImageUri,
                onPickImageClick = { photoPickerLauncher.launch("image/*") },
                onSendClick = {
                    if (messageText.isNotBlank() || selectedImageUri != null) {
                        chatViewModel.sendMessage(
                            communityId = communityId,
                            messageText = messageText,
                            senderId = currentUserId ?: "",
                            imageUrl = selectedImageUri
                        )
                        messageText = ""
                        selectedImageUri = null
                    }
                },
                onClearImage = { selectedImageUri = null }
            )
        }
    ) { innerPadding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            ChatTopBarBanner(
                communityImage = communityImage,
                onBackClick = { navController.popBackStack() },
                onImageClick = { navController.navigate("community_screen/$communityId") }
            )

            ChatMembersHeader(
                communityName = communityName,
                members = communityMembers,
                onClick = { navController.navigate("groupMembers/$communityId") }
            )

            if (uiMessages.isEmpty()) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No messages yet.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(uiMessages) { item ->
                        when (item.type) {
                            ChatViewModel.MessageType.DATE_HEADER -> {
                                DateHeader(date = item.date!!)
                            }
                            ChatViewModel.MessageType.MESSAGE -> {
                                item.message?.let { msg ->
                                    val senderInfo = senderInfoMap[msg.senderId]
                                    val allMemberIds = communityMembers.mapNotNull { it.userId }
                                    val unreadCount = allMemberIds.count { id -> msg.readBy[id] != true }

                                    ChatBubble(
                                        message = msg,
                                        isCurrentUser = msg.senderId == currentUserId,
                                        image = senderInfo?.image,
                                        userName = senderInfo?.name,
                                        unreadCount = unreadCount,
                                        onImageClick = { previewImageUrl = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    ImagePreviewOverlay(previewImageUrl = previewImageUrl) {
        previewImageUrl = null
    }

}