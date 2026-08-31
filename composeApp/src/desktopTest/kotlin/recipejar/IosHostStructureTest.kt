package recipejar

import recipejar.HelpLinks
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Structural + shipped-function checks for the iOS host path:
 * Swift [ContentView] → [MainViewController] → [MobilePrototypeApp] (not desktop Main).
 */
class IosHostStructureTest {

    private fun file(vararg rel: String): File {
        val candidates = rel.map { File(it) }
        return candidates.firstOrNull { it.isFile }
            ?: error("missing ${rel.toList()}")
    }

    @Test
    fun iosEntry_hostsMobilePrototype_notDesktopMain() {
        val mvc = file(
            "src/iosMain/kotlin/recipejar/MainViewController.kt",
            "composeApp/src/iosMain/kotlin/recipejar/MainViewController.kt",
        ).readText()
        assertTrue(mvc.contains("MobilePrototypeApp"), "iOS UIKit host must embed MobilePrototypeApp")
        assertFalse(mvc.contains("fun main()"), "must not be the desktop window entry")
        assertTrue(mvc.contains("onExit = null"), "iOS does not finish the process on Exit")

        val content = file(
            "../iosApp/iosApp/ContentView.swift",
            "iosApp/iosApp/ContentView.swift",
        ).readText()
        assertTrue(content.contains("MainViewControllerKt.MainViewController()"))
        assertTrue(content.contains("ComposeView"))
    }

    @Test
    fun iosLibrary_isApplicationSupport_notTestRecipes() {
        val paths = file(
            "src/iosMain/kotlin/recipejar/RecipeLibraryPaths.ios.kt",
            "composeApp/src/iosMain/kotlin/recipejar/RecipeLibraryPaths.ios.kt",
        ).readText()
        assertTrue(paths.contains("NSApplicationSupportDirectory"))
        assertTrue(paths.contains("RecipeJar"))
        assertFalse(paths.contains("Test/Recipes"))
    }

    @Test
    fun mobilePrototype_wiresAppearanceConverterHelp_andOmitsDesktopRepoChrome() {
        val proto = file(
            "src/commonMain/kotlin/recipejar/MobilePrototypeApp.kt",
            "composeApp/src/commonMain/kotlin/recipejar/MobilePrototypeApp.kt",
        ).readText()
        assertTrue(proto.contains("includeDesktopRepoChrome = false"))
        assertTrue(proto.contains("appearanceId"))
        assertTrue(proto.contains("UnitConverterDialog"))
        assertTrue(proto.contains("HelpLinks.WEB_URL") || proto.contains("openExternalUrl"))
        assertTrue(proto.contains("MobileContentModal.Converter"))
        assertTrue(proto.contains("onHelpWeb"))
        assertTrue(proto.contains("platformImportHtml") || proto.contains("platformShareText") || proto.contains("onImport"))
    }

    @Test
    fun mobileMenu_usedByIos_hasConverterHelp_notOpenRepoOrExit() {
        val model = MobileShellLogic.buildMenuModel(
            state = MobileShellState(),
            actions = MobileMenuActions(),
            includeDesktopRepoChrome = false,
        )
        val all = MobileShellLogic.allItemTitles(model)
        assertFalse(MobileMenuTitles.OPEN_REPOSITORY in all)
        assertFalse(MobileMenuTitles.EXIT in all)
        assertTrue(MobileMenuTitles.PREFERENCES in all)
        assertTrue(MobileMenuTitles.UNITS in all)
        assertTrue(MobileMenuTitles.CONVERTER in all)
        assertTrue(MobileMenuTitles.HELP_WEB in all)
        assertTrue(MobileMenuTitles.MANAGE_MACROS in all)
        assertTrue(MobileMenuTitles.FIND_ELLIPSIS in all)
        assertEquals(HelpLinks.WEB_URL, "https://github.com/jcmcconnel/recipejar")
        assertFalse(AppearanceTheme.isPinkOrPurplePrimary(AppearanceTheme.schemeFor(AppearanceTheme.DEFAULT_ID)))
    }

    @Test
    fun iosOpenUrl_andShare_existAsExpectActuals() {
        val open = file(
            "src/iosMain/kotlin/recipejar/OpenExternalUrl.ios.kt",
            "composeApp/src/iosMain/kotlin/recipejar/OpenExternalUrl.ios.kt",
        ).readText()
        assertTrue(open.contains("actual fun openExternalUrl"))
        assertTrue(open.contains("UIApplication"))
        val share = file(
            "src/iosMain/kotlin/recipejar/PlatformShare.ios.kt",
            "composeApp/src/iosMain/kotlin/recipejar/PlatformShare.ios.kt",
        ).readText()
        assertTrue(share.contains("actual fun platformImportHtml") || share.contains("UIDocumentPicker"))
        assertTrue(share.contains("actual fun platformShareText") || share.contains("UIActivityViewController"))
    }
}
