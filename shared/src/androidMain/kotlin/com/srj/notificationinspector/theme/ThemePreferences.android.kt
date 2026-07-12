package com.srj.notificationinspector.theme

import android.content.Context
import android.content.SharedPreferences

actual object ThemePreferences {
    var context: Context? = null

    private val prefs: SharedPreferences?
        get() = context?.getSharedPreferences("notification_inspector_theme_prefs", Context.MODE_PRIVATE)

    actual fun saveTheme(themeMode: ThemeMode) {
        prefs?.edit()?.putString("theme_mode", themeMode.name)?.apply()
    }

    actual fun loadTheme(): ThemeMode {
        val name = prefs?.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
}
