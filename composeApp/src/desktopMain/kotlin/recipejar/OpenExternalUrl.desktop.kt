package recipejar

import java.awt.Desktop
import java.net.URI

actual fun openExternalUrl(url: String): Boolean {
    return try {
        val uri = URI(url)
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(uri)
            true
        } else {
            val cmd = if (System.getProperty("os.name").orEmpty().contains("Mac", ignoreCase = true)) {
                arrayOf("open", url)
            } else if (System.getProperty("os.name").orEmpty().contains("Win", ignoreCase = true)) {
                arrayOf("cmd", "/c", "start", url)
            } else {
                arrayOf("xdg-open", url)
            }
            ProcessBuilder(*cmd).start()
            true
        }
    } catch (e: Exception) {
        Debug.error("Failed to open $url", e)
        false
    }
}
