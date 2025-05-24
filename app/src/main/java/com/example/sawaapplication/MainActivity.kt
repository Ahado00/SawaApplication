package com.example.sawaapplication

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.sawaapplication.core.sharedpreferences.LanguageManager
import com.example.sawaapplication.navigation.AppNavigation
import com.example.sawaapplication.screens.profile.presentation.screens.changeAppLocale
import com.example.sawaapplication.screens.profile.presentation.vm.ThemeViewModel
import com.example.sawaapplication.ui.theme.SawaApplicationTheme
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        // Enable verbose logging for debugging (remove in production)
        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        // Initialize with your OneSignal App ID
        OneSignal.initWithContext(this, "cf902765-3bc6-4eab-84cb-307d5db55cd1")

        // Use this method to prompt for push notifications.
        // We recommend removing this method after testing and instead use In-App Messages to prompt for notification permission.
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }

        // Set saved language but don't trigger recreate loop
        val savedLang = LanguageManager.getSavedLanguage(this)
        changeAppLocale(this, savedLang)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            SawaApplicationTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    navController = navController,
                    changeAppTheme = { themeViewModel.toggleTheme() },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

