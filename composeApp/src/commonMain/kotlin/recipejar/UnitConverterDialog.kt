package recipejar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import recipejar.recipe.UnitConverter
import recipejar.recipe.UnitDef

/**
 * Unit converter: quantity + from/to units from the live catalog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterPanel(
    units: List<UnitDef>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fromOptions = remember(units) { UnitConverter.convertableUnits(units) }
    var quantity by remember { mutableStateOf("1") }
    var from by remember { mutableStateOf(fromOptions.firstOrNull()) }
    var to by remember {
        mutableStateOf(
            from?.let { UnitConverter.conversionTargets(it, units).firstOrNull() },
        )
    }
    var message by remember { mutableStateOf<String?>(null) }
    var fromOpen by remember { mutableStateOf(false) }
    var toOpen by remember { mutableStateOf(false) }

    val toOptions = remember(from, units) {
        from?.let { UnitConverter.conversionTargets(it, units) }.orEmpty()
    }

    fun runConvert() {
        val src = from
        val dst = to
        if (src == null || dst == null) {
            message = "Pick units that have a conversion factor"
            return
        }
        val result = UnitConverter.convert(quantity, src, dst)
        if (result == null) {
            message = "No conversion from ${src.displayName()} to ${dst.displayName()}"
        } else {
            quantity = result
            from = dst
            to = UnitConverter.conversionTargets(dst, units).firstOrNull()
            message = null
        }
    }

    Column(modifier.padding(16.dp).fillMaxWidth()) {
        Text("Unit Converter", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Uses conversion factors from the units catalog (Tools → Units…). " +
                "Fractions and mixed numbers are accepted.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = fromOpen,
            onExpandedChange = { fromOpen = it },
        ) {
            OutlinedTextField(
                value = from?.displayName().orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text("From") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(fromOpen) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = fromOpen, onDismissRequest = { fromOpen = false }) {
                fromOptions.forEach { u ->
                    DropdownMenuItem(
                        text = { Text(u.displayName()) },
                        onClick = {
                            from = u
                            to = UnitConverter.conversionTargets(u, units).firstOrNull()
                            fromOpen = false
                            message = null
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = toOpen,
            onExpandedChange = { toOpen = it },
        ) {
            OutlinedTextField(
                value = to?.displayName().orEmpty().ifBlank { "— Convert to —" },
                onValueChange = {},
                readOnly = true,
                label = { Text("To") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(toOpen) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = toOpen, onDismissRequest = { toOpen = false }) {
                if (toOptions.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No conversions") },
                        onClick = { toOpen = false },
                        enabled = false,
                    )
                } else {
                    toOptions.forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u.displayName()) },
                            onClick = {
                                to = u
                                toOpen = false
                                runConvert()
                            },
                        )
                    }
                }
            }
        }
        if (message != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                message!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Close") }
            Button(onClick = { runConvert() }, enabled = from != null && to != null) {
                Text("Convert")
            }
        }
    }
}

@Composable
fun UnitConverterDialog(
    units: List<UnitDef>,
    onDismiss: () -> Unit,
    useDialog: Boolean = true,
) {
    ModalHost(useDialog = useDialog, onDismiss = onDismiss) {
        UnitConverterPanel(units = units, onDismiss = onDismiss)
    }
}
