package recipejar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * iOS actual for [RecipeHtmlWebView].
 * Prototype sets [webViewReady] false so [App] uses the HTML text reader path;
 * this stub satisfies the expect declaration without WKWebView wiring yet.
 */
@Composable
actual fun RecipeHtmlWebView(
    fileUrl: String,
    modifier: Modifier,
) {
    // Intentionally empty — mobile prototype uses text fallback.
}
