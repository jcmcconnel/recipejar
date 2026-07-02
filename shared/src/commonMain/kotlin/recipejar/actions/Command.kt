package recipejar.actions

/**
 * Command ported concept for CMP (no javax.swing.Action).
 * enabled uses lambda so Compose can observe state (e.g. recipe open) without direct mutation.
 * Keep string id for compatibility.
 */
data class Command(
    val id: String,
    val title: String,
    val execute: (ActionContext) -> Unit,
    val enabled: () -> Boolean = { true },
    val mnemonic: Char? = null,
    val shortcut: KeyCombo? = null
)

/**
 * Minimal context for execute (expand later with editor focus etc).
 */
data class ActionContext(
    val recipeId: String? = null
)

/**
 * Simple key combo for shortcut (maps to Compose KeyShortcut in UI layer).
 * OS meta/ctrl decided at registration using isMac.
 */
data class KeyCombo(
    val key: Char,
    val ctrl: Boolean = false,
    val meta: Boolean = false,
    val alt: Boolean = false,
    val shift: Boolean = false
)
