package recipejar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Preferences: repository path, author, welcome file, and appearance.
 *
 * [onBrowseRepo] / [onBrowseWelcome] open pickers and return a path (or null).
 * [onSave] receives committed values and returns an error message to keep the dialog open,
 * or null when save succeeded (dialog closes).
 *
 * [useDialog]: true on Desktop; false embeds in the main content panel on mobile/compact.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesDialog(
    initialRepoPath: String,
    initialAuthorName: String,
    initialWelcomeFilePath: String = "",
    initialAppearanceId: String = AppearanceTheme.DEFAULT_ID,
    initialAppearanceDark: Boolean = false,
    onBrowseRepo: () -> String?,
    onBrowseWelcome: (() -> String?)? = null,
    onSave: (
        repoPath: String,
        authorName: String,
        welcomeFilePath: String,
        appearanceId: String,
        appearanceDark: Boolean,
    ) -> String?,
    onDismiss: () -> Unit,
    useDialog: Boolean = true,
) {
    var repoPath by remember { mutableStateOf(initialRepoPath) }
    var authorName by remember { mutableStateOf(initialAuthorName) }
    var welcomePath by remember { mutableStateOf(initialWelcomeFilePath) }
    var appearanceId by remember { mutableStateOf(AppearanceTheme.parse(initialAppearanceId).id) }
    var appearanceDark by remember { mutableStateOf(initialAppearanceDark) }
    var appearanceOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scroll = rememberScrollState()

    ModalHost(useDialog = useDialog, onDismiss = onDismiss) {
        Column(Modifier.padding(16.dp).fillMaxWidth().fillMaxHeight()) {
            Text("Preferences", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scroll),
            ) {
                Text(
                    "Repository path is remembered across launches (absolute path). " +
                        "Leave blank and Save to forget the last repository. " +
                        "Author is written into recipe meta on save when set. " +
                        "Welcome message file is shown when no recipe is selected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Text("Appearance", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = appearanceOpen,
                    onExpandedChange = { appearanceOpen = it },
                ) {
                    OutlinedTextField(
                        value = AppearanceTheme.parse(appearanceId).label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Color scheme") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(appearanceOpen) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = appearanceOpen,
                        onDismissRequest = { appearanceOpen = false },
                    ) {
                        AppearanceId.entries.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt.label) },
                                onClick = {
                                    appearanceId = opt.id
                                    appearanceOpen = false
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = appearanceDark,
                        onCheckedChange = { appearanceDark = it },
                    )
                    Text("Dark appearance")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = repoPath,
                    onValueChange = {
                        repoPath = it
                        error = null
                    },
                    label = { Text("Recipe repository path") },
                    singleLine = true,
                    isError = error != null,
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
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = welcomePath,
                    onValueChange = {
                        welcomePath = it
                        error = null
                    },
                    label = { Text("Welcome message file") },
                    singleLine = true,
                    supportingText = {
                        Text("HTML file shown when no recipe is selected. Blank = bundled default.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (onBrowseWelcome != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        OutlinedButton(
                            onClick = {
                                val picked = onBrowseWelcome()
                                if (picked != null) {
                                    welcomePath = picked
                                    error = null
                                }
                            },
                        ) {
                            Text("Browse welcome…")
                        }
                    }
                }
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Button(
                    onClick = {
                        val msg = onSave(
                            repoPath.trim(),
                            authorName.trim(),
                            welcomePath.trim(),
                            appearanceId,
                            appearanceDark,
                        )
                        if (msg != null) {
                            error = msg
                        } else {
                            onDismiss()
                        }
                    },
                ) {
                    Text("Save")
                }
            }
        }
    }
}

/**
 * Pure prefs validation helpers used by hosts and unit tests.
 */
object PreferencesSaveLogic {
    /**
     * Resolve welcome file path: blank → null (use bundled default);
     * non-blank must be an existing readable file or return error message.
     */
    fun validateWelcomeFilePath(path: String, fileExists: (String) -> Boolean): String? {
        if (path.isBlank()) return null
        return if (fileExists(path)) null else "Welcome file not found: $path"
    }
}
