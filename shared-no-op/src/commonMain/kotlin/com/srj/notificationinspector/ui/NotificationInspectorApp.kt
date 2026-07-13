package com.srj.notificationinspector.ui

import androidx.compose.runtime.Composable
import com.srj.notificationinspector.repository.NotificationRepository

@Composable
fun NotificationInspectorApp(
    repository: NotificationRepository,
    onClose: (() -> Unit)? = null,
    onReplay: ((NotificationLog) -> Unit)? = null
) {
    // No-op UI
}
