# RecipeJar Recipe Organizer

Welcome to the RecipeJar — the local offline recipe organizer.

Keep your recipes in a local repository of HTML files, viewable in any browser and managed by RecipeJar.

## v1 status (Compose Desktop)

**Desktop (JVM) is the v1 target.** Core port is complete for day-to-day use:

| Area | Status |
|------|--------|
| Open HTML recipe repo (`Test/Recipes` works) | Done |
| A–Z index + recipe reader (WebView / HTML fallback) | Done |
| Structured editor + save (HTML roundtrip) | Done |
| Menus, Cmd/Ctrl shortcuts, OS hooks | Done |
| Macros (JSON + legacy txt import) | Done |
| Search + preferences (last repo/recipe, author) | Done |
| Native packaging (DMG / MSI / Deb) | Configured |
| **Android** | **Working prototype** — debug APK loads bundled sample recipes via real HTML serializer + shared UI shell |
| **iOS (iPhone)** | **Working prototype** — Simulator app host (`iosApp`) with the same sample jar + Compose shell |
| **iPad** | **Target platform** (same iOS app binary / universal iPhone+iPad family; layout uses compact shell; full iPad chrome later) |
| **Web** | **Deferred** |
| Full mobile MVP (SAF/iCloud open, edit parity, store polish) | Phase 1A+ |

The classic Java Swing app under `src/` remains for **reference only**. Prefer `./gradlew :composeApp:run` for the active app.

---

## Compose Multiplatform (desktop)

Active code lives in `shared/` + `composeApp/`.

### Requirements

- **JDK 17+** for development builds (required by KCEF / recipe WebView). The Gradle `run` task is pinned to a **Java 17 toolchain** so system Java 11 alone is not enough unless a JDK 17 is installed or auto-provisioned.
- Internet on **first run** (KCEF downloads a Chromium embed under `~/.cache/recipejar/`)
- Optional: OS packaging tools if you build installers (macOS for DMG, WiX/JDK for MSI, etc.)

### Run (end users)

**Preferred:** install or unpack a **packaged app** (bundled runtime — no `JAVA_HOME`, no Gradle):

```bash
# Build a self-contained app image for this OS
./gradlew :composeApp:createDistributable

# Then launch the binary (path may vary slightly by OS/version), e.g. Linux:
# composeApp/build/compose/binaries/main/app/RecipeJar/bin/RecipeJar
```

Or build an installer:

```bash
./gradlew :composeApp:packageDistributionForCurrentOS
# → Deb / MSI / DMG under composeApp/build/compose/binaries/…
```

Double-click the installed app (or the `RecipeJar` launcher in the distributable) like any desktop application.

### Run (developers)

From the repository root:

```bash
# Simplest: one launcher finds JDK 17 and runs the app (or a prebuilt distributable if present)
./recipejar

# Or explicitly:
./gradlew :composeApp:run

# Tests (shared; does not mutate Test/Recipes)
./gradlew :shared:desktopTest

# Android prototype (requires ANDROID_HOME / local.properties sdk.dir)
./gradlew :composeApp:assembleDebug
# → composeApp/build/outputs/apk/debug/composeApp-debug.apk

# iOS prototype framework (then open iosApp in Xcode, or see docs)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Mobile platforms (roadmap)

| Platform | Role |
|----------|------|
| **Android** (phone) | Primary store target; debug prototype ships sample jar |
| **iOS (iPhone)** | Primary store target; Simulator prototype via `iosApp` |
| **iPad** | **Explicit target** — same RecipeJar iOS app supports **iPhone and iPad**; prototype runs on iPad Simulator; larger-canvas layout polish follows phone MVP |
| Desktop (macOS / Windows / Linux) | Secondary / power-user (v1 complete) |

Publishing / first-time store operator steps: [docs/publishing/HUMAN-OPERATOR-GUIDE.md](docs/publishing/HUMAN-OPERATOR-GUIDE.md).

If `./gradlew :composeApp:run` fails with `UnsupportedClassVersionError`, your runtime is older than 17. Use `./recipejar`, install JDK 17, or set `JAVA_HOME` to a 17+ JDK. The Gradle run task also requests a Java 17 toolchain launcher.

First launch may take longer while KCEF installs under `~/.cache/recipejar/kcef-bundle/` and `kcef-cache/` (stable user cache, not the project working directory). A status banner shows download/extract progress. If WebView is not ready, the reader shows HTML source / welcome text instead of a rendered page; restart after install if prompted.

### UI notes (desktop)

- **Alpha index**: vertical Rolodex-edge letter rail (A–Z + Other), matching the classic Swing `JTabbedPane.LEFT` placement — not a horizontal scrolling tab row.
- **Menus**: hybrid — native screen/AWT menu bar on **macOS**; Material in-window menus on **Windows/Linux**.
- **Phone layout**: toggle via the **Phone layout** chip (or Tools menu) to force single-pane mobile-style navigation (index ↔ recipe). Also activates automatically when the window is narrower than 600 dp.

### Open the sample corpus

1. Start the app (`./gradlew :composeApp:run`).
2. Click **Open repository** (or **Tools → Preferences…**).
3. Choose the **`Test/Recipes`** directory in this repo (absolute path).
4. Recipes appear in the A–Z index; select one to read.
5. **Recipe → Toggle Edit** opens a structured form (title, categories, notes, ingredients list, procedure). Notes and procedure accept HTML fragments. Changing the **title** and saving creates a **new** recipe file (original kept). **Save** writes via the HTML serializer and rebuilds `index.html` categories.

Last repository path (absolute), last recipe **per repository**, and optional author name are stored in Java user preferences (`recipejar` node) and restored on the next launch. Only valid directories are remembered; a blank path in Preferences clears the last-repo key without closing the open session.

### Shortcuts (OS-aware)

| Action | Windows / Linux | macOS |
|--------|-----------------|-------|
| New | Ctrl+N | ⌘N |
| Toggle Edit | Ctrl+O | ⌘O |
| Save | Ctrl+S | ⌘S |
| Find… | Ctrl+F | ⌘F |
| Quit / Exit | Recipe → Exit | Recipe → Quit RecipeJar (system ⌘Q reserved) |

macOS reserved accelerators (⌘H hide, ⌘Q quit, ⌘, preferences, ⌘⌥H hide others) are **not** bound by the app. Preferences open from **Tools → Preferences…** without ⌘,.

### Search & preferences

- **Edit → Find…** — substring search over titles and labels (optional notes, ingredients, procedure).
- **Find** menu — field-scoped search variants.
- **Tools → Preferences…** — default repository path and author name (author is written into recipe meta on save when set).
- **Help → About RecipeJar** — version / short description.

### Package native installers

Compose Desktop packaging is configured as package **RecipeJar** `1.0.0` (`composeApp/build.gradle.kts`): description, vendor, KCEF `jvmArgs` (`--add-opens` including macOS-specific opens), macOS `bundleID`, Windows menu group, Linux menu category.

```bash
# Platform-dependent targets (DMG / MSI / Deb as available on the host OS)
./gradlew :composeApp:packageDistributionForCurrentOS

# Or individual formats where supported:
# ./gradlew :composeApp:packageDmg
# ./gradlew :composeApp:packageMsi
# ./gradlew :composeApp:packageDeb
```

Packaged builds use main class `recipejar.MainKt` and the same KCEF JVM opens as the Gradle run task. After install, open a recipe folder the same way (e.g. a copy of `Test/Recipes`). First packaged run may still download/install KCEF components depending on bundling.

### Project layout (rewrite)

| Path | Role |
|------|------|
| `shared/` | Domain, HTML serialize/parse, repository, macros, actions, search |
| `composeApp/` | Desktop UI shell, WebView, dialogs, prefs, `Platform` OS hooks |
| `Test/Recipes/` | Sample recipe corpus (**do not mutate** in automated tests) |
| `src/` | Legacy Java RecipeJar (**reference only**) |

### OS-specific notes

- **macOS**: Screen menu bar preferred; app name set via `apple.awt.application.name`. Shortcuts use Meta (⌘). Extra KCEF `--add-opens` for `sun.lwawt` / `macosx`.
- **Windows / Linux**: Shortcuts use Ctrl (handled via window key events when Material menus are active). Packaging targets MSI and Deb respectively when the host supports them.
- Platform helpers live in `composeApp/.../Platform.kt` (`isMac` / `isWindows` / `isLinux`, primary shortcut, accelerator allow-list).

---

## Legacy Java app (reference)

Sources under `src/recipejar/` and `makefile` / `RecipeJar.jar` are the pre-rewrite Swing app. They are kept for behavior comparison (e.g. HTML shape, macros, Kernel.isOS). **Do not treat them as the build target** for new work.

---

## Android (and other targets) — deferred

v1 is **desktop-only**. No Android target is wired in Gradle yet. When starting mobile:

1. Add `androidTarget()` (and later iOS/web) to `shared` + a mobile UI module; keep domain/HTML/macros in `commonMain`.
2. **Filesystem**: replace directory picker + absolute paths with SAF / document tree (or app-private store + import/export zip).
3. **WebView**: desktop uses KCEF; Android needs a different `RecipeHtmlWebView` actual (system WebView).
4. **Menus / shortcuts**: map `ActionRegistry` to top app bars / overflow; no MenuBar.
5. **Prefs**: `java.util.prefs` is desktop-shaped; use DataStore / multiplatform settings for mobile.
6. **Packaging**: separate from Compose Desktop DMG/MSI/Deb.

Until then, develop and ship against `./gradlew :composeApp:run` and the desktop package tasks above.
