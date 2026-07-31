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
            url: "https://github.com/srjranjan/Notification-Inspector/releases/download/v1.0.20-alpha01/shared.xcframework.zip",
            checksum: "69947a701b565681dd12dba10f1348ad051824d6b65c2ebda0e8d12ffcc0261b"
        ),
        .binaryTarget(
            name: "sharedNoOp",
            url: "https://github.com/srjranjan/Notification-Inspector/releases/download/v1.0.20-alpha01/sharedNoOp.xcframework.zip",
            checksum: "47583b49111f69778e7e3553c0be230e87e80a31a1ba398fb26e1407dc094735"
        )
    ]
)
