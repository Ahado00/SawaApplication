package com.example.sawaapplication.core.sharedPreferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationSharedPreference @Inject constructor( @ApplicationContext context : Context) {
    private val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

    fun hasRequested(): Boolean = prefs.getBoolean("notification_permission_requested", false)

    fun markAsRequested() {
        prefs.edit().putBoolean("notification_permission_requested", true).apply()
    }
}