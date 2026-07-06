package com.srj.notificationinspector.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import io.github.srjranjan.shared.generated.resources.Res
import io.github.srjranjan.shared.generated.resources.ic_logo

object NotificationBannerEngine {
    private const val CHANNEL_ID = "notification_inspector_channel"
    private const val CHANNEL_NAME = "Notification Inspector Logs"
    private const val NOTIFICATION_ID = 4821

    fun showSystemDrawer(context: Context, title: String?, body: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android O (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows intercepted push notifications on-device"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Target the library's InspectorActivity directly
        val launchIntent = Intent(context, InspectorActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = launchIntent.let {
            val flags =
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            PendingIntent.getActivity(context, 0, it, flags)
        }

        // Simple default android app icon or fallback
        val iconRes = Res.drawable.ic_logo.hashCode()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title ?: "Interception Triggered 🔔")
            .setContentText(body ?: "A new push notification has been logged.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setAutoCancel(true)
            .apply {
                if (pendingIntent != null) {
                    setContentIntent(pendingIntent)
                }
            }
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
