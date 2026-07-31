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
            url: "https://github.com/srjranjan/Notification-Inspector/releases/download/vv1.0.19/shared.xcframework.zip",
            checksum: "309bbaab64e659b9d7bfba91e517957c85a89c0bb7b503a6cb069447a2bb82b7"
        ),
        .binaryTarget(
            name: "sharedNoOp",
            url: "https://github.com/srjranjan/Notification-Inspector/releases/download/vv1.0.19/sharedNoOp.xcframework.zip",
            checksum: "cc241a619c8396af60ffc63d3160f5426263c0472cf0f4f6916007d6d0b88fd2"
        )
    ]
)
