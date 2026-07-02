package recipejar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Shell skeleton for the RecipeJar CMP app.
// Desktop entry wires this; future PRs will expand with repo state, alpha tabs, webview etc.

@Composable
fun App(
    selectedDir: String?,
    files: List<String>,
    onOpenRepo: () -> Unit
) {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("RecipeJar (KMP/CMP Desktop)", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onOpenRepo) {
                Text("Open recipe repository")
            }
            Spacer(Modifier.height(16.dp))
            if (selectedDir != null) {
                Text("Repository: $selectedDir")
                Spacer(Modifier.height(8.dp))
                Text("Files (${files.size}):")
                if (files.isEmpty()) {
                    Text("(no matching files or stub)")
                } else {
                    // Basic listing stub; PR2+ will integrate real repo/index
                    files.take(20).forEach { file ->
                        Text("• $file", modifier = Modifier.padding(start = 8.dp))
                    }
                    if (files.size > 20) Text("... and ${files.size - 20} more")
                }
            } else {
                Text("No repository selected. Pick a directory containing recipes (e.g. Test/Recipes).")
            }
        }
    }
}