package com.example.sawaapplication.screens.notification.domain.useCases

import com.example.sawaapplication.screens.notification.domain.repository.NotificationRepository
import javax.inject.Inject

class SendEventCreatedNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(eventName: String) = repository.sendEventCreatedNotification(eventName)
}