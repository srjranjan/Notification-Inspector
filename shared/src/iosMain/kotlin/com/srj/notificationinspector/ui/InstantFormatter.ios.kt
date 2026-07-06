package com.srj.notificationinspector.ui

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970

actual object InstantFormatter {
    private val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd HH:mm:ss"
    }

    actual fun format(timestamp: Long): String {
        val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
        return formatter.stringFromDate(date)
    }
}
