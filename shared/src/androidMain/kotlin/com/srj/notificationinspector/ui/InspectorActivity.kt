package com.srj.notificationinspector.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import com.srj.notificationinspector.NotificationInspector
import com.srj.notificationinspector.PlatformContext
import com.srj.notificationinspector.getNotificationRepository
import com.srj.notificationinspector.theme.DarkColorScheme
import com.srj.notificationinspector.theme.LightColorScheme
import com.srj.notificationinspector.theme.ThemeMode
import com.srj.notificationinspector.theme.ThemeSettings

class InspectorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge first thing
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val platformContext = PlatformContext(applicationContext)
        val repository = getNotificationRepository(platformContext)

        setContent {
            val themeMode = ThemeSettings.themeMode
            val isSystemDark = isSystemInDarkTheme()

            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Dynamically update system bar styles when isDark changes
            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { isDark },
                    navigationBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { isDark }
                )
                onDispose {}
            }

            NotificationInspectorApp(
                repository = repository,
                onClose = { finish() },
                onReplay = { log ->
                    NotificationInspector(platformContext).replay(log)
                },
                onShare = { text ->
                    NotificationInspector(platformContext).shareText(text)
                }
            )
        }
    }
}
