package com.example.sawaapplication.core.sharedPreferences

import android.content.Context

object LanguageManager {

    fun saveLanguage(context: Context, language: String) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("language", language).apply()  // Save language preference
    }

    fun getSavedLanguage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("language", "en") ?: "en"  // Default to "en" if not set
    }
}