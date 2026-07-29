package recipejar

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS / iPadOS entry: Compose UIKit host for the mobile prototype shell.
 *
 * Exit is discoverable via Recipe → Exit. iOS discourages programmatic quit, so
 * the default [MobilePrototypeApp] path shows a short status message when no
 * platform finish callback is supplied (this host uses that path).
 */
fun MainViewController(): UIViewController =
    ComposeUIViewController {
        MobilePrototypeApp(
            onExit = null,
        )
    }
