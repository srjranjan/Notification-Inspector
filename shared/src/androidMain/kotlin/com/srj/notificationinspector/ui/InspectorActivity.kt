package com.srj.notificationinspector.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import com.srj.notificationinspector.PlatformContext
import com.srj.notificationinspector.getNotificationRepository
import com.srj.notificationinspector.theme.DarkColorScheme
import com.srj.notificationinspector.theme.LightColorScheme
import com.srj.notificationinspector.theme.ThemeMode
import com.srj.notificationinspector.theme.ThemeSettings

class InspectorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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

            val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
            val surfaceColor = colorScheme.surface.toArgb()

            // Dynamically update status bar color when theme mode changes
            LaunchedEffect(isDark, surfaceColor) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(surfaceColor)
                    } else {
                        SystemBarStyle.light(surfaceColor, surfaceColor)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(surfaceColor)
                    } else {
                        SystemBarStyle.light(surfaceColor, surfaceColor)
                    }
                )
            }

            NotificationInspectorApp(
                repository = repository,
                onClose = { finish() }
            )
        }
    }
}
