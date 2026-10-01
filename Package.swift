// swift-tools-version:5.7
import PackageDescription

let package = Package(
    name: "NotificationInspector",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(
            name: "NotificationInspector",
            targets: ["shared"]
        ),
        .library(
            name: "NotificationInspectorNoOp",
            targets: ["sharedNoOp"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "shared",
            url: "https://github.com/srjranjan/Notification-Inspector/releases/download/v1.0.20/shared.xcframework.zip",
            checksum: "6e0c9c9d206964fa3bb17411065b42d0b0f520fbf4a21a817164dfe600954e17"
        ),
        .binaryTarget(
            name: "sharedNoOp",
            url: "https://github.com/srjranjan/Notification-Inspector/releases/download/v1.0.20/sharedNoOp.xcframework.zip",
            checksum: "198f13fed240491ffdbe83a66cbc6cf1ad71bc6f232b0429d58183c5f0dad3cc"
        )
    ]
)
