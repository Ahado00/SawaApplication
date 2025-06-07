package com.example.sawaapplication.screens.home.domain.useCases

import com.example.sawaapplication.screens.home.domain.repository.HomeRepository
import com.example.sawaapplication.screens.post.domain.model.Comment
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(communityId: String, postId: String, comment: Comment) {
        repository.addComment(communityId, postId, comment)
    }
}