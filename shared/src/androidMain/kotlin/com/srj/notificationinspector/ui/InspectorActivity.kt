package com.srj.notificationinspector.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.srj.notificationinspector.PlatformContext
import com.srj.notificationinspector.getNotificationRepository

class InspectorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val platformContext = PlatformContext(applicationContext)
        val repository = getNotificationRepository(platformContext)
        setContent {
            NotificationInspectorApp(
                repository = repository,
                onClose = { finish() }
            )
        }
    }
}
