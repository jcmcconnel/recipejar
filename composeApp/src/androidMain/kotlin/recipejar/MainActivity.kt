package recipejar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Android entry point: hosts the shared [App] shell.
 *
 * Phase 1A will wire document pickers / SAF for repository open. This activity
 * proves the Compose Multiplatform Android target compiles and launches the real UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val forceCompact = remember { mutableStateOf(true) }
                    App(
                        selectedDir = null,
                        recipes = emptyList(),
                        selectedFilename = null,
                        selectedFileUrl = null,
                        selectedHtml = null,
                        welcomeHtml = "",
                        welcomeFileUrl = null,
                        webViewReady = false,
                        forceCompactLayout = forceCompact.value,
                        onForceCompactChange = { forceCompact.value = it },
                        onOpenRepo = {
                            // SAF / folder open lands in Phase 1A product work
                        },
                        onSelectRecipe = { },
                    )
                }
            }
        }
    }
}
