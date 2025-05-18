package com.example.sawaapplication.screens.notification.domain.useCases

import com.example.sawaapplication.screens.notification.domain.repository.NotificationRepository
import javax.inject.Inject

class SendProfileUpdateNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke() = repository.sendProfileUpdateNotification()
}