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
            checksum: "01bd64480e96868e3589763ba8e6d9cffa7b5cecb3a5929cc53999b6abd1a217"
        ),
        .binaryTarget(
            name: "sharedNoOp",
            url: "https://github.com/srjranjan/Notification-Inspector/releases/download/vv1.0.19/sharedNoOp.xcframework.zip",
            checksum: "08afc36cadcd53f4c7029b4a82b13f30f2cdbcf46508543ba2f9e63addc5362e"
        )
    ]
)
