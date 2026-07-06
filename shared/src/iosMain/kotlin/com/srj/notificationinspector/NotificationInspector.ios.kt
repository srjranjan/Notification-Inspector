@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.srj.notificationinspector

import com.srj.notificationinspector.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.Foundation.*

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
}
