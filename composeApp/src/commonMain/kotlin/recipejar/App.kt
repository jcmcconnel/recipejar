package recipejar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Shell skeleton for the RecipeJar CMP app.
// Updated for PR5: shell state for recipe open + edit/swap, list selection to drive context enabling.
// Menus/keyboard wired via registry in desktop Main.

@Composable
fun App(
    selectedDir: String?,
    files: List<String>,
    currentRecipe: String?,
    isRecipeOpen: Boolean,
    isEditing: Boolean,
    onOpenRepo: () -> Unit,
    onSelectRecipe: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
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
                if (currentRecipe != null) {
                    Text(
                        "Current: $currentRecipe ${if (isEditing) "[editing]" else "[read]"} (open=$isRecipeOpen)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text("Files (${files.size}) - click to open recipe (enables context actions):")
                if (files.isEmpty()) {
                    Text("(no matching files or stub)")
                } else {
                    // PR3: listed via repo.listRecipes()
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .padding(start = 8.dp)
                    ) {
                        files.take(20).forEach { file ->
                            Text(
                                "• $file",
                                modifier = Modifier.clickable { onSelectRecipe(file) }
                            )
                        }
                        if (files.size > 20) Text("... and ${files.size - 20} more")
                    }
                }
            } else {
                Text("No repository selected. Pick a directory containing recipes (e.g. Test/Recipes).")
            }
        }
    }
}
