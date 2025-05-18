package com.example.sawaapplication.screens.notification.domain.useCases

import com.example.sawaapplication.screens.notification.domain.model.Notification
import com.example.sawaapplication.screens.notification.domain.repository.NotificationRepository
import javax.inject.Inject

class FetchNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(listener: (List<Notification>) -> Unit) {
        repository.fetchNotifications(listener)
    }
}