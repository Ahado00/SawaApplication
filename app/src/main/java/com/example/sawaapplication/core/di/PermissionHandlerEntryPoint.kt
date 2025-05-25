package com.example.sawaapplication.core.di

import com.example.sawaapplication.core.permissions.PermissionHandler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PermissionHandlerEntryPoint {
    fun permissionHandler(): PermissionHandler
}