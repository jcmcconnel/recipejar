package recipejar

import android.content.Intent
import android.net.Uri

actual fun openExternalUrl(url: String): Boolean {
    val ctx = androidAppContext() ?: return false
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }
}
