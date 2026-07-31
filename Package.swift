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
            checksum: "f95e6eac51c2dd79200115be953b9f44e61e49fcde3906e8c5693edb54827097"
        ),
        .binaryTarget(
            name: "sharedNoOp",
            url: "https://github.com/srjranjan/Notification-Inspector/releases/download/v1.0.20/sharedNoOp.xcframework.zip",
            checksum: "1a01299e98d6bc7de5d98b5b7b567bc2bdf1368b89e021868dd5f1563531256f"
        )
    ]
)
