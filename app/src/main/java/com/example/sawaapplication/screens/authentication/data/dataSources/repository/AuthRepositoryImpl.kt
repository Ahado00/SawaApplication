package com.example.sawaapplication.screens.authentication.data.dataSources.repository


import com.example.sawaapplication.screens.authentication.data.dataSources.remote.FirebaseAuthDataSource
import com.example.sawaapplication.screens.authentication.domain.repository.AuthRepository
import javax.inject.Inject


class AuthRepositoryImpl @Inject constructor(private val firebaseAuthDataSource: FirebaseAuthDataSource) :
    AuthRepository {
    override suspend fun signUp(name: String, email: String, password: String): Result<Unit> {
        return try {
            firebaseAuthDataSource.signUp(name, email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val isLoginSuccessful = firebaseAuthDataSource.login(email, password)
            Result.success(isLoginSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuthDataSource.sendPasswordResetEmail(email)
    }

    override fun logOut() {
        firebaseAuthDataSource.logOut()
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuthDataSource.getCurrentUserId()
    }
}