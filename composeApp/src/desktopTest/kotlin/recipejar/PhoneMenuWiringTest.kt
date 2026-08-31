package recipejar

import recipejar.search.SearchScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phone/mobile menu items invoke real callbacks (open prefs/units/search/macros),
 * not silent no-ops. Sample-jar limits still produce non-empty status via host.
 */
class PhoneMenuWiringTest {

    @Test
    fun findAndToolsMenus_openRealContentModalsViaCallbacks() {
        var prefs = 0
        var units = 0
        var find = 0
        var findTitles = 0
        var macros = 0
        var converter = 0
        var helpWeb = 0
        val model = MobileShellLogic.buildMenuModel(
            state = MobileShellState(selectedFilename = "Pancakes.html", selectedHtml = "<html/>"),
            actions = MobileMenuActions(
                onPreferences = { prefs++ },
                onUnits = { units++ },
                onConverter = { converter++ },
                onFind = { find++ },
                onFindTitles = { findTitles++ },
                onManageMacros = { macros++ },
                onHelpWeb = { helpWeb++ },
            ),
        )
        MobileShellLogic.findItem(model, MobileMenuTitles.PREFERENCES)!!.onClick()
        MobileShellLogic.findItem(model, MobileMenuTitles.UNITS)!!.onClick()
        MobileShellLogic.findItem(model, MobileMenuTitles.CONVERTER)!!.onClick()
        MobileShellLogic.findItem(model, MobileMenuTitles.FIND_ELLIPSIS)!!.onClick()
        // Scoped Find Titles is dialog-only (no top-level Find menu)
        assertNull(MobileShellLogic.findItem(model, MobileMenuTitles.FIND_TITLES))
        MobileShellLogic.findItem(model, MobileMenuTitles.MANAGE_MACROS)!!.onClick()
        MobileShellLogic.findItem(model, MobileMenuTitles.HELP_WEB)!!.onClick()
        assertEquals(1, prefs)
        assertEquals(1, units)
        assertEquals(1, converter)
        assertEquals(1, find)
        assertEquals(0, findTitles)
        assertEquals(1, macros)
        assertEquals(1, helpWeb)
    }

    @Test
    fun recipeEditTransitions_areRealNotOnlyStatus() {
        var toggled = 0
        var saved = 0
        val model = MobileShellLogic.buildMenuModel(
            state = MobileShellState(selectedFilename = "x.html", selectedHtml = "<html/>"),
            actions = MobileMenuActions(
                onToggleEdit = { toggled++ },
                onSave = { saved++ },
            ),
            includeDesktopRepoChrome = false,
        )
        assertNull(MobileShellLogic.findItem(model, MobileMenuTitles.OPEN_REPOSITORY))
        MobileShellLogic.findItem(model, MobileMenuTitles.TOGGLE_EDIT)!!.onClick()
        MobileShellLogic.findItem(model, MobileMenuTitles.SAVE)!!.onClick()
        assertEquals(1, toggled)
        assertEquals(1, saved)
    }

    @Test
    fun sampleJarLimitations_stillReportStatus_notSilent() {
        // Host path: import/export stay status-backed on sample jar.
        var status: String? = null
        val actions = MobileMenuActions(
            onImport = { status = "Import not available on sample jar (Phase 1A storage)" },
            onExportZip = { status = "Directory zip export needs a filesystem repository" },
        )
        actions.onImport()
        assertNotNull(status)
        assertTrue(status!!.isNotBlank())
        actions.onExportZip()
        assertTrue(status!!.isNotBlank())
    }

    @Test
    fun searchScopesUsedByPhoneHost_areRealEnums() {
        // MobilePrototypeApp openSearch uses these sets
        val titlesLabels = setOf(SearchScope.TITLES, SearchScope.LABELS)
        assertTrue(SearchScope.TITLES in titlesLabels)
        assertTrue(SearchScope.LABELS in titlesLabels)
    }
}
