package com.example.sawaapplication.screens.notification.domain.useCases

import com.example.sawaapplication.screens.notification.domain.repository.NotificationRepository
import javax.inject.Inject

class NotifyCommunityOfEventUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(communityId: String, eventName: String) {
        repository.sendCommunityEventNotification(communityId, eventName)
    }
}