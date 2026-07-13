@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.srj.notificationinspector

import com.srj.notificationinspector.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.srj.notificationinspector.model.NotificationLog
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDelegateProtocol
import platform.objc.sel_registerName

actual typealias PlatformNotificationPayload = NSDictionary


actual class NotificationInspector actual constructor(private val context: PlatformContext) {
    private val repository = getNotificationRepository(context)

    actual fun capture(message: NSDictionary) {
        val aps = message.objectForKey("aps") as? NSDictionary
        val alert = aps?.objectForKey("alert") as? NSDictionary
        val title = alert?.objectForKey("title") as? String ?: aps?.objectForKey("alert") as? String
        val body = alert?.objectForKey("body") as? String

        // Convert Apple NSDictionary payload to pretty-printed JSON String using NSJSONSerialization
        val formattedJson = try {
            val data = NSJSONSerialization.dataWithJSONObject(message, NSJSONWritingPrettyPrinted, null)
            data?.let {
                NSString.create(data = it, encoding = NSUTF8StringEncoding) as String
            } ?: "{}"
        } catch (e: Exception) {
            "{}"
        }

        CoroutineScope(Dispatchers.Default).launch {
            repository.insertLog(
                title = title,
                body = body,
                rawPayload = formattedJson
            )
        }
    }

    actual fun launch() {
        // No-op for iOS (UI hosting can be done manually in SwiftUI / ComposeViewController)
    }

    actual fun replay(log: NotificationLog) {
        // 1. Programmatically re-deliver the payload to the App Delegate
        val app = UIApplication.sharedApplication
        val delegate = app.delegate

        if (delegate != null) {
            // Parse JSON string to NSDictionary
            val payload: NSDictionary? = try {
                val data = (log.rawPayload as NSString).dataUsingEncoding(NSUTF8StringEncoding)
                if (data != null) {
                    NSJSONSerialization.JSONObjectWithData(data, 0, null) as? NSDictionary
                } else null
            } catch (e: Exception) {
                null
            }

            if (payload != null) {
                val selectorWithCompletion = sel_registerName("application:didReceiveRemoteNotification:fetchCompletionHandler:")
                val oldSelector = sel_registerName("application:didReceiveRemoteNotification:")

                if (delegate.respondsToSelector(selectorWithCompletion)) {
                    val delegateProto = delegate as? UIApplicationDelegateProtocol
                    delegateProto?.application(
                        application = app,
                        didReceiveRemoteNotification = payload,
                        fetchCompletionHandler = { _ -> }
                    )
                } else if (delegate.respondsToSelector(oldSelector)) {
                    val delegateProto = delegate as? UIApplicationDelegateProtocol
                    delegateProto?.application(app, payload)
                }
            }
        }

        // 2. Invoke the registered listener if any
        replayListener?.onReplay(log)
    }

    companion object {
        actual var replayListener: NotificationReplayListener? = null
    }
}
