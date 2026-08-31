package recipejar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import recipejar.recipe.UnitDef
import recipejar.recipe.UnitsCatalog

/**
 * Maintain the ingredient units catalog: list, add/edit plural+singular, save.
 * Host persists via [onSave] (desktop units file / prefs path).
 */
@Composable
fun UnitsManagerPanel(
    initial: List<UnitDef>,
    onSave: (List<UnitDef>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var units by remember { mutableStateOf(initial.sortedBy { it.plural.lowercase() }) }
    var selectedIndex by remember { mutableStateOf(if (units.isEmpty()) -1 else 0) }
    var plural by remember {
        mutableStateOf(units.getOrNull(selectedIndex)?.plural.orEmpty())
    }
    var singular by remember {
        mutableStateOf(units.getOrNull(selectedIndex)?.singular.orEmpty())
    }
    var status by remember { mutableStateOf<String?>(null) }

    fun selectIndex(i: Int) {
        if (selectedIndex in units.indices) {
            units = UnitsCatalog.upsert(
                units.filterIndexed { idx, _ -> idx != selectedIndex },
                UnitDef(
                    plural = plural.trim().ifEmpty { units[selectedIndex].plural },
                    singular = singular.trim(),
                    conversions = units[selectedIndex].conversions,
                ),
            ).let { list ->
                // re-find selected by plural after sort
                list
            }
        }
        // Simpler: flush current into list by index before switch
        if (selectedIndex in units.indices) {
            val cur = units[selectedIndex]
            val next = units.toMutableList()
            next[selectedIndex] = cur.copy(
                plural = plural.trim().ifEmpty { cur.plural },
                singular = singular.trim(),
            )
            units = next.sortedBy { it.plural.lowercase() }
            // adjust selected index after sort
            val targetPlural = next[selectedIndex].plural
            selectedIndex = units.indexOfFirst { it.plural == targetPlural }
        }
        selectedIndex = i.coerceIn(-1, units.lastIndex)
        val u = units.getOrNull(selectedIndex)
        plural = u?.plural.orEmpty()
        singular = u?.singular.orEmpty()
    }

    fun flushCurrent(): List<UnitDef> {
        if (selectedIndex !in units.indices) return units
        val cur = units[selectedIndex]
        val updated = units.toMutableList()
        updated[selectedIndex] = cur.copy(
            plural = plural.trim().ifEmpty { cur.plural },
            singular = singular.trim(),
        )
        return updated
            .filter { it.plural.isNotBlank() }
            .sortedBy { it.plural.lowercase() }
    }

    Column(modifier.fillMaxSize().padding(8.dp)) {
        Text("Units", style = MaterialTheme.typography.titleLarge)
        Text(
            "Units appear in the ingredient unit picker. " +
                "Saved as plain-text units.txt so the catalog stays portable.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.weight(1f).fillMaxWidth()) {
            Column(Modifier.width(200.dp).fillMaxHeight()) {
                Text("Catalog", style = MaterialTheme.typography.titleSmall)
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                    itemsIndexed(units, key = { i, u -> "$i-${u.plural}" }) { index, unit ->
                        val selected = index == selectedIndex
                        Surface(
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // flush then select
                                    if (selectedIndex in units.indices) {
                                        val cur = units[selectedIndex]
                                        val next = units.toMutableList()
                                        next[selectedIndex] = cur.copy(
                                            plural = plural.trim().ifEmpty { cur.plural },
                                            singular = singular.trim(),
                                        )
                                        units = next.sortedBy { it.plural.lowercase() }
                                        val target = next[selectedIndex].plural
                                        val newIdx = units.indexOfFirst {
                                            it.plural.equals(target, ignoreCase = true)
                                        }
                                        selectedIndex = newIdx
                                    }
                                    selectedIndex = units.indexOfFirst {
                                        it.plural.equals(unit.plural, ignoreCase = true)
                                    }.takeIf { it >= 0 } ?: index
                                    val u = units.getOrNull(selectedIndex)
                                    plural = u?.plural.orEmpty()
                                    singular = u?.singular.orEmpty()
                                },
                        ) {
                            Text(
                                unit.displayName().ifBlank { "(unnamed)" },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        val base = flushCurrent().toMutableList()
                        val nu = UnitDef(plural = "new unit", singular = "")
                        base.add(nu)
                        units = base.sortedBy { it.plural.lowercase() }
                        selectedIndex = units.indexOfFirst { it.plural == "new unit" }
                        plural = "new unit"
                        singular = ""
                    }) { Text("Add") }
                    TextButton(
                        onClick = {
                            if (selectedIndex !in units.indices) return@TextButton
                            val target = units[selectedIndex].plural
                            units = UnitsCatalog.remove(units, target)
                            selectedIndex = when {
                                units.isEmpty() -> -1
                                selectedIndex >= units.size -> units.lastIndex
                                else -> selectedIndex
                            }
                            val u = units.getOrNull(selectedIndex)
                            plural = u?.plural.orEmpty()
                            singular = u?.singular.orEmpty()
                        },
                        enabled = selectedIndex in units.indices,
                    ) { Text("Delete") }
                }
            }
            VerticalDivider(Modifier.padding(horizontal = 8.dp))
            Column(Modifier.weight(1f).fillMaxHeight()) {
                if (selectedIndex < 0 || units.isEmpty()) {
                    Text(
                        "No units. Click Add to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    OutlinedTextField(
                        value = plural,
                        onValueChange = { plural = it },
                        label = { Text("Plural (display)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = singular,
                        onValueChange = { singular = it },
                        label = { Text("Singular (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        if (status != null) {
            Text(
                status!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Button(onClick = {
                val finalList = flushCurrent()
                if (finalList.any { it.plural.isBlank() }) {
                    status = "Plural name is required"
                    return@Button
                }
                onSave(finalList)
            }) { Text("Save") }
        }
    }
}

/**
 * Dialog or content-panel host for [UnitsManagerPanel].
 */
@Composable
fun UnitsManagerDialog(
    initial: List<UnitDef>,
    onSave: (List<UnitDef>) -> Unit,
    onDismiss: () -> Unit,
    useDialog: Boolean = true,
) {
    ModalHost(useDialog = useDialog, onDismiss = onDismiss) {
        UnitsManagerPanel(
            initial = initial,
            onSave = { list ->
                onSave(list)
                onDismiss()
            },
            onDismiss = onDismiss,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
