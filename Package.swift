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
            checksum: "10e3bd235eaf9ff414a5547fe925ef91d33eb1dd2ddf2e43c17be680bb7d1a33"
        ),
        .binaryTarget(
            name: "sharedNoOp",
            url: "https://github.com/srjranjan/Notification-Inspector/releases/download/v1.0.20/sharedNoOp.xcframework.zip",
            checksum: "3010a55a4fd2ebcdf93fe0aff6d4a0879998a513c2557500a054b4c8395bc3ad"
        )
    ]
)
