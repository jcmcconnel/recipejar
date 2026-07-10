package recipejar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Basic preferences: last repository path + optional author name for recipe meta.
 *
 * [onBrowseRepo] should open a directory picker and return the chosen path (or null).
 * [onSave] receives the committed values; host persists and may reopen the repo.
 */
@Composable
fun PreferencesDialog(
    initialRepoPath: String,
    initialAuthorName: String,
    onBrowseRepo: () -> String?,
    onSave: (repoPath: String, authorName: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var repoPath by remember { mutableStateOf(initialRepoPath) }
    var authorName by remember { mutableStateOf(initialAuthorName) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(min = 420.dp, max = 560.dp)
                .padding(16.dp),
        ) {
            Column(Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Preferences", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Repository path is remembered across launches. " +
                        "Author is written into recipe meta on save when set.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = repoPath,
                    onValueChange = {
                        repoPath = it
                        error = null
                    },
                    label = { Text("Recipe repository path") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(
                        onClick = {
                            val picked = onBrowseRepo()
                            if (picked != null) {
                                repoPath = picked
                                error = null
                            }
                        },
                    ) {
                        Text("Browse…")
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text("Author name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            val path = repoPath.trim()
                            if (path.isNotEmpty() && !pathLooksLikeDirectory(path)) {
                                // Soft check only — host validates existence on open.
                                error = null
                            }
                            onSave(path, authorName.trim())
                        },
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/** Minimal path-shape check (common code; full FS check is desktop-side). */
private fun pathLooksLikeDirectory(path: String): Boolean =
    path.isNotBlank() && !path.contains('\u0000')
