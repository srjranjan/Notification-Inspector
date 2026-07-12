package com.srj.notificationinspector.theme

import platform.Foundation.NSUserDefaults

actual object ThemePreferences {
    actual fun saveTheme(themeMode: ThemeMode) {
        NSUserDefaults.standardUserDefaults.setObject(themeMode.name, "theme_mode")
    }

    actual fun loadTheme(): ThemeMode {
        val name = NSUserDefaults.standardUserDefaults.stringForKey("theme_mode") ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
}
