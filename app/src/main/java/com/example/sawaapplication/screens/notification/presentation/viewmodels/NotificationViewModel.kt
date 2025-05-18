package com.example.sawaapplication.screens.notification.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawaapplication.screens.notification.domain.model.Notification
import com.example.sawaapplication.screens.notification.domain.useCases.FetchNotificationsUseCase
import com.example.sawaapplication.screens.notification.domain.useCases.MarkNotificationsAsReadUseCase
import com.example.sawaapplication.screens.notification.domain.useCases.NotifyCommunityOfEventUseCase
import com.example.sawaapplication.screens.notification.domain.useCases.SendEventCreatedNotificationUseCase
import com.example.sawaapplication.screens.notification.domain.useCases.SendLikeNotificationUseCase
import com.example.sawaapplication.screens.notification.domain.useCases.SendProfileUpdateNotificationUseCase
import com.example.sawaapplication.core.sharedPreferences.NotificationPreferences
import com.example.sawaapplication.screens.notification.domain.useCases.ObserveUnreadNotificationsUseCase
import com.example.sawaapplication.screens.post.domain.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val fetchNotificationsUseCase: FetchNotificationsUseCase,
    private val markNotificationsAsReadUseCase: MarkNotificationsAsReadUseCase,
    private val sendProfileUpdateNotificationUseCase: SendProfileUpdateNotificationUseCase,
    private val sendEventCreatedNotificationUseCase: SendEventCreatedNotificationUseCase,
    private val notifyCommunityOfEventUseCase: NotifyCommunityOfEventUseCase,
    private val sendLikeNotificationUseCase: SendLikeNotificationUseCase,
    private val notificationPreferences: NotificationPreferences,
    private val observeUnreadNotificationsUseCase: ObserveUnreadNotificationsUseCase
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _hasUnreadNotifications = MutableStateFlow(true)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications

    init {
        fetchNotifications()
        observeUnread()
    }

    fun fetchNotifications() {
        fetchNotificationsUseCase { fetched ->
            _notifications.value = fetched
            val hasUnread = fetched.any { !it.isRead }
            _hasUnreadNotifications.value = hasUnread
        }
    }

    fun markAsRead() {
        markNotificationsAsReadUseCase()
        _hasUnreadNotifications.value = false
        notificationPreferences.markAsSeen()
    }

    fun notifyProfileUpdate() {
        sendProfileUpdateNotificationUseCase()
    }

    fun notifyEventCreated(eventName: String) {
        sendEventCreatedNotificationUseCase(eventName)
    }

    fun notifyCommunityMembers(communityId: String, eventName: String) {
        notifyCommunityOfEventUseCase(communityId, eventName)
    }

    fun notifyLike(post: Post) {
        sendLikeNotificationUseCase(post)
    }

    private fun observeUnread() {
        observeUnreadNotificationsUseCase { hasUnread ->
            _hasUnreadNotifications.value = hasUnread
        }

    }
}