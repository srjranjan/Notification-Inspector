package com.srj.notificationinspector

import com.google.firebase.messaging.RemoteMessage
import com.srj.notificationinspector.ui.NotificationBannerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

import android.content.Intent
import com.srj.notificationinspector.ui.InspectorActivity

actual typealias PlatformNotificationPayload = RemoteMessage

actual class NotificationInspector actual constructor(private val context: PlatformContext) {
    private val repository = getNotificationRepository(context)

    actual fun capture(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"]
        val body = message.notification?.body ?: message.data["body"]

        // Convert the Firebase data Map to a pretty-printed 4-indent JSON string
        val formattedJson = try {
            JSONObject(message.data as Map<*, *>).toString(4)
        } catch (e: Exception) {
            "{}"
        }

        CoroutineScope(Dispatchers.Default).launch {
            repository.insertLog(
                title = title,
                body = body,
                rawPayload = formattedJson
            )
            // Trigger native Android system drawer notification banner passing native Context
            NotificationBannerEngine.showSystemDrawer(context.androidContext, title, body)
        }
    }

    actual fun launch() {
        val intent = Intent(context.androidContext, InspectorActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.androidContext.startActivity(intent)
    }
}
