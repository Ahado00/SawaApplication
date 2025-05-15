package com.example.sawaapplication.screens.post.domain.model

data class Post(
    val id: String = "",
    val userId: String = "", // Reference to User collection
    val content: String = "" ,
    val imageUri: String = "" ,
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val createdAt: String = "",
    val communityId: String = ""
)