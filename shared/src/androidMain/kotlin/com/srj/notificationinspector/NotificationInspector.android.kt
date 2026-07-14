package com.srj.notificationinspector

import com.google.firebase.messaging.RemoteMessage
import com.srj.notificationinspector.ui.NotificationBannerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

import android.content.Intent
import android.os.Bundle
import com.srj.notificationinspector.ui.InspectorActivity
import com.srj.notificationinspector.model.NotificationLog

actual typealias PlatformNotificationPayload = RemoteMessage

actual class NotificationInspector actual constructor(private val context: PlatformContext) {
    private val repository = getNotificationRepository(context)

    actual fun capture(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"]
        val body = message.notification?.body ?: message.data["bigMessage"]?:message.data["smallMessage"]

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

    actual fun replay(log: NotificationLog) {
        // 1. Show the local drawer notification banner again (with a "Replayed" header)
        NotificationBannerEngine.showSystemDrawer(
            context = context.androidContext,
            title = log.title,
            body = log.body,
            headerOverride = "Notification Replayed 🔄"
        )

        // 2. Programmatically re-deliver the payload to the host app's FirebaseMessagingService
        val serviceClass = findMessagingServiceClass()
        if (serviceClass != null) {
            val bundle = Bundle().apply {
                putString("from", "notification_inspector_replay")
                putString("google.message_id", "m_replay_${System.currentTimeMillis()}")

                // Reconstruct notification keys
                if (log.title != null) putString("gcm.n.title", log.title)
                if (log.body != null) putString("gcm.n.body", log.body)
                putString("gcm.n.e", "1")

                // Parse custom JSON payload keys into bundle extras
                try {
                    val json = JSONObject(log.rawPayload)
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        putString(key, json.optString(key))
                    }
                } catch (e: Exception) {
                    // Ignore parsing errors
                }
            }

            val triggerIntent = Intent("com.google.android.c2dm.intent.RECEIVE").apply {
                setClassName(context.androidContext.packageName, serviceClass)
                putExtras(bundle)
            }
            context.androidContext.startService(triggerIntent)
        }

        // 3. Invoke the registered listener if any
        replayListener?.onReplay(log)
    }

    private fun findMessagingServiceClass(): String? {
        val intent = Intent("com.google.firebase.MESSAGING_EVENT").apply {
            setPackage(context.androidContext.packageName)
        }
        val resolveInfo = context.androidContext.packageManager.queryIntentServices(intent, 0)
        return resolveInfo.firstOrNull()?.serviceInfo?.name
    }

    companion object {
        actual var replayListener: NotificationReplayListener? = null
    }
}
