package com.example.sawaapplication.screens.notification.domain.useCases

import com.example.sawaapplication.screens.notification.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationsAsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke() = repository.markAllAsRead()
}