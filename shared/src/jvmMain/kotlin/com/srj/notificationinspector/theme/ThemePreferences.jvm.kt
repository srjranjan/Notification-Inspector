package com.srj.notificationinspector.theme

actual object ThemePreferences {
    private var inMemoryTheme = ThemeMode.SYSTEM

    actual fun saveTheme(themeMode: ThemeMode) {
        inMemoryTheme = themeMode
    }

    actual fun loadTheme(): ThemeMode = inMemoryTheme
}
