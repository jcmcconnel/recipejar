package recipejar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Host for modal-style flows (preferences, search, macros, units, …).
 *
 * - Desktop / wide: [useDialog] true → floating [Dialog]
 * - iOS / iPad / Android / compact: [useDialog] false → full main-content-panel surface
 *
 * Content is the same composable either way so forms are not duplicated.
 */
@Composable
fun ModalHost(
    useDialog: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (useDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 6.dp,
                modifier = modifier
                    .widthIn(min = 360.dp, max = 720.dp)
                    .heightIn(min = 280.dp, max = 640.dp)
                    .padding(16.dp),
            ) {
                content()
            }
        }
    } else {
        // Full main content panel replacement (mobile / compact).
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = modifier.fillMaxSize(),
        ) {
            Box(Modifier.fillMaxSize().padding(12.dp)) {
                content()
            }
        }
    }
}

/**
 * Which presentation mode the shell should use for modal flows.
 * Compact / forced phone layout → content panel; otherwise dialog.
 */
fun preferContentPanelModals(forceCompactLayout: Boolean, windowIsCompact: Boolean): Boolean =
    forceCompactLayout || windowIsCompact
