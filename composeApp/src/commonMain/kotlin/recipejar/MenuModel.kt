package recipejar

/**
 * Platform-agnostic menu tree for Material in-window menus (non-macOS hybrid path).
 * Built from ActionRegistry / macros on desktop; rendered by [MaterialMenuBar].
 */
data class AppMenuModel(
    val menus: List<AppMenu>,
)

data class AppMenu(
    val title: String,
    val entries: List<AppMenuEntry>,
)

sealed class AppMenuEntry {
    data class Item(
        val title: String,
        val enabled: Boolean = true,
        val onClick: () -> Unit,
    ) : AppMenuEntry()

    data object Separator : AppMenuEntry()
}
