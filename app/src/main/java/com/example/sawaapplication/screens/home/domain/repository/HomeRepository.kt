package com.example.sawaapplication.screens.home.domain.repository

import com.example.sawaapplication.screens.event.domain.model.Event
import com.example.sawaapplication.screens.post.domain.model.Comment
import com.example.sawaapplication.screens.post.domain.model.Post

interface HomeRepository {
    suspend fun getUserCommunityIds(userId: String):List<String>
    suspend fun fetchAllPosts(): Pair<List<Post>, Map<Post, String>>
    suspend fun fetchCommunityNames(id: List<String>):Map<String, String>
    suspend fun fetchUserDetails(userId: List<String>): Map<String, Pair<String, String>>
    suspend fun likePost(post: Post, postDocId: String?): Pair<Post?, String?>
    suspend fun fetchPostsByUser(userId: String): Pair<List<Post>, Map<Post, String>>
    suspend fun fetchLikedPostsByUser(userId: String): Pair<List<Post>, Map<Post, String>>
    suspend fun deletePost(post: Post, docId: String)
    suspend fun fetchJoinedEvents(userId: String): List<Event>
    suspend fun addComment(communityId: String, postId: String, comment: Comment)
    suspend fun fetchComments(communityId: String, postId: String): List<Comment>
}