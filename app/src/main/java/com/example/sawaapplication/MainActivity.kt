package com.example.sawaapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.sawaapplication.navigation.AppNavigation
import com.example.sawaapplication.ui.theme.SawaApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            SawaApplicationTheme {
                AppNavigation(
                    navController = navController,
                )
            }
        }
    }
}
