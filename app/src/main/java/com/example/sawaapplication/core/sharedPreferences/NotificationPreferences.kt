package com.example.sawaapplication.core.sharedPreferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.content.edit

class NotificationPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

    fun markAsSeen() {
        prefs.edit { putBoolean("hasUnread", false) }
    }

    fun setUnread() {
        prefs.edit { putBoolean("hasUnread", true) }
    }

    fun hasUnread(): Boolean {
        return prefs.getBoolean("hasUnread", true)
    }

}
