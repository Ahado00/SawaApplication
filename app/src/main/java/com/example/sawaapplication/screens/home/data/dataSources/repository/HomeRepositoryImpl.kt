package com.example.sawaapplication.screens.home.data.dataSources.repository

import com.example.sawaapplication.screens.event.domain.model.Event
import com.example.sawaapplication.screens.home.data.dataSources.remote.HomeRemoteDataSource
import com.example.sawaapplication.screens.home.domain.repository.HomeRepository
import com.example.sawaapplication.screens.post.domain.model.Comment
import com.example.sawaapplication.screens.post.domain.model.Post
import java.util.Date

import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val remoteDataSource: HomeRemoteDataSource
) : HomeRepository {
    override suspend fun getUserCommunityIds(userId: String): List<String> {
        return remoteDataSource.getUserCommunityIds(userId)
    }

    override suspend fun fetchAllPosts(): Pair<List<Post>, Map<Post, String>> {
        return remoteDataSource.fetchAllPosts()
    }

    override suspend fun fetchCommunityNames(id: List<String>): Map<String, String> {
        return remoteDataSource.fetchCommunityNames(id)
    }

    override suspend fun fetchUserDetails(userId: List<String>): Map<String, Pair<String, String>> {
        return remoteDataSource.fetchUserDetails(userId)
    }

    override suspend fun likePost(post: Post, postDocId: String?): Pair<Post?, String?> {
        return remoteDataSource.likePost(post, postDocId)
    }

    override suspend fun fetchPostsByUser(userId: String): Pair<List<Post>, Map<Post, String>> {
        return remoteDataSource.fetchPostsByUser(userId)
    }

    override suspend fun fetchLikedPostsByUser(userId: String): Pair<List<Post>, Map<Post, String>> {
        return remoteDataSource.fetchLikedPostsByUser(userId)
    }

    override suspend fun deletePost(post: Post, docId: String) {
        return remoteDataSource.deletePost(post, docId)
    }

    override suspend fun fetchJoinedEvents(userId: String): List<Event> {
        return remoteDataSource.fetchJoinedEvents(userId)
    }

    override suspend fun addComment(communityId: String, postId: String, comment: Comment) {
        remoteDataSource.addComment(communityId, postId, comment)
    }

    override suspend fun fetchComments(communityId: String, postId: String): List<Comment> {
        return remoteDataSource.fetchComments(communityId, postId)
    }

    override suspend fun deleteComment(communityId: String, postId: String, commentId: String) {
        remoteDataSource.deleteComment(communityId, postId, commentId)
    }

}