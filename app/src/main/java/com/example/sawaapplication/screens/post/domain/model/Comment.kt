package com.example.sawaapplication.screens.post.domain.model

data class Comment (
    val postCreatorId: String? = "",
    val commentedById: String = "",
    val commentContent: String = "",
    val createdAt: String = "",
)