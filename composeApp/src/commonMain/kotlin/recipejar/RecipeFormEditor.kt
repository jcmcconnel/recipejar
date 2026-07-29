package recipejar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import recipejar.domain.Recipe
import recipejar.recipe.Ingredient
import recipejar.recipe.UnitsCatalog

/** Which free-text section last received focus (for macro application). */
enum class RecipeEditSection {
    NOTES,
    PROCEDURE,
    OTHER,
}

/**
 * Structured recipe editor: title, categories, notes, ingredients list, procedure.
 * Notes and procedure may contain raw HTML fragments; the full document is not edited here.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeFormEditor(
    recipe: Recipe,
    knownLabels: List<String>,
    unitCatalog: List<String> = emptyList(),
    onRecipeChange: (Recipe) -> Unit,
    onFocusSection: (RecipeEditSection) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    var labelDraft by remember { mutableStateOf("") }

    val suggestions = remember(labelDraft, knownLabels, recipe.labels) {
        val q = labelDraft.trim()
        if (q.isEmpty()) {
            emptyList()
        } else {
            knownLabels
                .asSequence()
                .filter { it !in recipe.labels }
                .filter { it.contains(q, ignoreCase = true) }
                .sortedBy { it.lowercase() }
                .take(8)
                .toList()
        }
    }

    fun emit(block: Recipe.() -> Unit) {
        onRecipeChange(recipe.deepCopy().also(block))
    }

    fun commitLabel(raw: String) {
        val parts = raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return
        emit {
            parts.forEach { addLabel(it) }
        }
        labelDraft = ""
    }

    // Outer Box is the height-bounded viewport; Column is scroll-only (no weight).
    // See [ContentScrollLayout] — procedure fields must remain reachable.
    Box(modifier.fillMaxSize()) {
    Column(
        ContentScrollLayout.contentScrollSurface(Modifier, scroll)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Edit recipe", style = MaterialTheme.typography.titleSmall)
        Text(
            "Notes and procedure accept HTML tags (e.g. <br/>). Title change + Save creates a new file.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider()

        OutlinedTextField(
            value = recipe.title,
            onValueChange = { t -> emit { title = t } },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) onFocusSection(RecipeEditSection.OTHER) },
        )

        // Categories: chips + free entry + soft suggestions (not a closed dropdown).
        Text("Categories", style = MaterialTheme.typography.labelLarge)
        Text(
            "Type a name and press Enter or Add. New categories appear in the browser index after save.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (recipe.labels.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                recipe.labels.forEach { label ->
                    InputChip(
                        selected = false,
                        onClick = { },
                        label = { Text(label) },
                        trailingIcon = {
                            TextButton(
                                onClick = {
                                    emit {
                                        labels.removeAll { it.equals(label, ignoreCase = false) }
                                    }
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp),
                            ) {
                                Text("×")
                            }
                        },
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = labelDraft,
                onValueChange = { labelDraft = it },
                label = { Text("Add category") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { commitLabel(labelDraft) }),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { if (it.isFocused) onFocusSection(RecipeEditSection.OTHER) }
                    .onPreviewKeyEvent { e ->
                        if (e.key == Key.Enter || e.key == Key.NumPadEnter) {
                            commitLabel(labelDraft)
                            true
                        } else {
                            false
                        }
                    },
            )
            Button(
                onClick = { commitLabel(labelDraft) },
                enabled = labelDraft.isNotBlank(),
            ) {
                Text("Add")
            }
        }
        if (suggestions.isNotEmpty()) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    "Suggestions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                suggestions.forEach { s ->
                    TextButton(
                        onClick = { commitLabel(s) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(s, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        OutlinedTextField(
            value = recipe.notes,
            onValueChange = { t -> emit { notes = t } },
            label = { Text("Notes") },
            minLines = 3,
            maxLines = 12,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp)
                .onFocusChanged { if (it.isFocused) onFocusSection(RecipeEditSection.NOTES) },
        )

        Text("Ingredients", style = MaterialTheme.typography.labelLarge)
        recipe.ingredients.forEachIndexed { index, ing ->
            IngredientRow(
                ingredient = ing,
                unitCatalog = unitCatalog,
                onChange = { next ->
                    emit {
                        if (index in ingredients.indices) {
                            ingredients[index] = next
                        }
                    }
                },
                onRemove = {
                    emit {
                        if (index in ingredients.indices) {
                            ingredients.removeAt(index)
                        }
                    }
                },
                onFocus = { onFocusSection(RecipeEditSection.OTHER) },
            )
        }
        OutlinedButton(
            onClick = {
                emit { ingredients.add(Ingredient()) }
            },
            modifier = Modifier.align(Alignment.Start),
        ) {
            Text("Add ingredient")
        }

        OutlinedTextField(
            value = recipe.procedure,
            onValueChange = { t -> emit { procedure = t } },
            label = { Text("Procedure") },
            minLines = 5,
            maxLines = 20,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .onFocusChanged { if (it.isFocused) onFocusSection(RecipeEditSection.PROCEDURE) },
        )

        Spacer(Modifier.height(16.dp))
    }
    } // end viewport Box
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientRow(
    ingredient: Ingredient,
    unitCatalog: List<String>,
    onChange: (Ingredient) -> Unit,
    onRemove: () -> Unit,
    onFocus: () -> Unit,
) {
    var unitMenuExpanded by remember { mutableStateOf(false) }
    val unitLabels = remember(unitCatalog, ingredient.unit) {
        // Catalog is plural names only; rebuild full dropdown list with blank + unknown.
        val asDefs = unitCatalog.map { recipejar.recipe.UnitDef(plural = it) }
        UnitsCatalog.dropdownLabels(asDefs, current = ingredient.unit)
    }

    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            OutlinedTextField(
                value = ingredient.quantity,
                onValueChange = { onChange(ingredient.copy(quantity = it)) },
                label = { Text("Qty") },
                singleLine = true,
                modifier = Modifier
                    .widthIn(min = 56.dp, max = 88.dp)
                    .onFocusChanged { if (it.isFocused) onFocus() },
            )
            ExposedDropdownMenuBox(
                expanded = unitMenuExpanded,
                onExpandedChange = {
                    unitMenuExpanded = it
                    if (it) onFocus()
                },
                modifier = Modifier.widthIn(min = 96.dp, max = 140.dp),
            ) {
                OutlinedTextField(
                    value = ingredient.unit,
                    onValueChange = { onChange(ingredient.copy(unit = it)) },
                    label = { Text("Unit") },
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) onFocus() },
                )
                ExposedDropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false },
                ) {
                    unitLabels.forEach { label ->
                        DropdownMenuItem(
                            text = {
                                Text(if (label.isEmpty()) "(none)" else label)
                            },
                            onClick = {
                                onChange(ingredient.copy(unit = label))
                                unitMenuExpanded = false
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = ingredient.name,
                onValueChange = { onChange(ingredient.copy(name = it)) },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { if (it.isFocused) onFocus() },
            )
            TextButton(onClick = onRemove) {
                Text("Remove")
            }
        }
    }
}

/** Deep copy so list/map mutations do not share structure with the previous state snapshot. */
internal fun Recipe.deepCopy(): Recipe = Recipe(
    title = title,
    notes = notes,
    ingredients = ingredients.map { it.copy() }.toMutableList(),
    procedure = procedure,
    labels = labels.toMutableList(),
    meta = meta.toMutableMap(),
)
