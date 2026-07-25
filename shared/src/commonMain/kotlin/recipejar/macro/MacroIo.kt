package recipejar.macro

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Load/store helpers for macros.json and legacy macros.txt (pure string I/O).
 */
object MacroIo {

    const val JSON_FILENAME = "macros.json"
    const val TXT_FILENAME = "macros.txt"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Built-in samples when a repository has neither macros.json nor macros.txt. */
    val DEFAULT_MACROS: List<MacroDefinition> = listOf(
        MacroDefinition("Bold", "<strong>[SELECTION]</strong>", mnemonic = "B", accelerator = "B", mask = "DEFAULT"),
        MacroDefinition("Italics", "<em>[SELECTION]</em>", mnemonic = "I", accelerator = "I", mask = "DEFAULT"),
        MacroDefinition(
            "Underline",
            """<span style="text-decoration: underline;">[SELECTION]</span>""",
            mnemonic = "U",
            accelerator = "U",
            mask = "DEFAULT",
        ),
        MacroDefinition(
            "Color",
            """<span style="color: [COLOR:Select Text Color];">[SELECTION]</span>""",
            mnemonic = "C",
            accelerator = "C",
            mask = "ALT-SHIFT",
        ),
        MacroDefinition("Paragraph", "<p>[SELECTION]</p>", mnemonic = "P", accelerator = "P", mask = "ALT-DEFAULT"),
        MacroDefinition(
            "Link",
            """<a href="[INPUT:Address]">[SELECTION]</a>""",
            mnemonic = "K",
            accelerator = "K",
            mask = "DEFAULT",
        ),
    )

    /**
     * Parse legacy macros.txt content.
     *
     * Format per line (comma-delimited, `;` comments):
     * `NAME, MNEMONIC, ACCELERATOR, MASK, TEXT…`
     * TEXT may contain commas; remaining fields after the 4th are rejoined.
     */
    fun parseMacrosTxt(content: String): List<MacroDefinition> {
        val result = mutableListOf<MacroDefinition>()
        for (rawLine in content.lineSequence()) {
            val line = rawLine.trimEnd('\r')
            if (line.isBlank()) continue
            val trimmed = line.trimStart()
            if (trimmed.startsWith(";")) continue
            val parts = line.split(',')
            if (parts.size < 5) continue
            val name = parts[0].trim()
            if (name.isEmpty()) continue
            val mnemonic = parts[1].trim().ifEmpty { null }
            val accelerator = parts[2].trim().ifEmpty { null }
            val mask = parts[3].trim().ifEmpty { null }
            // Rejoin TEXT so commas inside HTML templates are preserved.
            val text = parts.subList(4, parts.size).joinToString(",").trim()
            result.add(
                MacroDefinition(
                    name = name,
                    text = text,
                    mnemonic = mnemonic,
                    accelerator = accelerator,
                    mask = mask,
                )
            )
        }
        return result
    }

    fun toJson(macros: List<MacroDefinition>): String =
        json.encodeToString(MacroFile(macros = macros))

    /**
     * Accept either `{"macros":[...]}` or a bare JSON array of definitions.
     */
    fun fromJson(content: String): List<MacroDefinition> {
        val element = json.parseToJsonElement(content.trim())
        return when (element) {
            is JsonObject -> {
                if (element.containsKey("macros")) {
                    json.decodeFromJsonElement<MacroFile>(element).macros
                } else {
                    // single object? unlikely — treat as empty
                    emptyList()
                }
            }
            is JsonArray -> json.decodeFromJsonElement<List<MacroDefinition>>(element)
            else -> emptyList()
        }
    }
}
