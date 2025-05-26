package com.example.sawaapplication.screens.chat.data.dataSources.repository

import android.net.Uri
import com.example.sawaapplication.screens.chat.data.dataSources.remote.MassageRemoteDataSource
import com.example.sawaapplication.screens.chat.domain.model.ChatUserInfo
import com.example.sawaapplication.screens.chat.domain.model.Message
import com.example.sawaapplication.screens.chat.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val remoteDataSource: MassageRemoteDataSource
) : MessageRepository {
    override suspend fun sendMessage(communityId: String, message: Message) {
        val uri = if (message.imageUrl.isNotBlank()) Uri.parse(message.imageUrl) else null
        remoteDataSource.sendMessage(communityId, message.text, message.senderId, uri)
    }

    override fun observeMessages(communityId: String, currentUserId: String): Flow<List<Message>> {
        return remoteDataSource.observeMessages(communityId, currentUserId)
    }

    override suspend fun getLastMessageAndSender(communityId: String): Pair<String, ChatUserInfo>? {
        return remoteDataSource.getLastMessageAndSender(communityId)
    }

    override fun fetchUnreadMessages(communityId: String, userId: String, onResult: (Int) -> Unit) {
        remoteDataSource.fetchUnreadMessages(communityId, userId, onResult)
    }

    override suspend fun markMessagesAsRead(communityId: String, userId: String){
        remoteDataSource.markMessagesAsRead(communityId, userId)
    }

    override suspend fun getSenderInfo(userId: String): ChatUserInfo? {
        return remoteDataSource.getSenderInfo(userId)
    }

}

