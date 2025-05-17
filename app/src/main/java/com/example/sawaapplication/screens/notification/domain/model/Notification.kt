package com.example.sawaapplication.screens.notification.domain.model

import java.util.Date

data class Notification(
    val id: String,
    val message: String,
    val timestamp: Date,
    val userId: String,
    val isRead: Boolean
)