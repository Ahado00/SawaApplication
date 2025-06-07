package com.example.sawaapplication.screens.home.data.dataSources.remote

import android.util.Log
import com.example.sawaapplication.screens.event.domain.model.Event
import com.example.sawaapplication.screens.post.domain.model.Comment
import com.example.sawaapplication.screens.post.domain.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class HomeRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
){
    suspend fun getUserCommunityIds(userId: String): List<String> {
        return try {
            val snapshot = firestore.collection("Community").get().await()
            snapshot.documents
                .filter { doc ->
                    val members = doc.get("members") as? List<*> ?: emptyList<Any>()
                    userId in members
                }
                .map { it.id }
        } catch (e: Exception) {
            Log.e("PostRemote", "Error fetching user communities: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchAllPosts(): Pair<List<Post>, Map<Post, String>> {
        return try {
            val currentUserId = firebaseAuth.currentUser?.uid
                ?: return Pair(emptyList(), emptyMap())
            val userCommunityIds = getUserCommunityIds(currentUserId)

            if (userCommunityIds.isEmpty()) {
                return Pair(emptyList(), emptyMap())
            }

            val postsList = mutableListOf<Post>()
            val docIdMap = mutableMapOf<Post, String>()

            for (communityId in userCommunityIds) {
                val postSnapshot = firestore
                    .collection("Community")
                    .document(communityId)
                    .collection("posts")
                    .get()
                    .await()

                postSnapshot.documents.forEach { doc ->
                    val post = doc.toObject(Post::class.java)
                    if (post != null) {
                        postsList.add(post)
                        docIdMap[post] = doc.id
                    }
                }
            }
            val sortedPosts = postsList.sortedByDescending { post ->
                try {
                    val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                    dateFormat.parse(post.createdAt)
                } catch (e: Exception) {
                    Log.e("PostRepository", "Error parsing date: ${post.createdAt}")
                    null
                }
            }
            Pair(sortedPosts, docIdMap)
        } catch (e: Exception) {
            Log.e("PostRepository", "Error fetching posts: ${e.message}")
            Pair(emptyList(), emptyMap())
        }
    }

    suspend fun fetchCommunityNames(communityId: List<String>): Map<String, String> {
        val namesMap = mutableMapOf<String, String>()

        for (id in communityId) {
            try {
                val snapshot = firestore.collection("Community").document(id).get().await()
                if (snapshot.exists()) {
                    val name = snapshot.getString("name") ?: "Unknown"
                    namesMap[id] = name
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error fetching community $id: ${e.message}")
            }
        }

        return namesMap
    }

    suspend fun fetchUserDetails(userId: List<String>): Map<String, Pair<String, String>> {
        val detailsMap = mutableMapOf<String, Pair<String, String>>()

        for (id in userId) {
            try {
                val snapshot = firestore.collection("User").document(id).get().await()
                if (snapshot.exists()) {
                    val name = snapshot.getString("name") ?: "Unknown"
                    val image = snapshot.getString("image") ?: ""
                    detailsMap[id] = name to image
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error fetching user $id: ${e.message}")
            }
        }

        return detailsMap
    }

    suspend fun likePost(post: Post, postDocId: String?): Pair<Post?, String?> {
        if (firebaseAuth.currentUser?.uid.isNullOrEmpty()) {
            Log.e("PostRepository", "User not logged in")
            return Pair(null, null)
        }

        val currentUserId = firebaseAuth.currentUser!!.uid

        if (postDocId == null) {
            Log.e("PostRepository", "Document ID not found for post")
            return Pair(null, null)
        }

        val postRef = firestore
            .collection("Community")
            .document(post.communityId)
            .collection("posts")
            .document(postDocId)

        var postLikedUserId: String? = null
        var updatedPost: Post? = null

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()

            val isLiked = currentUserId in likedBy
            val newLikedBy = if (isLiked) likedBy - currentUserId else likedBy + currentUserId
            val newLikes = newLikedBy.size

            transaction.update(
                postRef, mapOf(
                    "likes" to newLikes,
                    "likedBy" to newLikedBy
                )
            )

            updatedPost = post.copy(
                likes = newLikes,
                likedBy = newLikedBy
            )

            if (!isLiked && post.userId != currentUserId) {
                postLikedUserId = post.userId
            }
        }.await()

        return Pair(updatedPost, postLikedUserId)
    }

    suspend fun fetchPostsByUser(userId: String): Pair<List<Post>, Map<Post, String>> {
        val userCommunityIds = getUserCommunityIds(userId)

        if (userCommunityIds.isEmpty()) {
            return Pair(emptyList(), emptyMap())
        }

        val postsList = mutableListOf<Post>()
        val docIdMap = mutableMapOf<Post, String>()

        userCommunityIds.forEach { communityId ->
            val postSnapshot = firestore
                .collection("Community")
                .document(communityId)
                .collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            postSnapshot.documents.forEach { doc ->
                val post = doc.toObject(Post::class.java)
                if (post != null) {
                    postsList.add(post)
                    docIdMap[post] = doc.id
                }
            }
        }

         val sortedPosts = postsList.sortedByDescending { post ->
             try {
                 val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                 dateFormat.parse(post.createdAt)
             } catch (e: Exception) {
                 Log.e("PostRepository", "Error parsing date: ${post.createdAt}")
                 null
             }
         }
         return Pair(sortedPosts, docIdMap)
    }

    suspend fun fetchLikedPostsByUser(userId: String): Pair<List<Post>, Map<Post, String>> {
        val userCommunityIds = getUserCommunityIds(userId)

        if (userCommunityIds.isEmpty()) return emptyList<Post>() to emptyMap()

        val postsList = mutableListOf<Post>()
        val docIdMap = mutableMapOf<Post, String>()

        userCommunityIds.forEach { communityId ->
            val postSnapshot = firestore
                .collection("Community")
                .document(communityId)
                .collection("posts")
                .whereArrayContains("likedBy", userId)
                .get()
                .await()

            for (doc in postSnapshot.documents) {
                val post = doc.toObject(Post::class.java)
                if (post != null) {
                    postsList.add(post)
                    docIdMap[post] = doc.id
                }
            }
        }

        val sortedPosts = postsList.sortedByDescending { post ->
            try {
                val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                dateFormat.parse(post.createdAt)
            } catch (e: Exception) {
                Log.e("PostRepository", "Error parsing date: ${post.createdAt}")
                null
            }
        }
        return sortedPosts to docIdMap
    }

    suspend fun deletePost(post: Post, docId: String) {
        val postRef = firestore
            .collection("Community")
            .document(post.communityId)
            .collection("posts")
            .document(docId)

        postRef.delete().await()
    }

    suspend fun deleteComment(communityId: String, postId: String, commentId: String) {
        try {
            val commentRef = firestore
                .collection("Community")
                .document(communityId)
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document(commentId)

            commentRef.delete().await()
        } catch (e: Exception) {
            Log.e("Firebase", "Error deleting comment: ${e.message}")
            throw e
        }
    }

    suspend fun fetchJoinedEvents(userId: String): List<Event> {
        val communityIds = getUserCommunityIds(userId)

        val joinedEventsDeferred = communityIds.map { communityId ->
            coroutineScope {
                async {
                    val eventsSnapshot = firestore.collection("Community")
                        .document(communityId)
                        .collection("event")
                        .get()
                        .await()

                    eventsSnapshot.documents.mapNotNull { doc ->
                        val event = doc.toObject(Event::class.java)
                        if (event != null && userId in event.joinedUsers) {
                            event.copy(id = doc.id, communityId = communityId)
                        } else null
                    }
                }
            }
        }

        return joinedEventsDeferred.awaitAll().flatten()
    }

    suspend fun addComment(communityId: String, postId: String, comment: Comment) {
        try {
            val commentRef = firestore
                .collection("Community")
                .document(communityId)
                .collection("posts")
                .document(postId)
                .collection("comments")
                .document()

            val commentWithId = comment.copy(id = commentRef.id)
            commentRef.set(comment).await()
        } catch (e: Exception) {
            Log.e("Firebase", "Error adding comment: ${e.message}")
            throw e
        }
    }

    suspend fun fetchComments(communityId: String, postId: String): List<Comment> {
        return try {
            val snapshot = firestore
                .collection("Community")
                .document(communityId)
                .collection("posts")
                .document(postId)
                .collection("comments")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val comment = doc.toObject(Comment::class.java)
                comment?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching comments: ${e.message}")
            emptyList()
        }
    }
}