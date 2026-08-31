package recipejar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

/**
 * iOS / iPad actual: [WKWebView] loads file:// or inline HTML for a rendered recipe view.
 * Non-stub — when [webViewReady] is true the shell prefers this over monospaced source.
 *
 * Loads only when [fileUrl] / [htmlContent] change — [update] must not re-load on every
 * parent recomposition or long recipes lose scroll position.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun RecipeHtmlWebView(
    fileUrl: String,
    modifier: Modifier,
    htmlContent: String?,
) {
    val config = remember { WKWebViewConfiguration() }
    val loadTracker = remember { WebViewLoadTracker() }

    UIKitView(
        factory = {
            WKWebView(frame = CGRectZero.readValue(), configuration = config).also { webView ->
                webView.allowsBackForwardNavigationGestures = true
                // New native view: force a load of current props (tracker may hold prior keys
                // from a previous WKWebView instance if the interop host recreated the view).
                loadTracker.invalidate()
                loadTracker.loadIfChanged(webView, fileUrl, htmlContent)
            }
        },
        modifier = modifier,
        update = { webView ->
            loadTracker.loadIfChanged(webView, fileUrl, htmlContent)
        },
    )
}

/**
 * Remembers the last (fileUrl, htmlContent) pair applied to the WKWebView so Compose
 * recomposition without content changes does not call loadHTMLString/loadRequest again.
 */
private class WebViewLoadTracker {
    private var lastFileUrl: String? = null
    private var lastHtmlContent: String? = SENTINEL
    private var hasLoaded: Boolean = false

    fun invalidate() {
        lastFileUrl = null
        lastHtmlContent = SENTINEL
        hasLoaded = false
    }

    fun loadIfChanged(webView: WKWebView, fileUrl: String, htmlContent: String?) {
        if (hasLoaded && lastFileUrl == fileUrl && lastHtmlContent == htmlContent) {
            return
        }
        lastFileUrl = fileUrl
        lastHtmlContent = htmlContent
        hasLoaded = true
        loadInto(webView, fileUrl, htmlContent)
    }

    companion object {
        /** Distinct from any real null htmlContent so first load always proceeds. */
        private val SENTINEL: String? = "\u0000__unset__"
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun loadInto(webView: WKWebView, fileUrl: String, htmlContent: String?) {
    when {
        !htmlContent.isNullOrBlank() -> {
            val base = fileUrl.takeIf { it.startsWith("file:") }?.let { NSURL.URLWithString(it) }
            webView.loadHTMLString(htmlContent, baseURL = base)
        }
        fileUrl.isNotBlank() -> {
            val url = NSURL.URLWithString(fileUrl) ?: return
            webView.loadRequest(NSURLRequest.requestWithURL(url))
        }
    }
}
