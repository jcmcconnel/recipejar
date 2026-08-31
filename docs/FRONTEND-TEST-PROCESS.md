# RecipeJar frontend test process

Reusable loop for this codebase (Compose desktop host + `shared/` APIs).

1. **Inventory against the reference, not a second oracle.** Compare menus/actions, prefs, footers, macros, units, and Help to `src/recipejar/` and `src/config.ini`. Do not treat `src/` as the build target and do not mutate `Test/Recipes`.

2. **Drive shipped functions on temp dirs.** Repository save/load/import/export, search, serializer footers, `MacroProcessor`, `htmlFragmentToAnnotatedString`, `UnitsCatalog`/`UnitConverter`, appearance scheme lookup, `HelpLinks.WEB_URL`. A passing test must enter the real type the desktop host uses.

3. **Fix the gap the test failed on.** Keep conversion math, footer choice, and theme resolution window-free. Persist units/macros/theme the same way the host already loads them.

4. **Retest the suite you touched**, then `./gradlew :shared:desktopTest :composeApp:desktopTest` twice (use `--rerun-tasks` so the second run is not only UP-TO-DATE).

5. **Launch or record an honest fallback.** `./recipejar` twice. If the process stays up (KCEF init, repo open), that counts. If `screencapture` cannot read a window in this session, say so — do not invent a screenshot. A blank captured window is an app defect.

6. **Write evidence.** Short diary (what was tested, what broke, what changed) plus this brief. Copy Gradle logs and JUnit XML/HTML reports to the scratch dir named by the goal.
