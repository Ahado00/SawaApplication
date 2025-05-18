package com.example.sawaapplication.screens.notification.domain.useCases

import com.example.sawaapplication.screens.notification.domain.repository.NotificationRepository
import com.example.sawaapplication.screens.post.domain.model.Post
import javax.inject.Inject

class SendLikeNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(post: Post) = repository.sendLikeNotification(post)
}