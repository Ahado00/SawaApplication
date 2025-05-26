package com.example.sawaapplication.core.di

import android.content.Context
import com.example.sawaapplication.core.sharedPreferences.AuthInterceptor
import com.example.sawaapplication.core.sharedPreferences.LocationSharedPreference
import com.example.sawaapplication.core.sharedPreferences.OkHTTPBuilder
import com.example.sawaapplication.core.sharedPreferences.TokenProvider
import com.example.sawaapplication.screens.authentication.data.dataSources.remote.FirebaseAuthDataSource
import com.example.sawaapplication.screens.authentication.data.dataSources.repository.AuthRepositoryImpl
import com.example.sawaapplication.screens.authentication.domain.repository.AuthRepository
import com.example.sawaapplication.screens.chat.data.dataSources.remote.MassageRemoteDataSource
import com.example.sawaapplication.screens.chat.data.dataSources.repository.MessageRepositoryImpl
import com.example.sawaapplication.screens.chat.domain.repository.MessageRepository
import com.example.sawaapplication.screens.communities.data.dataSources.remote.CommunityRemoteDataSource
import com.example.sawaapplication.screens.communities.data.repository.CommunityRepositoryImpl
import com.example.sawaapplication.screens.communities.domain.repository.CommunityRepository
import com.example.sawaapplication.screens.communities.domain.useCases.GetUserCreatedCommunitiesUseCase
import com.example.sawaapplication.screens.event.data.dataSources.EventInCommunityRemote
import com.example.sawaapplication.screens.event.data.repository.EventRepositoryImpl
import com.example.sawaapplication.screens.event.domain.repository.EventRepository
import com.example.sawaapplication.screens.home.data.dataSources.remote.HomeRemoteDataSource
import com.example.sawaapplication.screens.home.data.dataSources.repository.HomeRepositoryImpl
import com.example.sawaapplication.screens.home.domain.repository.HomeRepository
import com.example.sawaapplication.screens.notification.data.dataSources.NotificationRemoteDataSource
import com.example.sawaapplication.screens.notification.data.repository.NotificationRepositoryImpl
import com.example.sawaapplication.screens.notification.domain.repository.NotificationRepository
import com.example.sawaapplication.screens.post.data.dataSources.remote.PostsInCommunityRemote
import com.example.sawaapplication.screens.post.data.repository.PostRepositoryImpl
import com.example.sawaapplication.screens.post.domain.repository.PostRepository
import com.example.sawaapplication.screens.post.domain.useCases.CreatePostUseCase
import com.example.sawaapplication.screens.profile.dataSources.remote.ProfileRemoteDataSource
import com.example.sawaapplication.screens.profile.domain.repository.ProfileRepository
import com.example.sawaapplication.screens.profile.domain.useCases.FetchAboutMeUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.sawaapplication.screens.profile.dataSources.repository.ProfileRepositoryImpl

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun provideFirebaseAuthDataSource(
        firebaseAuth: FirebaseAuth
    ): FirebaseAuthDataSource = FirebaseAuthDataSource(firebaseAuth)

    @Provides
    fun provideCommunityRemoteDataSource(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): CommunityRemoteDataSource {
        return CommunityRemoteDataSource(
            firestore,
            firebaseAuth
        )
    }

    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    fun provideCommunityRepository(
        remoteDataSource: CommunityRemoteDataSource,
        firestore: FirebaseFirestore
    ): CommunityRepository {
        return CommunityRepositoryImpl(remoteDataSource, firestore)
    }

    // Provide GetUserCreatedCommunitiesUseCase
    @Provides
    fun provideGetUserCreatedCommunitiesUseCase(
        communityRepository: CommunityRepository
    ): GetUserCreatedCommunitiesUseCase {
        return GetUserCreatedCommunitiesUseCase(communityRepository)
    }

    @Provides
    fun provideAuthRepository( // AuthRepository
        firebaseAuthDataSource: FirebaseAuthDataSource
    ): AuthRepository = AuthRepositoryImpl(firebaseAuthDataSource)

    @Provides
    fun provideTokenProvider(
        firebaseAuth: FirebaseAuth
    ): TokenProvider = TokenProvider(firebaseAuth)

    @Provides
    fun provideAuthInterceptor(
        tokenProvider: TokenProvider
    ): AuthInterceptor = AuthInterceptor(tokenProvider)

    @Provides
    fun provideOkHTTPBuilder(
        authInterceptor: AuthInterceptor
    ): OkHTTPBuilder = OkHTTPBuilder(authInterceptor)

    @Provides
    fun provideEventRemoteDataSource(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): EventInCommunityRemote {
        return EventInCommunityRemote(
            firestore,
            firebaseAuth
        )
    }

    @Provides
    fun provideEventRepository(
        remoteDataSource: EventInCommunityRemote,
    ): EventRepository {
        return EventRepositoryImpl(remoteDataSource)
    }

    @Provides
    fun provideNotificationRemoteDataSource(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): NotificationRemoteDataSource {
        return NotificationRemoteDataSource(
            firestore,
            firebaseAuth
        )
    }

    @Provides
    fun provideNotificationRepository(
        remoteDataSource: NotificationRemoteDataSource,
        firebaseAuth: FirebaseAuth
    ): NotificationRepository {
        return NotificationRepositoryImpl(remoteDataSource, firebaseAuth)
    }


    /**
     * Add two provides
     * 1- PostRemoteDataSource
     * 2- PostRepository
     * */
    @Provides
    fun providePostRemoteDataSource(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): PostsInCommunityRemote {
        return PostsInCommunityRemote(firestore, firebaseAuth)
    }

    @Provides
    fun providePostRepository(
        postRemoteDataSource: PostsInCommunityRemote
    ): PostRepository {
        return PostRepositoryImpl(postRemoteDataSource,
            )
    }

    @Provides
    fun provideCreatePostUseCase(
        postRepository: PostRepository
    ): CreatePostUseCase {
        return CreatePostUseCase(postRepository)
    }

    @Provides
    fun provideLocationSharedPreference(
        @ApplicationContext context: Context
    ): LocationSharedPreference {
        return LocationSharedPreference(context)
    }
//
    @Provides
    fun provideMassagesRepository(
        remoteDataSource: MassageRemoteDataSource,
    ): MessageRepository {
        return MessageRepositoryImpl(remoteDataSource)
    }

    @Provides
    fun provideFirebaseStorage(): FirebaseStorage =
        FirebaseStorage.getInstance()

    @Provides
    fun provideMassageRemoteDataSource(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): MassageRemoteDataSource =
        MassageRemoteDataSource(firestore, storage)


    @Provides
    fun provideProfileRemoteDataSource(
        firestore: FirebaseFirestore
    ): ProfileRemoteDataSource {
        return ProfileRemoteDataSource(
            firestore)
    }
    @Provides
    fun provideProfileRepository(
        profileRemoteDataSource: ProfileRemoteDataSource
    ): ProfileRepository {
        return ProfileRepositoryImpl(profileRemoteDataSource
        )
    }
    @Provides
    fun provideFetchAboutMeUseCase(
        profileRepository: ProfileRepository
    ): FetchAboutMeUseCase {
        return FetchAboutMeUseCase(profileRepository)
    }
    @Provides
    fun provideHomeRemoteDataSource(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): HomeRemoteDataSource {
        return HomeRemoteDataSource(
            firestore,
            firebaseAuth
        )
    }
    @Provides
    fun provideHomeRepository(
        homeRemoteDataSource: HomeRemoteDataSource
    ): HomeRepository {
        return HomeRepositoryImpl(homeRemoteDataSource
        )
    }
}
