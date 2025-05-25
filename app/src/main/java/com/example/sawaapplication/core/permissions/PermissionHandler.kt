package com.example.sawaapplication.core.permissions

import com.example.sawaapplication.core.sharedPreferences.LocationSharedPreference
import com.example.sawaapplication.core.sharedPreferences.NotificationSharedPreference
import com.example.sawaapplication.core.sharedPreferences.PhotoSharedPreference
import javax.inject.Inject

class PermissionHandler @Inject constructor(
    private val photoPrefs: PhotoSharedPreference,
    private val locationPrefs: LocationSharedPreference,
    private val notificationPrefs: NotificationSharedPreference
){

    fun shouldRequestPhotoPermission(): Boolean = !photoPrefs.hasRequested()
    fun markPhotoPermissionRequested() = photoPrefs.markAsRequested()

    fun shouldRequestLocationPermission(): Boolean = !locationPrefs.hasRequested()
    fun markLocationPermissionRequested() = locationPrefs.markAsRequested()


    fun shouldRequestNotificationPermission(): Boolean = !notificationPrefs.hasRequested()
    fun markNotificationPermissionRequested() = notificationPrefs.markAsRequested()
}