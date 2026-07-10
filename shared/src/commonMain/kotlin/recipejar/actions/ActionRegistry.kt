package recipejar.actions

/**
 * Port of ActionRegistry.java to Kotlin for CMP.
 * Map based, string ids, require/find/ids.
 * [clearPrefix] supports dynamic macro re-registration without restart (PR-7).
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
        // Return the view (unmodifiable in effect); avoids copy per original intent but smaller
        return actions.keys
    }

    /**
     * Remove all actions whose id starts with [prefix] (e.g. `"macro."`).
     * Used when reloading macros after manager save/import.
     */
    fun clearPrefix(prefix: String) {
        val toRemove = actions.keys.filter { it.startsWith(prefix) }
        for (id in toRemove) {
            actions.remove(id)
        }
    }

    // For compatibility with original sanitize (used by macros)
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
