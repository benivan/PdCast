package com.example.pdcast.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pdcast.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PdCast") }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToPlayer = onNavigateToPlayer,
                onNavigateToAccount = onNavigateToAccount
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Welcome to PdCast\n\nModernized with Jetpack Compose and Media3!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    onNavigateToSearch: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Text("🏠") },
            label = { Text("Home") },
            selected = true,
            onClick = { /* Already on Home */ }
        )
        NavigationBarItem(
            icon = { Text("🔍") },
            label = { Text("Search") },
            selected = false,
            onClick = onNavigateToSearch
        )
        NavigationBarItem(
            icon = { Text("▶️") },
            label = { Text("Player") },
            selected = false,
            onClick = onNavigateToPlayer
        )
        NavigationBarItem(
            icon = { Text("👤") },
            label = { Text("Account") },
            selected = false,
            onClick = onNavigateToAccount
        )
    }
}

@Composable
fun SearchScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Search Screen",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}

@Composable
fun PlayerScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Media Player",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Placeholder for podcast artwork
        Card(
            modifier = Modifier.size(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎵",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Media controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* Skip previous */ }
            ) {
                Text("⏮️", style = MaterialTheme.typography.headlineMedium)
            }
            
            IconButton(
                onClick = { /* Play/Pause */ }
            ) {
                Text(
                    if (isPlaying) "⏸️" else "▶️",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            IconButton(
                onClick = { /* Skip next */ }
            ) {
                Text("⏭️", style = MaterialTheme.typography.headlineMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}

@Composable
fun AccountScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Account Screen",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) {
            Text("Go Back")
        }
    }
}