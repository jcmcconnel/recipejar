# RecipeJar Recipe Organizer

Welcome to the RecipeJar — the local offline recipe organizer.

Keep your recipes in a local repository of HTML files, viewable in any browser and managed by RecipeJar.

## Compose Multiplatform (desktop)

This tree is the Kotlin Multiplatform / Compose Desktop rewrite. The classic Java app sources remain under `src/` for reference; the active app lives in `shared/` + `composeApp/`.

### Requirements

- JDK 11+
- Internet on first run (KCEF downloads a Chromium embed for recipe WebView)

### Build & run (desktop)

From the repository root:

```bash
# Compile shared + desktop app
./gradlew :shared:compileKotlinDesktop :composeApp:compileKotlinDesktop

# Unit tests (shared)
./gradlew :shared:desktopTest

# Run the desktop app
./gradlew :composeApp:run
```

### Open the sample corpus

1. Start the app (`./gradlew :composeApp:run`).
2. Click **Open repository** (or **Tools → Preferences…**).
3. Choose the **`Test/Recipes`** directory in this repo (absolute path, e.g. `…/Test/Recipes`).
4. Recipes appear in the A–Z index; select one to read (WebView when KCEF is ready, else HTML source).

The last repository path (absolute), last recipe **per repository**, and optional author name are stored in Java user preferences (`recipejar` node) and restored on the next launch. Only valid directories are remembered; a blank path in Preferences clears the last-repo key without closing the open session.

### Search & preferences

- **Edit → Find…** (Ctrl/Cmd+F) — substring search over titles and labels (optional notes, ingredients, procedure).
- **Find** menu — field-scoped search variants.
- **Tools → Preferences…** — default repository path and author name (author is written into recipe meta on save when set).

### Package native installers

Compose Desktop packaging is configured as package **RecipeJar** (`composeApp/build.gradle.kts`).

```bash
# Platform-dependent targets (DMG / MSI / Deb as available on the host OS)
./gradlew :composeApp:packageDistributionForCurrentOS

# Or individual formats where supported:
# ./gradlew :composeApp:packageDmg
# ./gradlew :composeApp:packageMsi
# ./gradlew :composeApp:packageDeb
```

Packaged builds use the same main class (`recipejar.MainKt`) and KCEF JVM opens as the Gradle run task. After install, open a recipe folder the same way (e.g. point at a copy of `Test/Recipes`).

### Project layout (rewrite)

| Path | Role |
|------|------|
| `shared/` | Domain, HTML serialize/parse, repository, macros, actions |
| `composeApp/` | Desktop UI shell, WebView, dialogs, prefs |
| `Test/Recipes/` | Sample recipe corpus (do not treat as golden for mutating tests) |
| `src/` | Legacy Java RecipeJar (reference only) |
