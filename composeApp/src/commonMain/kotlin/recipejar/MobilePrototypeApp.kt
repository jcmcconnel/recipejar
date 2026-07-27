package recipejar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import recipejar.sample.SampleRecipeJar

/**
 * Mobile prototype root: loads the bundled sample jar via [SampleRecipeJar] /
 * [recipejar.html.RecipeSerializer] and hosts the shared [App] shell.
 *
 * WebView is off for the first prototype pass; the reader shows parsed HTML text
 * (same fallback path as desktop when KCEF is not ready).
 */
@Composable
fun MobilePrototypeApp() {
    val loaded = remember { SampleRecipeJar.loadRecipes() }
    val items = remember(loaded) {
        loaded.map { (filename, recipe) ->
            RecipeListItem(filename = filename, title = recipe.title)
        }
    }
    var selectedFilename by remember { mutableStateOf<String?>(null) }
    val selectedHtml = selectedFilename?.let { SampleRecipeJar.htmlFor(it) }
    var forceCompact by remember { mutableStateOf(true) }

    App(
        selectedDir = "sample://family-jar",
        recipes = items,
        selectedFilename = selectedFilename,
        selectedFileUrl = null,
        selectedHtml = selectedHtml,
        welcomeHtml = WELCOME_PROTOTYPE_HTML,
        welcomeFileUrl = null,
        webViewReady = false,
        forceCompactLayout = forceCompact,
        onForceCompactChange = { forceCompact = it },
        onOpenRepo = {
            // Prototype uses the bundled sample jar only; SAF/iCloud open is Phase 1A.
        },
        onSelectRecipe = { filename ->
            selectedFilename = filename
        },
        onClearSelection = {
            selectedFilename = null
        },
    )
}

private val WELCOME_PROTOTYPE_HTML =
    """
    <html><body>
    <h1>RecipeJar</h1>
    <p>Mobile prototype — sample family recipes loaded from the real HTML serializer.</p>
    <p>Pick a letter and open a recipe to read ingredients and procedure.</p>
    </body></html>
    """.trimIndent()
