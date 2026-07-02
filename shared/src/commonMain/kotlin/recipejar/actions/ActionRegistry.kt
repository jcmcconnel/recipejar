package recipejar.actions

/**
 * Port of ActionRegistry.java to Kotlin for CMP.
 * Map based, string ids, require/find/ids.
 * No menu registry or clearPrefix in PR5 (no macros/dynamic yet).
 * Follows original semantics as closely as possible.
 */
class ActionRegistry {

    private val actions = mutableMapOf<String, Command>()

    fun register(id: String, command: Command) {
        if (actions.containsKey(id)) {
            throw IllegalStateException("Duplicate action id: $id")
        }
        actions[id] = command
    }

    fun require(id: String): Command {
        return actions[id] ?: throw IllegalStateException("Missing action: $id")
    }

    fun find(id: String): Command? {
        return actions[id]
    }

    fun ids(): Set<String> {
        return actions.keys.toSet()
    }

    // For compatibility with original sanitize (used by macros later)
    fun sanitizeId(name: String): String {
        val sb = StringBuilder()
        for (c in name) {
            if (c.isLetterOrDigit()) {
                sb.append(c)
            }
        }
        return sb.toString()
    }
}
