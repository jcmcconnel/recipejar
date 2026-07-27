package recipejar

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS / iPadOS entry: Compose UIKit host for the mobile prototype shell.
 */
fun MainViewController(): UIViewController =
    ComposeUIViewController {
        MobilePrototypeApp()
    }
