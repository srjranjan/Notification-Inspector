package com.srj.notificationinspector

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform