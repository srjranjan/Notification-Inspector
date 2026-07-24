@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.srj.notificationinspector

import com.srj.notificationinspector.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.srj.notificationinspector.model.NotificationLog
import platform.UIKit.*
import platform.Foundation.*
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
                    NSJSONSerialization.JSONObjectWithData(data, 0UL, null) as? NSDictionary
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
                        didReceiveRemoteNotification = payload as Map<Any?, *>,
                        fetchCompletionHandler = { _ -> }
                    )
                } else if (delegate.respondsToSelector(oldSelector)) {
                    val delegateProto = delegate as? UIApplicationDelegateProtocol
                    delegateProto?.application(app, payload as Map<Any?, *>)
                }
            }
        }

        // 2. Invoke the registered listener if any
        replayListener?.onReplay(log)
    }

    actual fun shareText(text: String) {
        val window = UIApplication.sharedApplication.keyWindow ?: UIApplication.sharedApplication.windows.firstOrNull() as? platform.UIKit.UIWindow
        val rootViewController = window?.rootViewController
        if (rootViewController != null) {
            val activityViewController = platform.UIKit.UIActivityViewController(
                activityItems = listOf(text),
                applicationActivities = null
            )

            // For iPad, UIActivityViewController needs a source view or bar button item to be presented correctly
            activityViewController.popoverPresentationController?.sourceView = rootViewController.view

            rootViewController.presentViewController(
                viewControllerToPresent = activityViewController,
                animated = true,
                completion = null
            )
        }
    }

    actual companion object {
        actual var replayListener: NotificationReplayListener? = null
    }
}
