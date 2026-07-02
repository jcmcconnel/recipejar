# RecipeJar Full Rewrite Plan

**Date**: 2026-06-28  
**Goal**: Full rewrite as a universal (iOS, Android, Windows, Linux, macOS) recipe organizer that feels like a digital replacement for a box of index cards.  
**Core Constraint (Non-Negotiable)**: The on-disk format must remain (or be highly compatible with) the existing plain HTML + index.html structure. The application is optional for access; a browser on `index.html` must continue to work. Data must stay local, plain-text, family-owned, and trivially recoverable.

This document is the starting point for discussion. It uses the current Java Swing implementation as reference.

## Decisions Log
- **2026-06-28**: Selected **Kotlin Multiplatform + Compose Multiplatform** as primary stack.
- **2026-06-28**: Desktop (JVM + Compose for Desktop) prototype first; add Android then iOS after core validation. Mobile FS challenges deferred.

## 1. What to Preserve (and Why)

### Storage & File Format (Highest Priority)
- Repository = a user-chosen directory containing:
  - `index.html` (catalog with letter sections A-Z/0 + category `<ul id="Label">` sublists)
  - `*.html` individual recipe "cards" (XHTML 1.0 Transitional, self-contained)
  - Supporting assets: `style/*.css`, optional `images/`, etc.
  - Config-ish files that can live in-repo or app prefs: `macros.txt` (or successor), `units.txt`, templates.
- Recipe HTML structure (see examples in `Test/Recipes/`):
  - Meta: `labels`, `created`, `last saved`, `generator`
  - Sections via `id` or class: `header`, `notes`, `ingredients` (structured `<ul><li><span class="qty">...</span> <span class="unit">...</span> <span class="name">...</span></li>`), `procedure`
  - Multiple footers (`program-footer`, `browser-footer`, `export-footer`) using `[MACRO]` placeholders
  - Title in `<title>` and `<h1>`
- Index generation/maintenance logic: alpha buckets by first letter of title, plus label-based sublists. Recipes can appear under multiple categories.
- Direct editability/recoverability: user or another tool can edit the `.html` files manually and the index can be regenerated.
- No external DB, no cloud requirement, no proprietary binary format.

### UI Paradigms
- **Alpha-tab index**: A-Z (and numeric) tabs/panes showing the catalog as hyperlinked lists. Feels like a physical card catalog or Rolodex/address book. User likes this.
- **Single main content pane with modal swap**: Editor and read-only view occupy the same space and are swapped (not side-by-side tabs or separate windows). Current `JSplitPane` (left index, right reader/editor) + `setRightComponent` toggle.
- **Simple overall layout**: Minimize chrome. Index on left (or collapsible), main area for the current recipe.
- Read-only rendered view (current HTML via pane or equivalent).
- **HTML code view prioritized** for prototype. WYSIWYG is acceptable later.
- Ingredient/unit tables currently provide data consistency (structured `Ingredient` model); keep this for editing convenience but treat HTML round-tripping as source of truth where possible.

### Action & Macro System (Key Extensibility Point)
- Current `ActionRegistry` (string ID → `javax.swing.Action`) + menu assembly.
- `ActionIds` as a central catalog.
- `MacroTextAction` + `macros.txt`: user-defined text actions supporting `[SELECTION]`, `[INPUT:Prompt]`, `[COLOR]`, literal text, accelerators, mnemonics. Loaded at startup into "Macros" menus + popup.
- `AbstractTextAction` base for focus/caret-aware enablement.
- `FileRecipeActions` helpers for context (enable/disable on open recipe).
- **Requirement**: Highly configurable menuing/action system inspired by Arachnophilia (customizable actions that appear in menus/toolbars, powerful text macros). At minimum keep the registry paradigm (or equivalent). Explicit core actions + extensible user text actions.

Current examples live in:
- `src/recipejar/actions/`
- `src/recipejar/lib/MacroTextAction.java`, `AbstractTextAction.java`
- `src/macros.txt`
- Registration and menu building in `MainFrame.java`, `EditorPanel.java`

### Other Keepable Elements
- Preferences/settings dialog (user info, paths, search defaults, LAF, etc.).
- Unit converter (low priority, post-MVP).
- Search across titles/labels/notes/ingredients/procedures.
- Templates (`recipe.template`, `index.template`) for new files.
- CSS theming per-repo (`style/default.css`, `index.css`).
- Future-proof output: the `.html` files themselves are the deliverable.

## 2. What Changes / New Requirements

- **Universal platform support**: One (or very few) codebases targeting iOS, Android, Windows, Linux, macOS.
- **Modern editor capabilities**: Prioritize raw HTML code editing (power users). Optional WYSIWYG. Reliable read-only rendered preview. Must handle the existing structured sections gracefully.
- **Better cross-platform FS story**: Desktop has easy dir access + watching. Mobile (especially iOS) has heavy sandboxing. Solutions needed for "open this folder of cards" UX (document picker, cloud-synced folders, full export of repo as zip/dir, scoped storage + "import recipes" flows).
- **Configurable action system evolution**: Expand beyond Swing `Action`. Support command palette, keybinding customization?, context-sensitive actions, dynamic menu contribution. User-defined macros should be powerful and easy to edit.
- **Settings/preferences**: Definitely needed (paths, defaults, appearance, macro editor UI, sync behavior, etc.).
- **Performance/scale**: Current approach rescans or reparses; new version should be efficient for hundreds of recipes.
- **Packaging & distribution**: Native installers, app stores for mobile, sideloading for desktop.
- **Maintainability**: Move away from 2008-era Swing + custom XHTML parser if possible, while preserving output compatibility.

## 3. Tech Stack Options & Evaluation

The ideal stack must:
- Support **all 5 platforms** from largely shared code.
- Provide excellent **local filesystem** access (especially desktop).
- Make **HTML source editing + preview** natural and high-quality.
- Support rich, **configurable command/action** systems (menus, shortcuts, macros).
- Allow **simple, card-catalog-like UI** (tabs or equivalent + pane swapping).
- Produce reasonably small, native-feeling apps.
- Have good long-term viability.

### Primary Options (as of 2026)

**1. Tauri 2 (Rust core + web frontend: Svelte/React/Solid/Vue/TS) — Strong Recommendation for this project**
- Desktop (Win/mac/Linux): First-class. Uses system webviews (not bundled Chromium). Extremely small binaries, fast startup, low memory, excellent security model (Rust for privileged FS ops).
- Mobile (iOS/Android): Supported via Tauri v2 (commands run in Rust; UI in web tech). `tauri android dev` / `tauri ios dev`. Plugins can add native Swift/Kotlin. Not as "native UI" as pure mobile frameworks but very usable and improving. Real apps shipping.
- HTML fit: Outstanding. Code editing with CodeMirror 6 or Monaco Editor (full-featured, syntax, search/replace). Preview = literally load the recipe `.html` file into an `<iframe>` or separate webview (no translation layer). Round-tripping HTML is native.
- Action system: Trivial to implement a powerful registry in TypeScript. Command palette (Ctrl/Cmd+K) is common pattern. Macros can be defined in JSON/YAML or keep close to `macros.txt` parser. Easy to expose user-editable "Actions" panel.
- FS: Rust side gives clean, explicit APIs for reading/writing the entire repo dir. Desktop: full access. Mobile: combine with Capacitor-like plugins or OS document providers.
- Other: Hot-reload dev UX good. Theming via CSS (perfect since recipes already use CSS).
- Cons: Mobile experience slightly less "pixel native" than Flutter/Compose (but webviews on mobile are high quality). Requires comfort with Rust for any deep FS/plugin work (or use existing plugins). iOS signing still needed for real devices.
- When it shines: Projects where the data is already web/HTML, desktop is primary or co-equal with mobile, power-user editing features (code view) matter.

**2. Flutter (Dart) + webview / html packages**
- All 5 platforms from one codebase. Mature desktop targets + mobile.
- UI: Declarative widgets; easy to replicate alpha tabs (TabBar + TabBarView or custom), split views, swapping content in a pane.
- HTML: Use `webview_flutter` (mobile) + desktop webview equivalents, or `flutter_html` + `flutter_widget_from_html` for partial renders. Full fidelity preview requires webview. Source editing: community `code_editor` / `code_text_field` or embed a web-based editor (Monaco via webview).
- Action system: Implement registry + macros in Dart. Command palette straightforward. Context menus, shortcuts via `Shortcuts`/`Actions` or packages.
- FS: `file_picker`, `path_provider`, `synchronized` etc. Desktop near-native. Mobile: scoped storage/document picker. "Pick a whole recipes folder" works on desktop/Android reasonably; iOS more limited (may need "the app's documents" + export, or Files app integration).
- Pros: Excellent hot reload, consistent UI, large ecosystem, good performance (Impeller). One team can own all platforms.
- Cons: Dart (new language for Java team, though easy to learn). HTML editing/preview not as seamless as a web stack (translation or webview overhead). Binary sizes larger than Tauri on desktop.
- Good when: You want maximum platform parity with a single UI framework and are okay investing in Dart.

**3. Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP)**
- Share Kotlin business logic + UI (Compose) across Android + Desktop + iOS.
- UI fidelity: Native on Android (Material/Compose), iOS (via Skiko/Compose for iOS), desktop JVM.
- HTML: Embed platform WebView (WKWebView on iOS, WebView on Android/desktop) for preview and/or rich code editor. Or use rich text + custom HTML serializer.
- Action system: Clean Kotlin command objects + registry. Easy key handling.
- FS: Excellent on desktop (java.nio / okio). Mobile scoped but workable.
- Pros: Kotlin is modern/Java-adjacent (low learning curve from current code). True native performance/UI where Compose targets. Strong for teams already doing Android.
- Cons: Compose for iOS/desktop still catching up to Flutter in some widget richness/3rd-party. Web integration requires platform channels for advanced editor features. Slightly more fragmentation than "one framework" solutions.
- Good when: You prefer native-feeling controls and already like Kotlin.

**4. .NET MAUI (C# / XAML or Blazor Hybrid)**
- Targets iOS, Android, macOS, Windows (Linux via community?).
- Blazor Hybrid variant lets you use web tech (HTML/CSS/JS) inside the app → excellent for HTML code + preview.
- FS: Good .NET APIs, with platform specifics.
- Pros: If you or team know C#/.NET, very productive. Strong enterprise support, Visual Studio.
- Cons: Smaller mindshare for consumer/desktop+mobile than Flutter. Linux support weaker. Webview-based Blazor hybrid has some overhead.
- Niche fit here.

**5. Other / Hybrids to Consider**
- **Web SPA (SvelteKit / Next.js / Vite + TS) + shells**: Core app as web. Desktop: Tauri or Electron wrapper. Mobile: Capacitor + native plugins. This is very close to pure Tauri but splits concerns.
- **Qt 6 (C++ or Python)**: Mature everywhere including mobile. QWebEngine for HTML. Heavy for this use case.
- **React Native + desktop** (or Expo): Mobile-strong, desktop secondary.
- **Pure native split** (SwiftUI + Kotlin/Jetpack Compose): Highest fidelity but 2+ codebases. Only if team size allows.

### Recommended Path for Discussion
- **Desktop-first prototype**: Tauri 2 + modern lightweight frontend (Svelte 5 + TS recommended for simplicity and reactivity, or React if preferred). This gives the best HTML code + preview experience and easiest powerful action/macro implementation immediately.
- Validate the file format roundtrip, alpha index, pane-swap "modal" editing, and macro registry.
- Then expand mobile targets using Tauri mobile or evaluate adding a Flutter layer / webview-heavy approach.
- Alternative if mobile parity from day one is mandatory: Flutter, accepting some webview usage for the editor/preview.
- Hybrid long-term: Keep a web-frontend core (for HTML strengths) + Tauri desktop + Capacitor/Flutter mobile shells.

**Language considerations** (from current Java codebase):
- Staying close to JVM: Kotlin Multiplatform or even modern Java + some cross-platform UI (less ideal).
- Jumping to web tech: High leverage because data format is HTML.
- Dart: Fast for UI, different ecosystem.
- Rust (Tauri backend): Only for the privileged parts; most app code in frontend language.

**Packaging/Distribution notes**:
- Tauri: Excellent native installers (`.exe`, `.dmg`, `.deb`/AppImage, etc.). Mobile via standard Android/iOS projects.
- Flutter: `flutter build`, platform tools for stores.
- All options support code signing and app stores.

## 4. High-Level Architecture

### Layers
1. **Repository / Persistence** (critical)
   - `RecipeRepository` abstraction (current skeleton in `persistence/` can be evolved).
   - Concrete impls per platform or unified with good FS primitives.
   - Responsibilities: open repo dir, list recipes, load/save `RecipeFile` (HTML), maintain/update `IndexFile`, import/export single or whole repo, optional fs watching for external edits.
   - Must support **importing existing RecipeJar folders** without data loss.
   - HTML compatibility layer: parse existing files (improve on or replace custom `AbstractXHTMLBasedFile` + `Element` parser), extract structured data (title, notes, ingredients list, procedure, labels, meta), serialize back while preserving as much as possible (CSS links, footers, macros, unknown elements?).

2. **Domain Model**
   - `Recipe` (title, notes, ingredients: `List<Ingredient>`, procedure, labels, meta timestamps).
   - `Ingredient` (qty, unit, name) – keep for consistency and future unit conversion.
   - Index structures (letter sections + categories) or derive on the fly + cache.

3. **Action / Command System**
   - Central registry: `Map<id, Command>` where Command has `execute(context)`, `title`, optional `keybinding`, `enabledWhen`, `category`.
   - Core commands: file (new, save, delete, rename, import, export, toggle-edit, print, exit), edit (cut/copy/paste, find), find variants, tools (converter, prefs), help.
   - Text/Macro commands: loaded from user file (support legacy `macros.txt` format + richer JSON successor).
   - Menu contribution + toolbar + context menu + command palette.
   - Context: current recipe open/closed, editor focused + has selection, platform, etc.
   - User configuration: UI to edit macros/actions, assign keys?

4. **UI / Presentation**
   - Shell: resizable split (index | content). Content area swaps reader vs editor (or uses a state machine to replace child).
   - Index: Replicate AlphaTab feel. Options:
     - TabBar (A..Z + #) + list or rich HTML view per tab.
     - Or virtualized list with sticky alpha headers + quick-jump.
     - Keep hyperlinks or buttons that select recipe.
   - Reader: WebView / equivalent loading the recipe `.html` (or static render).
   - Editor (prototype priority):
     - Primary: Code editor (full recipe HTML or "body" sections). Syntax highlighting, validation?
     - Secondary: Structured form (title, notes textarea, ingredients data grid/table, procedure textarea, labels).
     - Toggle or "Apply" to sync between code and structured (or treat code as source of truth for prototype).
     - Preview button or split (but respect "swap" preference for minimalism).
   - Global: MenuBar (built from registry), shortcuts, status.
   - Dialogs: Search, Prefs, UnitConverter (later), macro editor, about.
   - Theming: load repo CSS where possible for previews.

5. **Services**
   - Macro processor (port `processMacros`, support same `[TITLE]`, `[LASTSAVE]`, etc.).
   - Search engine (index titles + content or simple grep-like).
   - Unit system + future converter.
   - Settings store (cross-platform prefs + per-repo overrides?).

### Data Flow (Save Example)
User edits in code view → structured parse or direct save → `RecipeFile` serializer writes canonical HTML using template + current data → update index.html sections → refresh UI.

For power users editing raw HTML: on save, attempt to re-extract structured fields for consistency where possible; otherwise treat as opaque but still catalog it.

## 5. Phased Roadmap (Prioritize Prototype)

**Phase 0: Setup & Decisions (this plan)**
- Agree on tech stack.
- Confirm exact HTML compatibility invariants (what must round-trip 100%?).
- Decide on macro file format evolution.
- Repository root selection UX (especially mobile constraints).

**Phase 1: Bootstrap + Core Compatibility (MVP foundation)**
- New project scaffold (Tauri or Flutter as chosen).
- FS layer: pick/open repo dir, enumerate `.html` files, basic read.
- HTML parser/serializer that can:
  - Load existing recipes from `Test/Recipes/`.
  - Produce equivalent output (title, notes, ingredients spans, procedure, metas, footers).
  - Regenerate index.html sections from recipes.
- Domain models + mappers (current `RecipeMapper` idea).
- Basic "open existing Test/Recipes" and display index + open one recipe in read view.

**Phase 2: Index + Basic Navigation**
- Alpha-tab (or equivalent) index UI.
- Hyperlink / selection opens recipe in read-only rendered view.
- Reload on external change (nice-to-have).
- Split-pane or equivalent layout with swap capability stub.

**Phase 3: Action Registry + Core Commands**
- Implement registry + contribution to menus + keyboard.
- Port key actions: New, Toggle Edit (swap), Save, Delete, Rename, Import, Export, Exit, Find variants, Prefs stub.
- Context enabling (no recipe open → disable edit/delete etc.).
- Basic menu bar assembly.

**Phase 4: Editor — Code View First**
- HTML source editor component for the recipe (prioritized).
- Load recipe HTML into editor.
- Save writes back (using serializer to keep structure nice, or minimal diff if raw).
- Toggle/swap between read-only rendered and editor (replicate current modal behavior).
- Structured fields as secondary tab or view (or generated from code).
- Macro execution against the editor buffer.
- Title change handling, new recipe from template.

**Phase 5: Macros / User Actions (The Shine Feature)**
- Load `macros.txt` (or new format) and register dynamic text actions.
- UI for editing macros (at minimum text file; ideally nicer editor).
- Popup menu on editor fields.
- Support the existing syntax + room for extension (variables, more commands).

**Phase 6: Polish for Prototype**
- Search dialog.
- Basic preferences (repo path, user info, defaults).
- CSS loading for previews.
- State persistence (last tab, last recipe, window size).
- Error handling + logging.
- Packaging a desktop build that can open the existing `Test/` folder successfully.

**Phase 7: Mobile Hardening + Cross-Platform**
- Platform FS abstractions / document picker.
- Repo export (zip entire folder) as safety net.
- Test on real devices/emulators.
- Adjust UI for touch (larger targets, different navigation if needed).
- Platform-specific menus/shortcuts.

**Later / Post-MVP**
- WYSIWYG option.
- Unit converter (reuse current logic?).
- Image support / attachments inside recipes.
- Better diffing when user edits raw HTML.
- Full index regeneration command.
- Theming / multiple CSS.
- Sync helpers (git? cloud folder pointers?).
- Tests (unit for parser/serializer/macro processor, integration for roundtrips).
- Documentation, migration guide from old RecipeJar.

## 6. Open Questions & Discussion Points

1. **Primary stack decision**: Tauri+web vs Flutter vs KMP+Compose? Desktop priority vs equal mobile?
2. **Editor model**: For prototype, is the editor a full-document HTML editor, or a "body sections" editor (header/notes/ingredients/procedure as separate editable regions)? How smart should round-tripping be?
3. **Macro file format**: Keep `macros.txt` parser for compatibility, or migrate users to JSON with richer features? Provide an in-app macro manager?
4. **Mobile repository UX**: "Open a folder of HTML files" on iOS is painful. Preferred strategy: 
   - Documents / iCloud Drive folder that user can also browse in Files app?
   - App owns its recipes + "Export entire repo" button?
   - Support cloud-synced locations only?
5. **Alpha tabs on mobile**: Tabs work, or switch to alphabet jumper + searchable list for smaller screens?
6. **How much of the old custom XHTML parser to port vs replace** (use a real parser like jsdom/cheerio on web, or html5lib/xml libs in native)?
7. **Do we want a command palette** in addition to menus (highly Arachnophilia-like)?
8. **Settings storage**: Per-repo `settings.json` + global app prefs? Or everything in app prefs like current `ProgramVariables` + Java Preferences?
9. **Team/language preferences**: Your comfort with Rust/TS/Dart/Kotlin?
10. **Scope of "universal" for v1**: Desktop all three + one mobile? Or full five from start?
11. **Regeneration vs preservation**: When saving, should we always re-apply the template structure, or do minimal edits to the user's file?

## 7. Risks & Mitigations

- **HTML fidelity loss** on roundtrip: Write comprehensive tests against real recipe corpus in `Test/Recipes/`. Support a "raw save" mode.
- **Mobile FS restrictions**: Design "export whole repo" and "import recipe(s)" flows early.
- **Action system complexity**: Start minimal (registry + macros) and expand. Make core actions non-macro for reliability.
- **Platform UI divergence**: Define a "simple" design system that works everywhere; use web CSS for previews.
- **Performance on large repos**: Index the catalog; lazy-load recipe content.
- **Maintenance of two worlds** (old Java + new): Keep old as reference; do not attempt in-place migration of the Java sources.

## 8. Next Steps (After Agreement)

1. Pick stack + confirm Phase 1 scope.
2. Set up repo (new branch or separate dir for rewrite?).
3. Extract or recreate a minimal set of test recipes + golden HTML outputs.
4. Prototype the persistence layer + roundtrip.
5. Implement shell + index + one recipe reader.
6. Iterate with feedback.

---

This plan is meant to be living. Edit, comment, or ask questions. The current implementation is excellent reference material for the exact file format behaviors and the spirit of the action/macro system.

Current key reference files:
- Storage: `src/recipejar/filetypes/{IndexFile,RecipeFile}.java`, `AbstractXHTMLBasedFile.java`
- Actions: `src/recipejar/actions/*`, `lib/{MacroTextAction,AbstractTextAction}.java`, `macros.txt`
- UI: `MainFrame.java`, `EditorPanel.java`, `AlphaTab.java`
- Domain/persistence transition: `persistence/`, `domain/Recipe.java`, `recipe/Recipe.java` + `Ingredient.java`
- Data examples: `Test/Recipes/`, `src/recipe.template`, `src/index.template`

Let's discuss the tech choice and any adjustments to scope or priorities.
