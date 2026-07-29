package recipejar

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier

/**
 * Layout contract for long recipe content (read HTML fallback and edit form).
 *
 * Compose cannot combine [androidx.compose.foundation.layout.ColumnScope.weight]
 * with [verticalScroll] on the **same** node: scroll measures unbounded height while
 * weight needs a finite max. The shipped pattern is:
 *
 * 1. **Viewport** — parent `Box` / `Column` child with `weight(1f)` or `fillMaxSize()`
 *    (bounded height).
 * 2. **Scroll surface** — child with [contentScrollSurface] only (`fillMaxWidth` +
 *    `verticalScroll`), no weight on that node.
 *
 * [RecipeReader] and [RecipeFormEditor] must follow this contract so procedure
 * content remains reachable on compact mobile layouts.
 */
object ContentScrollLayout {
    /** Documented contract flag for unit tests. */
    const val USES_VIEWPORT_THEN_SCROLL: Boolean = true

    /** Scroll surface must not use weight (pair is incompatible). */
    const val SCROLL_SURFACE_USES_WEIGHT: Boolean = false

    /**
     * Modifier for scrollable content inside a height-bounded viewport.
     * Callers place this on the **inner** content, not on a weighted sibling header.
     */
    fun contentScrollSurface(base: Modifier, scrollState: ScrollState): Modifier =
        base.fillMaxWidth().verticalScroll(scrollState)
}
