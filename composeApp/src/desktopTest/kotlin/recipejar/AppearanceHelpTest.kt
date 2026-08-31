package recipejar

import androidx.compose.material3.lightColorScheme
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppearanceHelpTest {

    @Test
    fun defaultScheme_isNotMaterialPinkPurple() {
        val materialDefault = lightColorScheme().primary
        assertTrue(
            AppearanceTheme.isPinkOrPurplePrimary(materialDefault),
            "sanity: Material3 default primary is pink/purple",
        )
        val applied = AppearanceTheme.schemeFor(AppearanceTheme.DEFAULT_ID)
        assertFalse(
            AppearanceTheme.isPinkOrPurplePrimary(applied),
            "default appearance must not be Material pink/purple (primary=${applied.primary})",
        )
        assertTrue(AppearanceTheme.schemeHasReadablePrimary(applied))
        assertEquals(AppearanceId.FOREST, AppearanceTheme.parse(null))
    }

    @Test
    fun nonPinkOptions_canBeSelected() {
        val ids = AppearanceTheme.all().map { it.id }
        assertTrue("forest" in ids && "ocean" in ids && "slate" in ids && "warm" in ids)
        for (id in listOf("forest", "ocean", "slate", "warm")) {
            val scheme = AppearanceTheme.schemeFor(id)
            assertFalse(
                AppearanceTheme.isPinkOrPurplePrimary(scheme),
                "$id should not be pink/purple (primary=${scheme.primary})",
            )
        }
        val rose = AppearanceTheme.schemeFor("rose")
        assertTrue(
            AppearanceTheme.isPinkOrPurplePrimary(rose),
            "rose option keeps Material pink/purple for those who want it",
        )
        assertEquals("ocean", AppearanceTheme.parse("OCEAN").id)
    }

    @Test
    fun helpUrl_isDocumentedGithubRepo() {
        assertTrue(HelpLinks.isDocumentedHelpUrl(HelpLinks.WEB_URL))
        assertEquals("https://github.com/jcmcconnel/recipejar", HelpLinks.WEB_URL)
    }

    @Test
    fun desktopMain_registersHelpWebAndConverterAndAppearance() {
        val main = File("src/desktopMain/kotlin/recipejar/Main.kt").takeIf { it.isFile }
            ?: File("composeApp/src/desktopMain/kotlin/recipejar/Main.kt")
        val src = main.readText()
        assertTrue(src.contains("ActionIds.HELP_WEB"), "Help web action registered")
        assertTrue(src.contains("HelpLinks.WEB_URL") || src.contains("github.com/jcmcconnel/recipejar"))
        assertTrue(src.contains("ActionIds.TOOLS_CONVERTER"), "converter action registered")
        assertTrue(src.contains("UnitConverterDialog"), "converter dialog hosted")
        assertTrue(src.contains("appearanceId"), "appearance applied")
        assertTrue(src.contains("item(ActionIds.HELP_WEB)") || src.contains("registry.require(ActionIds.HELP_WEB)"))
    }

    @Test
    fun preferencesDialog_exposesAppearanceControls() {
        val prefs = File("src/commonMain/kotlin/recipejar/PreferencesDialog.kt").takeIf { it.isFile }
            ?: File("composeApp/src/commonMain/kotlin/recipejar/PreferencesDialog.kt")
        val src = prefs.readText()
        assertTrue(src.contains("Color scheme") || src.contains("Appearance"))
        assertTrue(src.contains("initialAppearanceId"))
        assertTrue(src.contains("appearanceDark"))
        val schemeIdx = src.indexOf("""label = { Text("Color scheme") }""")
        val welcomeFieldIdx = src.indexOf("""label = { Text("Welcome message file") }""")
        assertTrue(schemeIdx >= 0 && welcomeFieldIdx >= 0, "both field labels present")
        assertTrue(
            schemeIdx < welcomeFieldIdx,
            "appearance field must sit above welcome field so it is visible without scrolling",
        )
    }
}
