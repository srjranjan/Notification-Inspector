package com.srj.notificationinspector

import com.google.firebase.messaging.RemoteMessage
import com.srj.notificationinspector.ui.NotificationBannerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        Log.d("NotificationInspector", "Replaying notification ID: ${log.id} (Title: ${log.title})")

        // 1. Show the local drawer notification banner again (with a "Replayed" header)
        try {
            NotificationBannerEngine.showSystemDrawer(
                context = context.androidContext,
                title = log.title,
                body = log.body,
                headerOverride = "Notification Replayed 🔄"
            )
            Log.d("NotificationInspector", "System drawer notification replayed successfully")
        } catch (e: Exception) {
            Log.e("NotificationInspector", "Failed to show system drawer notification for replay", e)
        }

        // 2. Programmatically re-deliver the payload to the host app's FirebaseMessagingService
        val serviceClassName = findMessagingServiceClass()
        if (serviceClassName != null) {
            Log.d("NotificationInspector", "Discovered FirebaseMessagingService subclass: $serviceClassName")
            try {
                // Dynamically instantiate the client's service subclass
                val serviceClass = Class.forName(serviceClassName)
                val serviceInstance = serviceClass.getDeclaredConstructor().newInstance() as com.google.firebase.messaging.FirebaseMessagingService

                // Use reflection to attach the base context so that any context-dependent helper methods
                // (e.g. getSystemService, getApplicationContext) inside the service's onMessageReceived run safely
                val attachMethod = ContextWrapper::class.java.getDeclaredMethod("attachBaseContext", Context::class.java).apply {
                    isAccessible = true
                }
                attachMethod.invoke(serviceInstance, context.androidContext)
                Log.d("NotificationInspector", "Successfully attached base context to service instance")

                // Reconstruct the RemoteMessage Bundle payload
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
                        Log.e("NotificationInspector", "Error parsing log.rawPayload JSON keys into Bundle", e)
                    }
                }

                // Wrap in standard Firebase RemoteMessage object
                val remoteMessage = RemoteMessage(bundle)

                // Directly invoke the client's onMessageReceived logic
                Log.d("NotificationInspector", "Invoking onMessageReceived on client's service subclass")
                serviceInstance.onMessageReceived(remoteMessage)
                Log.d("NotificationInspector", "onMessageReceived invocation finished successfully")
            } catch (e: Exception) {
                Log.e("NotificationInspector", "Error programmatically replaying to client's FirebaseMessagingService", e)
            }
        } else {
            Log.w("NotificationInspector", "No FirebaseMessagingService subclass found in AndroidManifest.xml. Skipping programmatic FCM delivery.")
        }

        // 3. Invoke the registered listener if any
        try {
            replayListener?.onReplay(log)
            Log.d("NotificationInspector", "Replay listener invoked successfully")
        } catch (e: Exception) {
            Log.e("NotificationInspector", "Error invoking manual replay listener", e)
        }
    }

    private fun findMessagingServiceClass(): String? {
        val intent = Intent("com.google.firebase.MESSAGING_EVENT").apply {
            setPackage(context.androidContext.packageName)
        }
        val resolveInfo = context.androidContext.packageManager.queryIntentServices(intent, 0)
        return resolveInfo.firstOrNull()?.serviceInfo?.name
    }

    actual companion object {
        actual var replayListener: NotificationReplayListener? = null
    }
}
