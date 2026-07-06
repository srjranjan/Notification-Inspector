package com.srj.notificationinspector.ui

actual object InstantFormatter {
    actual fun format(timestamp: Long): String {
        val date = kotlin.js.Date(timestamp.toDouble())
        // Formats to "YYYY-MM-DD HH:MM:SS" approximately using ISO string modification
        val iso = date.toISOString()
        return iso.replace("T", " ").substringBefore(".")
    }
}
