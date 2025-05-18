package com.example.sawaapplication.screens.notification.data.repository

import com.example.sawaapplication.screens.notification.data.dataSources.NotificationRemoteDataSource
import com.example.sawaapplication.screens.notification.domain.model.Notification
import com.example.sawaapplication.screens.notification.domain.repository.NotificationRepository
import com.example.sawaapplication.screens.post.domain.model.Post
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val remote: NotificationRemoteDataSource,
    private val auth: FirebaseAuth
) : NotificationRepository {

    override fun fetchNotifications(listener: (List<Notification>) -> Unit) {
        remote.fetchNotifications(listener)
    }

    override fun markAllAsRead() {
        remote.markAllAsRead()
    }

    override fun sendProfileUpdateNotification() {
        val userId = auth.currentUser?.uid ?: return
        remote.sendNotificationToUser(userId, "Your profile has been updated!")
    }

    override fun sendEventCreatedNotification(eventName: String) {
        val userId = auth.currentUser?.uid ?: return
        remote.sendNotificationToUser(userId, "You created the event: $eventName")
    }

    override fun sendCommunityEventNotification(communityId: String, eventName: String) {
        remote.sendCommunityEventNotification(communityId, eventName)
    }

    override fun sendLikeNotification(post: Post) {
        remote.sendLikeNotification(post)
    }

    override fun observeUnreadNotifications(listener: (Boolean) -> Unit) {
        remote.observeUnreadNotifications(listener)
    }

}