package recipejar

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Structural checks for modal presentation policy:
 * compact / phone → content panel; desktop wide → dialog.
 */
class ModalHostPreferenceTest {

    @Test
    fun preferContentPanelModals_whenCompactOrForced() {
        assertTrue(preferContentPanelModals(forceCompactLayout = true, windowIsCompact = false))
        assertTrue(preferContentPanelModals(forceCompactLayout = false, windowIsCompact = true))
        assertTrue(preferContentPanelModals(forceCompactLayout = true, windowIsCompact = true))
        assertFalse(preferContentPanelModals(forceCompactLayout = false, windowIsCompact = false))
    }

    @Test
    fun contentScrollLayout_usesViewportThenScroll() {
        assertTrue(ContentScrollLayout.USES_VIEWPORT_THEN_SCROLL)
        assertFalse(ContentScrollLayout.SCROLL_SURFACE_USES_WEIGHT)
    }
}
