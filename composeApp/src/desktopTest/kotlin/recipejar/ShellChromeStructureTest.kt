package recipejar

import recipejar.actions.ActionIds
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Structural checks for slim shell chrome:
 * - Open repository lives in Recipe menu builders / action ids
 * - Dense header band (app name, path, count, mode, Phone toggle) is not composed
 */
class ShellChromeStructureTest {

    private fun appKt(): File {
        val candidates = listOf(
            File("src/commonMain/kotlin/recipejar/App.kt"),
            File("composeApp/src/commonMain/kotlin/recipejar/App.kt"),
        )
        return candidates.first { it.isFile }
    }

    private fun mainKt(): File {
        val candidates = listOf(
            File("src/desktopMain/kotlin/recipejar/Main.kt"),
            File("composeApp/src/desktopMain/kotlin/recipejar/Main.kt"),
        )
        return candidates.first { it.isFile }
    }

    private fun mobileShellKt(): File {
        val candidates = listOf(
            File("src/commonMain/kotlin/recipejar/MobileShellLogic.kt"),
            File("composeApp/src/commonMain/kotlin/recipejar/MobileShellLogic.kt"),
        )
        return candidates.first { it.isFile }
    }

    @Test
    fun appShell_hasNoDenseHeaderBand() {
        val src = appKt().readText()
        // Dense header composable and chrome labels removed from shipped App.
        assertFalse(src.contains("private fun AppTopBar"), "AppTopBar dense band must be gone")
        assertFalse(
            Regex("""Text\(\s*"RecipeJar"""").containsMatchIn(src),
            "app name must not appear in shell chrome",
        )
        // Header Open button / Phone toggle row removed (Recipe menu holds Open).
        assertFalse(src.contains("Phone ✓"), "header Phone toggle must be gone")
        assertFalse(src.contains("AppHeaderMetrics"), "header metrics for dense row must be gone")
        // Material menu strip remains the only top chrome when materialMenus is set.
        assertTrue(src.contains("MaterialMenuBar"), "Material menu strip still present")
        assertTrue(
            src.contains("if (materialMenus != null)"),
            "menu strip gated on materialMenus",
        )
    }

    @Test
    fun desktopMain_registersOpenRepoInRecipeMenu() {
        val src = mainKt().readText()
        assertTrue(src.contains("ActionIds.FILE_OPEN_REPO"), "open repo action registered")
        assertTrue(src.contains("\"Open repository\""), "open repo title")
        assertTrue(src.contains("pickDirectory()"), "wired to directory picker")
        // Appears in Material Recipe menu list and native MenuBar Recipe menu.
        assertTrue(
            src.contains("item(ActionIds.FILE_OPEN_REPO)"),
            "Material Recipe menu includes Open repository",
        )
        assertTrue(
            src.contains("registry.require(ActionIds.FILE_OPEN_REPO)"),
            "native Recipe menu includes Open repository",
        )
    }

    @Test
    fun mobileShell_openRepositoryTitleAndActionIdStable() {
        assertTrue(MobileMenuTitles.OPEN_REPOSITORY == "Open repository")
        assertTrue(MobileMenuTitles.OPEN_REPOSITORY in MobileMenuTitles.RECIPE_ITEMS)
        assertTrue(ActionIds.FILE_OPEN_REPO == "file.openRepo")
        val shell = mobileShellKt().readText()
        assertTrue(shell.contains("OPEN_REPOSITORY"), "mobile shell exposes open repo title")
        assertTrue(
            shell.contains("onOpenRepo"),
            "mobile menu actions include onOpenRepo",
        )
    }
}
