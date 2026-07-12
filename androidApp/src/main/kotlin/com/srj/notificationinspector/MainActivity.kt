package com.srj.notificationinspector

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import com.srj.notificationinspector.theme.DarkColorScheme
import com.srj.notificationinspector.theme.LightColorScheme
import com.srj.notificationinspector.theme.ThemeMode
import com.srj.notificationinspector.theme.ThemeSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

            App(PlatformContext(this))
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(PlatformContext(androidx.compose.ui.platform.LocalContext.current))
}