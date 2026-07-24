package com.srj.notificationinspector.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op for iOS (swipe gestures are handled natively by the platform)
}
