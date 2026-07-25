package recipejar

import recipejar.actions.KeyCombo

/**
 * Desktop OS helpers (port of Kernel.isOS / isAllowableAccelerator).
 * Used for primary modifier (Cmd vs Ctrl), reserved macOS accelerators, and startup props.
 */
object Platform {
    const val APP_NAME = "RecipeJar"
    const val APP_VERSION = "1.0.0"
    const val APP_ABOUT =
        "RecipeJar Recipe Organizer\n" +
            "Local offline recipe manager (Compose Desktop rewrite).\n" +
            "Version $APP_VERSION"

    private val osName: String by lazy {
        System.getProperty("os.name").orEmpty().lowercase()
    }

    /** Substring match on `os.name` (same idea as Java Kernel.isOS). */
    fun isOS(token: String): Boolean =
        osName.contains(token.lowercase())

    val isMac: Boolean get() = isOS("mac")
    val isWindows: Boolean get() = isOS("windows")
    val isLinux: Boolean get() = isOS("linux")

    /**
     * Primary accelerator: Meta (⌘) on macOS, Ctrl on Windows/Linux.
     * Does not claim reserved macOS system combos (see [isAllowableAccelerator]).
     */
    fun primaryShortcut(
        key: Char,
        shift: Boolean = false,
        alt: Boolean = false,
    ): KeyCombo =
        KeyCombo(
            key = key,
            ctrl = !isMac,
            meta = isMac,
            shift = shift,
            alt = alt,
        )

    /**
     * Reserved macOS system shortcuts must not be claimed by the app:
     * Cmd+H hide, Cmd+Q quit, Cmd+, preferences, Cmd+Option+H hide others.
     * Port of [recipejar.Kernel.isAllowableAccelerator].
     */
    fun isAllowableAccelerator(combo: KeyCombo): Boolean {
        if (!isMac || !combo.meta) return true
        val k = combo.key.uppercaseChar()
        if (!combo.alt && !combo.shift && !combo.ctrl) {
            if (k == 'H' || k == 'Q' || k == ',') return false
        }
        if (combo.alt && !combo.shift && !combo.ctrl && k == 'H') return false
        return true
    }

    /** Safe register: returns [combo] only when allowed on this OS; otherwise null. */
    fun allowedShortcut(combo: KeyCombo?): KeyCombo? {
        if (combo == null) return null
        return if (isAllowableAccelerator(combo)) combo else null
    }

    /**
     * AWT / app properties before Compose/Swing UI starts.
     * macOS: app name in menu bar and screen menu bar preference.
     */
    fun applyStartupProperties() {
        if (isMac) {
            System.setProperty("apple.awt.application.name", APP_NAME)
            System.setProperty("apple.laf.useScreenMenuBar", "true")
        }
    }
}
