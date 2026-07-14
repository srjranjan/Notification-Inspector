# Contributing to Notification Inspector 🔔

Thank you for your interest in contributing to **Notification Inspector**! Contributions from the community help make this tool better for everyone.

By contributing to this project, you agree to abide by the standard open-source code of conduct, respecting all participants and keeping discussions constructive.

---

## 🛠️ Getting Started

### Prerequisites
To build and run the project locally, you will need:
- **Java Development Kit (JDK) 17** or higher.
- **Android Studio** (latest stable version recommended) with the Kotlin Multiplatform plugin.
- **Xcode** (if you plan to build, test, or contribute to the iOS side of the project).
- **Cocoapods** or **Swift Package Manager** (for iOS linking).

### Setup Instructions
1. **Fork the Repository**: Create a personal fork of this repository on GitHub.
2. **Clone the Fork**:
   ```bash
   git clone https://github.com/your-username/Notification-Inspector.git
   cd Notification-Inspector
   ```
3. **Open in Android Studio**: Open the root folder in Android Studio. Gradle will sync automatically.
4. **Create a Branch**: Create a new branch off `dev` for your changes:
   ```bash
   git checkout dev
   git pull origin dev
   git checkout -b feature/your-feature-name
   # Or for bug fixes:
   # git checkout -b bug/your-fix-name
   ```

---

## 🌿 Branching & Commit Guidelines

### Branch Naming Conventions
Please follow these prefix patterns when creating branches:
- `feature/...` — Adding new functionality or support for a platform.
- `bug/...` — Fixing defects, crashes, or incorrect behavior.
- `chore/...` — Upgrading dependencies, configuring CI, or formatting code.
- `docs/...` — Modifying documentation.

### Commit Messages
We prefer clean, descriptive, and concise commit messages:
- Use the imperative mood (e.g., "Add JSON tree viewer" instead of "Added JSON tree viewer").
- Start with a capital letter and do not end the subject line with a period.
- Keep the subject line under 72 characters.
- If necessary, add a blank line followed by a detailed description.

---

## 🎨 Coding & Style Standards

- **Consistency**: Write code that reads like the surrounding code. Match the existing naming conventions, comment density, and idiom.
- **Kotlin Code**: Adhere to the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- **Swift / iOS Code**: Follow standard Swift API Design Guidelines.
- **Architecture**:
  - Keep the full inspector code in `shared`.
  - Ensure the matched API signature exists and behaves gracefully in `shared-no-op` (returning stubs/empty results) to maintain zero overhead in production builds.

---

## 🧪 Testing

Before submitting a Pull Request, please verify your changes locally:
1. Ensure all Kotlin Multiplatform compile and build tasks pass.
2. If you added business logic or parsing rules, write corresponding unit tests.
3. Test on both **Android** and **iOS** targets to confirm that the changes did not break multiplatform compatibility.

---

## 🚀 Submitting a Pull Request (PR)

1. **Commit and Push**: Commit your changes and push them to your fork.
2. **Open a PR**: Open a Pull Request targeting the `dev` branch of the main repository.
3. **Describe Your Changes**: Provide a clear explanation of what your PR solves, what changes you made, and how to test them. If applicable, add screenshots or videos demonstrating UI changes.
4. **CI Checks**: Ensure all automated GitHub Actions checks pass successfully.
5. **Code Review**: Be responsive to feedback and suggestions from maintainers during the code review process.

Thank you again for contributing to **Notification Inspector**! 🚀
