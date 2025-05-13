package com.example.sawaapplication.screens.profile.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.sawaapplication.core.sharedPreferences.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _isArabic = MutableStateFlow(false)
    val isArabic: StateFlow<Boolean> = _isArabic

    init {
        val lang = LanguageManager.getSavedLanguage(context)
        _isArabic.value = lang == "ar"
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleLanguage() {
        val newLang = if (_isArabic.value) "en" else "ar"
        _isArabic.value = !_isArabic.value
        LanguageManager.saveLanguage(context, newLang)
    }
}