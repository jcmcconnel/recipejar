package recipejar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import recipejar.macro.MacroDefinition

/**
 * In-app macro manager: list, add/edit name+template, save, import legacy txt.
 *
 * [initial] is the current macro list; edits are local until [onSave].
 * [onImportTxt] should open a file picker and return parsed macros (or null if cancelled).
 */
@Composable
fun MacroManagerDialog(
    initial: List<MacroDefinition>,
    onSave: (List<MacroDefinition>) -> Unit,
    onDismiss: () -> Unit,
    onImportTxt: (() -> List<MacroDefinition>?)? = null,
) {
    var macros by remember { mutableStateOf(initial.toList()) }
    var selectedIndex by remember { mutableStateOf(if (macros.isEmpty()) -1 else 0) }
    var editName by remember {
        mutableStateOf(macros.getOrNull(selectedIndex)?.name.orEmpty())
    }
    var editText by remember {
        mutableStateOf(macros.getOrNull(selectedIndex)?.text.orEmpty())
    }
    var status by remember { mutableStateOf<String?>(null) }

    fun selectIndex(i: Int) {
        // Flush current fields into list before switching
        if (selectedIndex in macros.indices) {
            macros = macros.toMutableList().also { list ->
                val cur = list[selectedIndex]
                list[selectedIndex] = cur.copy(name = editName.trim().ifEmpty { cur.name }, text = editText)
            }
        }
        selectedIndex = i
        val m = macros.getOrNull(i)
        editName = m?.name.orEmpty()
        editText = m?.text.orEmpty()
    }

    fun flushEditor(): List<MacroDefinition> {
        if (selectedIndex !in macros.indices) return macros
        return macros.toMutableList().also { list ->
            val cur = list[selectedIndex]
            list[selectedIndex] = cur.copy(
                name = editName.trim().ifEmpty { cur.name },
                text = editText,
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(min = 560.dp, max = 720.dp)
                .heightIn(min = 400.dp, max = 560.dp)
                .padding(16.dp),
        ) {
            Column(Modifier.padding(16.dp).fillMaxSize()) {
                Text("Macro Manager", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Templates support [SELECTION], [INPUT:prompt], [COLOR:prompt].",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))

                Row(Modifier.weight(1f).fillMaxWidth()) {
                    // List
                    Column(Modifier.width(200.dp).fillMaxHeight()) {
                        Text("Macros", style = MaterialTheme.typography.titleSmall)
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                            itemsIndexed(macros, key = { i, m -> "$i-${m.name}" }) { index, macro ->
                                val selected = index == selectedIndex
                                Surface(
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectIndex(index) },
                                ) {
                                    Text(
                                        macro.name.ifBlank { "(unnamed)" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = {
                                val flushed = flushEditor().toMutableList()
                                flushed.add(
                                    MacroDefinition(
                                        name = "New Macro",
                                        text = "[SELECTION]",
                                    )
                                )
                                macros = flushed
                                selectedIndex = flushed.lastIndex
                                editName = "New Macro"
                                editText = "[SELECTION]"
                            }) { Text("Add") }
                            TextButton(
                                onClick = {
                                    if (selectedIndex !in macros.indices) return@TextButton
                                    val next = macros.toMutableList().also { it.removeAt(selectedIndex) }
                                    macros = next
                                    selectedIndex = when {
                                        next.isEmpty() -> -1
                                        selectedIndex >= next.size -> next.lastIndex
                                        else -> selectedIndex
                                    }
                                    val m = macros.getOrNull(selectedIndex)
                                    editName = m?.name.orEmpty()
                                    editText = m?.text.orEmpty()
                                },
                                enabled = selectedIndex in macros.indices,
                            ) { Text("Delete") }
                        }
                    }

                    VerticalDivider(Modifier.padding(horizontal = 8.dp))

                    // Editor
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        if (selectedIndex < 0 || macros.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "No macros. Click Add or Import.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Template", style = MaterialTheme.typography.labelMedium)
                            OutlinedTextField(
                                value = editText,
                                onValueChange = { editText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                            )
                        }
                    }
                }

                if (status != null) {
                    Text(
                        status!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onImportTxt != null) {
                        OutlinedButton(onClick = {
                            val imported = onImportTxt()
                            if (imported != null) {
                                macros = imported
                                selectedIndex = if (imported.isEmpty()) -1 else 0
                                val m = macros.getOrNull(selectedIndex)
                                editName = m?.name.orEmpty()
                                editText = m?.text.orEmpty()
                                status = "Imported ${imported.size} macro(s) from macros.txt"
                            }
                        }) { Text("Import .txt") }
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        val finalList = flushEditor().filter { it.name.isNotBlank() }
                        onSave(finalList)
                    }) { Text("Save") }
                }
            }
        }
    }
}
