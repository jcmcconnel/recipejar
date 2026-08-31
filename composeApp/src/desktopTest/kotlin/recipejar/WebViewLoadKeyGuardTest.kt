package recipejar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Documents and unit-tests the load-guard contract used by Android/iOS
 * [RecipeHtmlWebView] actuals: reload only when (fileUrl, htmlContent) changes.
 *
 * Mirrors the shipped [WebViewLoadKey]-equivalent logic so a regression that
 * reloads on every recomposition is caught without an emulator.
 */
class WebViewLoadKeyGuardTest {

    private data class LoadKey(val fileUrl: String, val htmlContent: String?)

    /** Pure model of the Android tag / iOS tracker guard. */
    private class LoadGuard {
        private var last: LoadKey? = null
        var loadCount: Int = 0
            private set

        fun loadIfChanged(fileUrl: String, htmlContent: String?): Boolean {
            val key = LoadKey(fileUrl, htmlContent)
            if (last == key) return false
            last = key
            loadCount++
            return true
        }

        fun invalidate() {
            last = null
        }
    }

    @Test
    fun sameProps_doNotReload_onRepeatedUpdate() {
        val guard = LoadGuard()
        val url = "file:///recipes/Pancakes.html"
        val html = "<html><body>program-footer</body></html>"
        assertTrue(guard.loadIfChanged(url, html))
        assertFalse(guard.loadIfChanged(url, html))
        assertFalse(guard.loadIfChanged(url, html))
        assertEquals(1, guard.loadCount)
    }

    @Test
    fun contentChange_reloads() {
        val guard = LoadGuard()
        assertTrue(guard.loadIfChanged("file:///a.html", "<p>a</p>"))
        assertTrue(guard.loadIfChanged("file:///a.html", "<p>b</p>"))
        assertTrue(guard.loadIfChanged("file:///b.html", "<p>b</p>"))
        assertEquals(3, guard.loadCount)
    }

    @Test
    fun invalidate_allowsReloadOfSameProps_forNewNativeView() {
        val guard = LoadGuard()
        val url = "file:///x.html"
        val html = "<html/>"
        assertTrue(guard.loadIfChanged(url, html))
        guard.invalidate()
        assertTrue(guard.loadIfChanged(url, html))
        assertEquals(2, guard.loadCount)
    }

    @Test
    fun androidAndIosSources_documentGuardedLoad() {
        // Structural: shipped actuals must call guarded load paths (not bare load in update).
        val androidSrc = java.io.File("src/androidMain/kotlin/recipejar/RecipeHtmlWebView.android.kt")
            .takeIf { it.isFile }
            ?: java.io.File("composeApp/src/androidMain/kotlin/recipejar/RecipeHtmlWebView.android.kt")
        val iosSrc = java.io.File("src/iosMain/kotlin/recipejar/RecipeHtmlWebView.ios.kt")
            .takeIf { it.isFile }
            ?: java.io.File("composeApp/src/iosMain/kotlin/recipejar/RecipeHtmlWebView.ios.kt")
        assertTrue(androidSrc.isFile, "android actual present at ${androidSrc.absolutePath}")
        assertTrue(iosSrc.isFile, "ios actual present at ${iosSrc.absolutePath}")
        val a = androidSrc.readText()
        val i = iosSrc.readText()
        assertTrue(a.contains("loadIntoIfChanged"), a.take(200))
        assertTrue(a.contains("WebViewLoadKey") || a.contains("previous == key"), "android guard")
        assertTrue(i.contains("loadIfChanged"), i.take(200))
        assertTrue(i.contains("WebViewLoadTracker") || i.contains("hasLoaded"), "ios tracker")
        // Must not call loadDataWithBaseURL / loadHTMLString directly from update without guard.
        assertFalse(
            Regex("""update\s*=\s*\{[^}]*loadDataWithBaseURL""").containsMatchIn(a.replace("\n", " ")),
            "android update must not bare-load",
        )
    }
}
