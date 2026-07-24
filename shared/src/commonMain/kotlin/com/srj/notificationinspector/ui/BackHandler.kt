package com.srj.notificationinspector.ui

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
