package com.example.sawaapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.sawaapplication.core.sharedPreferences.LanguageManager
import com.example.sawaapplication.navigation.AppNavigation
import com.example.sawaapplication.screens.profile.screens.changeAppLocale
import com.example.sawaapplication.screens.profile.vm.ThemeViewModel
import com.example.sawaapplication.ui.theme.SawaApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
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