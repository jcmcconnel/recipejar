package recipejar

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Android actual for [RecipeHtmlWebView]: system [WebView] loading a local file:// URL
 * so relative CSS/images resolve the same way as the desktop KCEF path.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun RecipeHtmlWebView(
    fileUrl: String,
    modifier: Modifier,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = false
                settings.allowFileAccess = true
                if (fileUrl.isNotBlank()) {
                    loadUrl(fileUrl)
                }
            }
        },
        update = { webView ->
            if (fileUrl.isNotBlank()) {
                webView.loadUrl(fileUrl)
            }
        },
        modifier = modifier,
    )
}
