package recipejar

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS / iPadOS entry: Compose UIKit host for the mobile prototype shell.
 *
 * Recipe → Exit is omitted on this host ([MobilePrototypeApp] uses the mobile
 * menu set). [onExit] is null so a stray Exit callback would only set status.
 */
fun MainViewController(): UIViewController =
    ComposeUIViewController {
        MobilePrototypeApp(
            onExit = null,
        )
    }
