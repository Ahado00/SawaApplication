package com.example.sawaapplication.screens.home.domain.useCases

import com.example.sawaapplication.screens.home.domain.repository.HomeRepository
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(communityId: String, postId: String, commentId: String) {
        repository.deleteComment(communityId, postId, commentId)
    }
}