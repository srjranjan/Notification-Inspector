package com.srj.notificationinspector.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual object InstantFormatter {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    actual fun format(timestamp: Long): String {
        return formatter.format(Date(timestamp))
    }
}
