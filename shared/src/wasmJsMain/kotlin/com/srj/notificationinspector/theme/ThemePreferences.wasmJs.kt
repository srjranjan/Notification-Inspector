package com.srj.notificationinspector.theme

import kotlinx.browser.localStorage

actual object ThemePreferences {
    actual fun saveTheme(themeMode: ThemeMode) {
        localStorage.setItem("theme_mode", themeMode.name)
    }

    actual fun loadTheme(): ThemeMode {
        val name = localStorage.getItem("theme_mode") ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
}
