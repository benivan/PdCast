package com.example.pdcast.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pdcast.ui.MainViewModel

/**
 * Enhanced Player Screen with modern Media3 integration
 * Shows the improved UI and media controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedPlayerScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Text("◀️", style = MaterialTheme.typography.headlineSmall)
            }
            
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            // Placeholder for menu
            Box(modifier = Modifier.size(48.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Large artwork with modern styling
        Card(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎵",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Media3 Player",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Episode information
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.currentEpisode?.title ?: "Sample Episode Title",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = uiState.currentPodcast?.feedTitle ?: "Sample Podcast Name",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Progress bar with time indicators
        Column {
            Slider(
                value = if (uiState.duration > 0) uiState.playbackPosition.toFloat() / uiState.duration else 0f,
                onValueChange = { /* Handle seek */ },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(uiState.playbackPosition),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatTime(uiState.duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Enhanced media controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skip backward
            IconButton(
                onClick = { /* Skip backward 10s */ },
                modifier = Modifier.size(56.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⏪", style = MaterialTheme.typography.headlineSmall)
                    Text("10s", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            // Previous track
            IconButton(
                onClick = { /* Previous track */ },
                modifier = Modifier.size(56.dp)
            ) {
                Text("⏮️", style = MaterialTheme.typography.headlineMedium)
            }
            
            // Play/Pause (larger)
            FilledTonalButton(
                onClick = { /* Play/Pause */ },
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    if (isPlaying) "⏸️" else "▶️",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            
            // Next track
            IconButton(
                onClick = { /* Next track */ },
                modifier = Modifier.size(56.dp)
            ) {
                Text("⏭️", style = MaterialTheme.typography.headlineMedium)
            }
            
            // Skip forward
            IconButton(
                onClick = { /* Skip forward 30s */ },
                modifier = Modifier.size(56.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⏩", style = MaterialTheme.typography.headlineSmall)
                    Text("30s", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Additional controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Add to favorites */ }) {
                Text("❤️", style = MaterialTheme.typography.headlineMedium)
            }
            
            IconButton(onClick = { /* Share */ }) {
                Text("📤", style = MaterialTheme.typography.headlineMedium)
            }
            
            IconButton(onClick = { /* Speed control */ }) {
                Text("⚡", style = MaterialTheme.typography.headlineMedium)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Status indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "✨ Enhanced with Media3 & Compose",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Format time in milliseconds to MM:SS format
 */
private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}