package com.example.pdcast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pdcast.ui.compose.AccountScreen
import com.example.pdcast.ui.compose.HomeScreen
import com.example.pdcast.ui.compose.PlayerScreen
import com.example.pdcast.ui.compose.SearchScreen
import com.example.pdcast.ui.theme.PdCastTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Modern MainActivity using Jetpack Compose instead of XML layouts
 * This replaces the traditional Fragment-based architecture
 */
@AndroidEntryPoint
class MainActivityCompose : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide the action bar since we're using Compose
        supportActionBar?.hide()
        
        setContent {
            PdCastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PdCastApp()
                }
            }
        }
    }
}

@Composable
fun PdCastApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToPlayer = { navController.navigate("player") },
                onNavigateToAccount = { navController.navigate("account") }
            )
        }
        
        composable("search") {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("player") {
            PlayerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("account") {
            AccountScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}