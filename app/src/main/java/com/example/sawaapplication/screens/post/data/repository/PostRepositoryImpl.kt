package com.example.sawaapplication.screens.post.data.repository

import android.net.Uri
import android.util.Log

import com.example.sawaapplication.screens.post.data.dataSources.remote.PostsInCommunityRemote
import com.example.sawaapplication.screens.post.domain.model.Post
import com.example.sawaapplication.screens.post.domain.model.PostUiModel
import com.example.sawaapplication.screens.post.domain.repository.PostRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val remoteDataSource: PostsInCommunityRemote,
    private val firestore: FirebaseFirestore,
) : PostRepository {

    override suspend fun getPostsForCommunity(communityId: String): Result<List<PostUiModel>> {
        return try {
            val snapshot = firestore.collection("Community")
                .document(communityId)
                .collection("posts")
                .get()
                .await()

            val uiModels = snapshot.documents.mapNotNull { doc ->
                // 1) Convert Firestore doc → domain Post
                val post = doc.toObject(Post::class.java) ?: return@mapNotNull null

                // 2) Fetch the user document
                val userDoc = firestore.collection("User")
                    .document(post.userId)
                    .get()
                    .await()
                val username  = userDoc.getString("name") ?: "Unknown"
                val avatarUrl = userDoc.getString("profileImage") ?: ""
                val content   = post.content
                Log.d("Content", " $content ")
                Log.d("PostRepo", "Loaded post (id=${doc.id}) → user=$username, content=$content")
                // 3) Map into PostUiModel
                PostUiModel(
                    id = post.id,
                    username      = userDoc.getString("name") ?: "Unknown",
                    userAvatarUrl = userDoc.getString("image") ?: "",
                    postImageUrl  = post.imageUri,
                    content       = post.content
                )

            }


            Result.success(uiModels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }





    override suspend fun createPostInCommunity(communityId: String, post: Post, imageUri: Uri?) {
        remoteDataSource.createPostInCommunity(communityId, post, imageUri)
    }
}
