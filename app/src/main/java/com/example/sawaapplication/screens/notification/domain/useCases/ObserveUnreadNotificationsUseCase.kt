package com.example.sawaapplication.screens.notification.domain.useCases

import com.example.sawaapplication.screens.notification.domain.repository.NotificationRepository
import javax.inject.Inject

class ObserveUnreadNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(listener: (Boolean) -> Unit) {
        repository.observeUnreadNotifications(listener)
    }
}
