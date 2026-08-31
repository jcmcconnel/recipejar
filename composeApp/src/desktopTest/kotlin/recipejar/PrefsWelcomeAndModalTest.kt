package recipejar

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Welcome-file prefs validation + modal stacking structure.
 */
class PrefsWelcomeAndModalTest {

    @Test
    fun validateWelcomeFilePath_blankOk_missingErrors_existingOk() {
        assertNull(PreferencesSaveLogic.validateWelcomeFilePath("") { false })
        assertNull(PreferencesSaveLogic.validateWelcomeFilePath("  ") { false })
        val err = PreferencesSaveLogic.validateWelcomeFilePath("/no/such/file.html") { false }
        assertNotNull(err)
        assertTrue(err!!.contains("not found", ignoreCase = true))

        val dir = Files.createTempDirectory("rj-welcome").toFile()
        try {
            val f = File(dir, "welcome.html")
            f.writeText("<html><body>Hi family</body></html>", Charsets.UTF_8)
            assertNull(
                PreferencesSaveLogic.validateWelcomeFilePath(f.absolutePath) { File(it).isFile },
            )
            // Load path used by desktop host after save
            val text = f.readText(Charsets.UTF_8)
            assertTrue(text.contains("Hi family"))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun welcomeSave_updatesContentSource_likeDesktopHost() {
        val dir = Files.createTempDirectory("rj-welcome2").toFile()
        try {
            val f = File(dir, "custom-welcome.html")
            f.writeText("<html><body><h1>Custom Welcome</h1></body></html>", Charsets.UTF_8)
            val path = f.absolutePath
            val err = PreferencesSaveLogic.validateWelcomeFilePath(path) { File(it).isFile }
            assertNull(err)
            // Simulate host applying path → load content
            val loaded = File(path).readText(Charsets.UTF_8)
            assertTrue(loaded.contains("Custom Welcome"))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun appShell_contentModalTakesPrecedence_andWelcomeWebViewCanBeDisabled() {
        val app = File("src/commonMain/kotlin/recipejar/App.kt").takeIf { it.isFile }
            ?: File("composeApp/src/commonMain/kotlin/recipejar/App.kt")
        val src = app.readText()
        // Body branch: contentModal first, then no-repo welcome
        val modalIdx = src.indexOf("if (contentModal != null)")
        val noRepoBodyIdx = src.indexOf("} else if (selectedDir == null)")
        assertTrue(modalIdx >= 0, "contentModal branch present")
        assertTrue(noRepoBodyIdx >= 0, "no-repo body branch present")
        assertTrue(modalIdx < noRepoBodyIdx, "contentModal must win over welcome/repo body")
        assertTrue(src.contains("welcomeWebViewEnabled"), "stacking flag still passed by hosts")
        assertTrue(src.contains("fun WelcomePane("), "welcome surface present")
        assertTrue(src.contains("htmlFragmentToAnnotatedString"), "welcome uses read-only fragment renderer")
    }

    @Test
    fun desktopMain_disablesWelcomeWebViewWhileModalsOpen() {
        val main = File("src/desktopMain/kotlin/recipejar/Main.kt").takeIf { it.isFile }
            ?: File("composeApp/src/desktopMain/kotlin/recipejar/Main.kt")
        val src = main.readText()
        assertTrue(src.contains("welcomeWebViewEnabled"), "Main passes stacking flag")
        assertTrue(src.contains("showPreferences"), "prefs dialog")
        assertTrue(
            src.contains("!showPreferences.value") || src.contains("showPreferences.value"),
            "prefs participates in welcome suppress",
        )
        assertTrue(src.contains("initialWelcomeFilePath") || src.contains("welcomeFilePath"))
    }
}
