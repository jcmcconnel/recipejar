package recipejar

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Android actual for [RecipeHtmlWebView]: system [WebView] loading a local file:// URL
 * or inline HTML so recipes render (not source). Category `recipejar://` links are
 * left to Compose chips; other navigations load normally.
 *
 * Loads only when [fileUrl] / [htmlContent] change — [update] must not re-load on every
 * parent recomposition (status banner, menus) or long recipes lose scroll position.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun RecipeHtmlWebView(
    fileUrl: String,
    modifier: Modifier,
    htmlContent: String?,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        val url = request?.url?.toString().orEmpty()
                        if (url.startsWith("recipejar://")) {
                            return true // Compose category chips handle navigation
                        }
                        return false
                    }
                }
                settings.javaScriptEnabled = false
                settings.allowFileAccess = true
                settings.domStorageEnabled = false
                // Configure only here; first [update] performs the guarded load so we
                // never double-load (factory + update) on the same content.
                tag = null
            }
        },
        update = { webView ->
            loadIntoIfChanged(webView, fileUrl, htmlContent)
        },
        modifier = modifier,
    )
}

/**
 * Signature of the last successful load, stored on [WebView.tag].
 * Only [fileUrl] + [htmlContent] participate — parent recomposition must not reload.
 */
private data class WebViewLoadKey(
    val fileUrl: String,
    val htmlContent: String?,
)

private fun loadIntoIfChanged(webView: WebView, fileUrl: String, htmlContent: String?) {
    val key = WebViewLoadKey(fileUrl = fileUrl, htmlContent = htmlContent)
    val previous = webView.tag as? WebViewLoadKey
    if (previous == key) return
    webView.tag = key
    when {
        !htmlContent.isNullOrBlank() -> {
            val base = fileUrl.takeIf { it.startsWith("file:") } ?: "about:blank"
            webView.loadDataWithBaseURL(base, htmlContent, "text/html", "UTF-8", null)
        }
        fileUrl.isNotBlank() -> webView.loadUrl(fileUrl)
        else -> {
            // Nothing to show — leave blank; keep key so empty updates are no-ops.
        }
    }
}
