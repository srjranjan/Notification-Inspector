package com.srj.notificationinspector.ui

actual object InstantFormatter {
    actual fun format(timestamp: Long): String {
        // Safe, robust fallback representation for WasmJS compiled output
        return "Time: $timestamp ms"
    }
}
