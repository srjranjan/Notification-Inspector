package com.srj.notificationinspector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.srj.notificationinspector.model.NotificationLog
import com.srj.notificationinspector.repository.NotificationRepository

sealed interface InspectorScreen {
    object ListScreen : InspectorScreen
    data class DetailScreen(val logId: Long) : InspectorScreen
}

@Composable
fun NotificationInspectorApp(
    repository: NotificationRepository,
    onClose: (() -> Unit)? = null
) {
    var currentScreen by remember { mutableStateOf<InspectorScreen>(InspectorScreen.ListScreen) }

    // Define Dark Material 3 theme palette
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFF38BDF8), // Light Sky Blue
        primaryContainer = Color(0xFF0F172A), // Slate 900
        secondary = Color(0xFF0EA5E9),
        background = Color(0xFF0F172A),
        surface = Color(0xFF1E293B), // Slate 800
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(colorScheme = darkColorScheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            when (val screen = currentScreen) {
                is InspectorScreen.ListScreen -> {
                    NotificationListScreen(
                        repository = repository,
                        onNavigateToDetail = { id ->
                            currentScreen = InspectorScreen.DetailScreen(id)
                        }
                    )
                }
                is InspectorScreen.DetailScreen -> {
                    var selectedLog by remember(screen.logId) { mutableStateOf<NotificationLog?>(null) }

                    // Fetch log by ID inside a LaunchedEffect reactively
                    LaunchedEffect(screen.logId) {
                        selectedLog = repository.getLogById(screen.logId)
                    }

                    val log = selectedLog
                    if (log != null) {
                        NotificationDetailScreen(
                            log = log,
                            onNavigateBack = {
                                currentScreen = InspectorScreen.ListScreen
                            }
                        )
                    } else {
                        // Display a sleek loading indicator while the DB fetches the log record
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
