package com.srj.notificationinspector.ui

import androidx.activity.compose.BackHandler
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
import com.srj.notificationinspector.theme.NotificationInspectorTheme
import kotlinx.coroutines.launch

sealed interface InspectorScreen {
    object ListScreen : InspectorScreen
    data class DetailScreen(val logId: Long) : InspectorScreen
    data class EditPayloadScreen(val logId: Long) : InspectorScreen
}

@Composable
fun NotificationInspectorApp(
    repository: NotificationRepository,
    onClose: (() -> Unit)? = null,
    onReplay: ((NotificationLog) -> Unit)? = null,
    onShare: ((String) -> Unit)? = null
) {
    var currentScreen by remember { mutableStateOf<InspectorScreen>(InspectorScreen.ListScreen) }

    NotificationInspectorTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val screen = currentScreen) {
                is InspectorScreen.ListScreen -> {
                    if (onClose != null) {
                        BackHandler {
                            onClose()
                        }
                    }

                    NotificationListScreen(
                        repository = repository,
                        onNavigateToDetail = { id ->
                            currentScreen = InspectorScreen.DetailScreen(id)
                        },
                    )
                }
                is InspectorScreen.DetailScreen -> {
                    var selectedLog by remember(screen.logId) { mutableStateOf<NotificationLog?>(null) }

                    // Fetch log by ID inside a LaunchedEffect reactively
                    LaunchedEffect(screen.logId) {
                        selectedLog = repository.getLogById(screen.logId)
                    }
                    BackHandler {
                        currentScreen = InspectorScreen.ListScreen
                    }

                    val log = selectedLog
                    if (log != null) {
                        NotificationDetailScreen(
                            log = log,
                            onNavigateBack = {
                                currentScreen = InspectorScreen.ListScreen
                            },
                            onReplay = onReplay,
                            onShare = onShare,
                            onEditPayload = { id ->
                                currentScreen = InspectorScreen.EditPayloadScreen(id)
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
                is InspectorScreen.EditPayloadScreen -> {
                    var selectedLog by remember(screen.logId) { mutableStateOf<NotificationLog?>(null) }

                    LaunchedEffect(screen.logId) {
                        selectedLog = repository.getLogById(screen.logId)
                    }
                    BackHandler {
                        currentScreen = InspectorScreen.DetailScreen(screen.logId)
                    }

                    val log = selectedLog
                    if (log != null) {
                        val scope = rememberCoroutineScope()
                        NotificationEditPayloadScreen(
                            log = log,
                            onNavigateBack = {
                                currentScreen = InspectorScreen.DetailScreen(screen.logId)
                            },
                            onReplayWithPayload = { editedLog ->
                                scope.launch {
                                    // No need to manually insert. Replaying triggers the Firebase service
                                    // which will be intercepted and logged as a new notification by the library.
                                    onReplay?.invoke(editedLog)
                                    // Return to list screen so user can see the new log pop up at the top
                                    currentScreen = InspectorScreen.ListScreen
                                }
                            }
                        )
                    } else {
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
