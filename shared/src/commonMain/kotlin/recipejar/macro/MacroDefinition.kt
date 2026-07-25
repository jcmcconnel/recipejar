package recipejar.macro

import kotlinx.serialization.Serializable

/**
 * User-defined text macro (successor of one macros.txt line).
 *
 * [text] is the expansion template supporting [SELECTION], [INPUT]/[INPUT:prompt],
 * [COLOR]/[COLOR:prompt], and literal segments (ported from MacroTextAction).
 */
@Serializable
data class MacroDefinition(
    val name: String,
    val text: String,
    val mnemonic: String? = null,
    val accelerator: String? = null,
    val mask: String? = null,
)

/**
 * JSON root for macros.json (array-or-object tolerant via encode of this wrapper).
 */
@Serializable
data class MacroFile(
    val macros: List<MacroDefinition> = emptyList(),
)
