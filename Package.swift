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
            checksum: "e44e9ae2cfa291656583f1a19f79c84ffcc04763253928834dec3a1e2736f3ed"
        ),
        .binaryTarget(
            name: "sharedNoOp",
            url: "https://github.com/srjranjan/Notification-Inspector/releases/download/v1.0.20/sharedNoOp.xcframework.zip",
            checksum: "caf5c6d197278333c92e08cd0427fdb1cb9afda7d885108f2faa0e6e30a0c5f1"
        )
    ]
)
