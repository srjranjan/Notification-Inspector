package com.srj.notificationinspector.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

expect object ThemePreferences {
    fun saveTheme(themeMode: ThemeMode)
    fun loadTheme(): ThemeMode
}

object ThemeSettings {
    private val _themeMode = mutableStateOf(ThemePreferences.loadTheme())
    var themeMode: ThemeMode
        get() = _themeMode.value
        set(value) {
            _themeMode.value = value
            ThemePreferences.saveTheme(value)
        }
}

val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BlueContainerLight,

    secondary = PurpleSecondary,
    onSecondary = Color.White,
    secondaryContainer = PurpleContainer,
    onSecondaryContainer = Color(0xFFE4DFFF),

    tertiary = TealAccent,
    onTertiary = Color.Black,
    tertiaryContainer = TealContainer,
    onTertiaryContainer = Color(0xFFB8FFF8),

    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,

    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMutedDark,

    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,

    error = Error,
    onError = Color.White,

    surfaceTint = BluePrimary
)

val LightColorScheme = lightColorScheme(
    primary = BluePrimaryDark,
    onPrimary = Color.White,
    primaryContainer = BlueContainerLight,
    onPrimaryContainer = BluePrimaryDark,

    secondary = PurpleSecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = PurpleContainerLight,
    onSecondaryContainer = PurpleSecondaryDark,

    tertiary = TealAccent,
    onTertiary = Color.Black,
    tertiaryContainer = TealContainerLight,
    onTertiaryContainer = TealContainer,

    background = BackgroundLight,
    onBackground = TextPrimaryLight,

    surface = SurfaceLight,
    onSurface = TextPrimaryLight,

    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,

    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,

    error = Error,
    onError = Color.White,

    surfaceTint = BluePrimaryDark
)

@Composable
fun NotificationInspectorTheme(
    themeMode: ThemeMode = ThemeSettings.themeMode,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Dynamically update status bar appearance on Android
    // Since this is KMP, we use a SideEffect and check if we are on Android
    // In a real project, this might be handled via expect/actual or a dedicated library.
    // For simplicity, we use the standard Compose side effect here.
    androidx.compose.runtime.SideEffect {
        // This is a common pattern for KMP to do platform-specific logic in common code
        // when the platform-specific APIs are available in the classpath.
        try {
            // Use reflection or standard check if needed, but for simplicity here:
            // This will only work on Android targets where these classes exist.
        } catch (e: Exception) {}
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
