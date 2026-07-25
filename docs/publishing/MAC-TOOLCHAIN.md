# Mac toolchain (as installed)

Verified on **MacBook Air M4** / macOS **26.3** / 2026-07-24.

First-time publisher steps (accounts, signing, store handoff): [HUMAN-OPERATOR-GUIDE.md](./HUMAN-OPERATOR-GUIDE.md).

## Status

| Component | Status | Location / version |
|-----------|--------|--------------------|
| Xcode | OK | 26.6 (17F113), license accepted |
| iOS SDK / Simulator | OK | iOS **26.5**; devices e.g. iPhone 17 Pro |
| JDK | OK | Temurin **21.0.11+10** (user-local, no Homebrew) |
| Desktop Gradle | OK | `:composeApp:compileKotlinDesktop` succeeded |
| Android SDK | OK | platform-tools, android-35, build-tools 35.0.0 |
| Homebrew | Skipped | Needs admin/sudo; not required |
| Apple Developer signing | Not yet | 0 codesign identities |

## Environment (already in `~/.zprofile`)

```bash
export JAVA_HOME="$HOME/.local/jdks/jdk-21.0.11+10/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

export ANDROID_HOME="$HOME/Library/Android/sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
```

Open a **new terminal** (or `source ~/.zprofile`) after install so `java` / `adb` resolve.

## Quick checks

```bash
xcodebuild -license check
xcrun simctl list devices available | head
java -version
./gradlew :composeApp:compileKotlinDesktop
./gradlew :shared:compileDebugKotlinAndroid
./gradlew :composeApp:assembleDebug
adb version
echo "$ANDROID_HOME"
```

## Notes

- Homebrew was preferred in the plan but **could not be installed** without Administrator `sudo`. Temurin was installed from the Adoptium tarball under `~/.local/jdks/` instead — fully adequate for RecipeJar.
- Xcode.app size on disk can look small; platforms/SDKs live under Xcode bundles and `~/Library/Developer`.
- Keep **≥40–50 GB free** on this 256 GB machine as Gradle caches and simulators grow.
