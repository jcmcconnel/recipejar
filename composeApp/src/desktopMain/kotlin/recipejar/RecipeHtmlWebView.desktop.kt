package recipejar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState

/**
 * Desktop WebView reader: load recipe via file:// so relative CSS/images resolve.
 * Requires KCEF to have completed [dev.datlag.kcef.KCEF.init] (see Main.kt).
 */
@Composable
actual fun RecipeHtmlWebView(
    fileUrl: String,
    modifier: Modifier,
) {
    val state = rememberWebViewState(url = fileUrl)
    val navigator = rememberWebViewNavigator()

    LaunchedEffect(fileUrl) {
        if (fileUrl.isNotBlank()) {
            navigator.loadUrl(fileUrl)
        }
    }

    WebView(
        state = state,
        navigator = navigator,
        modifier = modifier,
    )
}
