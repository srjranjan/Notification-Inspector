#!/bin/bash
set -e

# Git safety check
if [ "$(git rev-parse --is-inside-work-tree 2>/dev/null)" != "true" ]; then
    echo "❌ Error: Not inside a git repository."
    exit 1
fi

# ==========================================
# CONFIGURATION
# ==========================================
SHARED_FRAMEWORK_NAME="shared"
NOOP_FRAMEWORK_NAME="sharedNoOp"
SHARED_BUILD_DIR="shared/build/XCFrameworks/release"
NOOP_BUILD_DIR="shared-no-op/build/XCFrameworks/release"
SHARED_XCFRAMEWORK_PATH="$SHARED_BUILD_DIR/$SHARED_FRAMEWORK_NAME.xcframework"
NOOP_XCFRAMEWORK_PATH="$NOOP_BUILD_DIR/$NOOP_FRAMEWORK_NAME.xcframework"
SHARED_ZIP_PATH="$SHARED_BUILD_DIR/$SHARED_FRAMEWORK_NAME.xcframework.zip"
NOOP_ZIP_PATH="$NOOP_BUILD_DIR/$NOOP_FRAMEWORK_NAME.xcframework.zip"

# Read version from input argument or fallback to gradle.properties
VERSION=$1
if [ -z "$VERSION" ]; then
    VERSION=$(grep "libVersion=" gradle.properties | cut -d'=' -f2 | tr -d ' ')
fi

if [ -z "$VERSION" ]; then
    echo "❌ Error: Could not determine version. Pass it as an argument: ./publish-ios.sh 1.0.17"
    exit 1
fi

TAG_NAME="v$VERSION"
REPO_OWNER_REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
SHARED_DOWNLOAD_URL="https://github.com/$REPO_OWNER_REPO/releases/download/$TAG_NAME/$SHARED_FRAMEWORK_NAME.xcframework.zip"
NOOP_DOWNLOAD_URL="https://github.com/$REPO_OWNER_REPO/releases/download/$TAG_NAME/$NOOP_FRAMEWORK_NAME.xcframework.zip"

echo "=========================================="
echo "🚀 Publishing iOS Framework Version: $VERSION"
echo "📦 Target Repository: $REPO_OWNER_REPO"
echo "=========================================="

# 1. Clean and build the release XCFrameworks locally
echo "🛠️  [1/5] Building Release XCFrameworks locally..."
./gradlew --stop
./gradlew :shared:clean :shared:assembleSharedReleaseXCFramework :shared-no-op:clean :shared-no-op:assembleSharedNoOpReleaseXCFramework -PpublishTarget=all

# 2. Zip the frameworks
echo "📦 [2/5] Zipping XCFrameworks..."
if [ ! -d "$SHARED_XCFRAMEWORK_PATH" ]; then
    echo "❌ Error: XCFramework not found at $SHARED_XCFRAMEWORK_PATH"
    exit 1
fi
if [ ! -d "$NOOP_XCFRAMEWORK_PATH" ]; then
    echo "❌ Error: XCFramework not found at $NOOP_XCFRAMEWORK_PATH"
    exit 1
fi

rm -f "$SHARED_ZIP_PATH"
rm -f "$NOOP_ZIP_PATH"
cd "$SHARED_BUILD_DIR"
zip -q -r "$SHARED_FRAMEWORK_NAME.xcframework.zip" "$SHARED_FRAMEWORK_NAME.xcframework"
cd - > /dev/null

cd "$NOOP_BUILD_DIR"
zip -q -r "$NOOP_FRAMEWORK_NAME.xcframework.zip" "$NOOP_FRAMEWORK_NAME.xcframework"
cd - > /dev/null

# 3. Calculate Checksums
echo "🔍 [3/5] Computing Swift Package Checksums..."
SHARED_CHECKSUM=$(swift package compute-checksum "$SHARED_ZIP_PATH")
NOOP_CHECKSUM=$(swift package compute-checksum "$NOOP_ZIP_PATH")
echo "   $SHARED_FRAMEWORK_NAME Checksum: $SHARED_CHECKSUM"
echo "   $NOOP_FRAMEWORK_NAME Checksum: $NOOP_CHECKSUM"

# 4. Generate/Update Package.swift
echo "📝 [4/5] Updating Package.swift..."
cat <<EOF > Package.swift
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
            url: "$SHARED_DOWNLOAD_URL",
            checksum: "$SHARED_CHECKSUM"
        ),
        .binaryTarget(
            name: "sharedNoOp",
            url: "$NOOP_DOWNLOAD_URL",
            checksum: "$NOOP_CHECKSUM"
        )
    ]
)
EOF

# 5. Create GitHub Release & Commit Package.swift
echo "🌐 [5/5] Creating GitHub Release & uploading binaries..."

# Commit Package.swift and tag it
git add Package.swift
git commit -m "chore(release): update Package.swift for $TAG_NAME" || echo "No changes in Package.swift to commit"
git push origin HEAD

# Create GitHub Release with both binary assets
gh release create "$TAG_NAME" "$SHARED_ZIP_PATH" "$NOOP_ZIP_PATH" \
  --title "Release $TAG_NAME" \
  --notes "iOS Binary XCFrameworks for version $VERSION." \
  --clobber

echo "=========================================="
echo "SUCCESS! 🎉"
echo "iOS Release $TAG_NAME published to GitHub."
echo "Remote developers can now import via SPM using:"
echo "URL: https://github.com/$REPO_OWNER_REPO"
echo "Tag: $TAG_NAME"
echo "=========================================="
