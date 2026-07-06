package com.srj.notificationinspector.ui

expect object InstantFormatter {
    fun format(timestamp: Long): String
}

// Public top-level utility function to format timestamps across KMP screens
fun formatTimestamp(timestamp: Long): String {
    return InstantFormatter.format(timestamp)
}
