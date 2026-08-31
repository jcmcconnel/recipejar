package recipejar

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openExternalUrl(url: String): Boolean {
    val nsUrl = NSURL.URLWithString(url) ?: return false
    val app = UIApplication.sharedApplication
    return if (app.canOpenURL(nsUrl)) {
        app.openURL(nsUrl)
        true
    } else {
        false
    }
}
