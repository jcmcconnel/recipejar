# RecipeJar frontend test diary

- Inventoried Compose desktop vs the Swing reference. Already present: recipe CRUD, search, macros, readonly HTML fragments, units manager, macro manager, footer variants. Missing: appearance prefs (UI locked to Material pink/purple), Help → GitHub, and the Unit Converter.
- Added catalog-driven `UnitConverter` (cups↔oz, oz↔ml, user-added units) and Tools → Unit Converter.
- Added appearance schemes (default Forest; Ocean/Slate/Warm; Rose keeps Material pink). Preferences persist scheme + dark toggle; `App` applies them at the Material wrapper.
- Wired Help → `https://github.com/jcmcconnel/recipejar` (menu + serialize footers). Macro manager already persisted via `MacroStore`; added list/add/save/reload test.
- New/extended tests drive shipped APIs on temp dirs only.
- `./gradlew :shared:desktopTest :composeApp:desktopTest` — BUILD SUCCESSFUL twice (`desktopTest-1.log`, `--rerun-tasks` `desktopTest-2.log`).
- `./recipejar` twice: both stayed up, opened `Test/Recipes`, KCEF initialized. `screencapture` failed (`could not create image from display`) — no window readback in this session; not a blank captured window.

## Permission-retry (2026-08-17)

- Re-launched `./recipejar` twice after extra TCC. Full-display `screencapture -x` still: `could not create image from display`. Window-id `screencapture -l` worked: `{SCRATCH}/launch.png` and `launch-2.png` show the RecipeJar index + Test4 reader, Forest green headings (not pink).
- CoreGraphics listed `owner=RecipeJar name=RecipeJar` 800×600. KCEF initialized both runs.
- Menu driving blocked: `osascript is not allowed assistive access.` / `(-1719)`. Process listing works; AX menu clicks do not. Did not invent clicks. Grant still needed: Accessibility for iTerm2/Grok (osascript). Screen Recording still needed for full-display capture.
- `./gradlew :shared:desktopTest :composeApp:desktopTest` green twice (second `--rerun-tasks`). No new product defect from the live window.

## After iTerm2 restart (2026-08-17)

- Screen Recording works (`screencapture -x`). RecipeJar menu AX works (`Recipe / Edit / Macros / Tools / Help`).
- Live clicks: Tools → Preferences / Units / Unit Converter; Macros → Manage Macros; Edit → Find; Help → RecipeJar on GitHub. Status banner: `Opened https://github.com/jcmcconnel/recipejar`.
- Defect: dialogs used default Material purple and Appearance sat below the fold. Fix: wrap App+dialogs in `AppearanceTheme`; move Color scheme above Welcome. Re-shot Preferences (Forest (green) + green Save) and Converter (green Convert).
- Compose dialog text fields are not in the AX tree (could not type into Find). Menu-driven open of Search still works.

## iOS Simulator pass (2026-08-17)

- Host is `ContentView` → `MainViewController` → `MobilePrototypeApp` (not desktop `Main.kt`). Library is Application Support / sample jar. Added `IosHostStructureTest`.
- `linkDebugFrameworkIosSimulatorArm64` + `xcodebuild -scheme iosApp` for iPhone 17 Pro Simulator: **BUILD SUCCEEDED**.
- Launched `org.recipejar.app` twice. `simctl io screenshot` both times: compact shell, menus Recipe/Edit/Macros/Tools/Help in Forest green, letter B, Banana Bread, Bread/Breakfast categories — not blank, not Material pink.
- No tap API (`simctl ui` has no tap; Compose AX not visible). Did not invent taps.
- `./gradlew :shared:desktopTest :composeApp:desktopTest` green twice (`--rerun-tasks` on the second).
