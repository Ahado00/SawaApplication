package com.example.sawaapplication.core.sharedPreferences

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GallerySharedPreference @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun hasRequested(): Boolean {
        return prefs.getBoolean(KEY_GALLERY_REQUESTED, false)
    }

    fun markAsRequested() {
        prefs.edit { putBoolean(KEY_GALLERY_REQUESTED, true) }
    }

    companion object {
        private const val PREF_NAME = "user_prefs" // Using the same preferences file
        private const val KEY_GALLERY_REQUESTED = "gallery_requested"
    }
}