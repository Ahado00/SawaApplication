package com.example.sawaapplication.screens.notification.domain.repository

import com.example.sawaapplication.screens.notification.domain.model.Notification
import com.example.sawaapplication.screens.post.domain.model.Post

interface NotificationRepository {
    fun fetchNotifications(listener: (List<Notification>) -> Unit)
    fun markAllAsRead()
    fun sendProfileUpdateNotification()
    fun sendEventCreatedNotification(eventName: String)
    fun sendCommunityEventNotification(communityId: String, eventName: String)
    fun sendLikeNotification(post: Post)
    fun observeUnreadNotifications(listener: (Boolean) -> Unit)
}