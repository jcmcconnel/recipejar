package recipejar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import java.io.File

/**
 * Desktop WebView host (welcome page / optional use). Recipe readonly uses
 * [RecipeReadonlyDocument] so Desktop and Phone stay reliable without KCEF sizing races.
 */
@Composable
actual fun RecipeHtmlWebView(
    fileUrl: String,
    modifier: Modifier,
    htmlContent: String?,
) {
    val previewDir = remember {
        val home = System.getProperty("user.home") ?: "."
        File(home, ".cache/recipejar/previews").also { it.mkdirs() }
    }
    val loadTarget = remember(fileUrl, htmlContent) {
        resolveLoadUrl(fileUrl, htmlContent, previewDir).ifBlank { "about:blank" }
    }
    val state = rememberWebViewState(url = loadTarget)
    val navigator = rememberWebViewNavigator()

    LaunchedEffect(loadTarget) {
        if (loadTarget.isNotBlank() && loadTarget != "about:blank") {
            navigator.loadUrl(loadTarget)
        }
    }

    WebView(
        state = state,
        navigator = navigator,
        modifier = modifier,
    )
}

/**
 * Build a CEF-friendly `file:///` URL. Prefer on-disk [fileUrl] when present.
 */
internal fun resolveLoadUrl(
    fileUrl: String,
    htmlContent: String?,
    previewDir: File,
): String {
    val normalizedDisk = FileUrls.normalizeFileUri(fileUrl)
    val diskFile = FileUrls.toFileOrNull(normalizedDisk)

    if (diskFile != null && diskFile.isFile && htmlContent.isNullOrBlank()) {
        return FileUrls.fromFile(diskFile)
    }

    if (!htmlContent.isNullOrBlank()) {
        val recipeParent = diskFile?.parentFile?.takeIf { it.isDirectory }
        val baseDir = recipeParent ?: previewDir
        val baseHref = when {
            recipeParent != null -> FileUrls.directoryBaseUrl(recipeParent)
            diskFile?.parentFile != null -> FileUrls.directoryBaseUrl(diskFile.parentFile!!)
            else -> FileUrls.directoryBaseUrl(baseDir)
        }
        val withBase = injectBaseHref(htmlContent, baseHref)
        val out = File(baseDir, previewFileName(fileUrl, withBase))
        out.writeText(withBase, Charsets.UTF_8)
        return FileUrls.fromFile(out)
    }

    return normalizedDisk
}

private fun previewFileName(fileUrl: String, html: String): String {
    val fromUrl = FileUrls.toFileOrNull(fileUrl)?.nameWithoutExtension
        ?: FileUrls.toFileOrNull(FileUrls.normalizeFileUri(fileUrl))?.nameWithoutExtension
        ?: "preview"
    val safe = fromUrl.replace(Regex("[^A-Za-z0-9._-]"), "_").take(80)
    val hash = (html.length.toString(16) + "-" + (html.hashCode().toUInt().toString(16)))
    return "rj-preview-$safe-$hash.html"
}

internal fun injectBaseHref(html: String, baseHref: String): String {
    if (baseHref.isBlank()) return html
    val baseTag = """<base href="$baseHref"/>"""
    val replaced = Regex("(?i)<base\\s+[^>]*>").replace(html, baseTag)
    if (replaced != html) return replaced
    val headOpen = Regex("(?i)<head([^>]*)>")
    val m = headOpen.find(html)
    return if (m != null) {
        html.replaceRange(m.range.last + 1, m.range.last + 1, "\n    $baseTag")
    } else {
        "<head>$baseTag</head>\n$html"
    }
}
