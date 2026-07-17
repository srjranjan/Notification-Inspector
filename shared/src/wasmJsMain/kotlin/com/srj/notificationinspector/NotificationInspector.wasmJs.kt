package com.srj.notificationinspector

import com.srj.notificationinspector.model.NotificationLog

actual typealias PlatformNotificationPayload = Any

actual class NotificationInspector actual constructor(context: PlatformContext) {
    actual fun capture(message: Any) {
        // No-op stub for WasmJS/Web target
    }

    actual fun launch() {
        // No-op stub for WasmJS/Web target
    }

    actual fun replay(log: NotificationLog) {
        replayListener?.onReplay(log)
    }

    actual fun shareText(text: String) {
        // Accessing navigator.clipboard in WasmJS
        // Note: In some environments this might need a JS-interop wrapper,
        // but for now we follow the same pattern as JS for consistency.
        js("window.navigator.clipboard.writeText(text)")
    }

    companion object {
        actual var replayListener: NotificationReplayListener? = null
    }
}
