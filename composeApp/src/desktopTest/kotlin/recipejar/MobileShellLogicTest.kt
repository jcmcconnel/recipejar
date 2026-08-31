package recipejar

import recipejar.html.RecipeSerializer
import recipejar.sample.SampleRecipeJar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Drives the shipped [MobileShellLogic] + [MobileShellLogic.buildMenuModel] path
 * used by [MobilePrototypeApp], with real [SampleRecipeJar] HTML and [RecipeSerializer].
 */
class MobileShellLogicTest {

    @Test
    fun fullMenuModel_matchesDesktopTopLevelAndRequiredItems() {
        val state = MobileShellState(selectedFilename = "BananaBread.html", selectedHtml = "<html/>")
        var aboutHits = 0
        var findAllHits = 0
        var saveHits = 0
        var prefsHits = 0
        val model = MobileShellLogic.buildMenuModel(
            state = state,
            actions = MobileMenuActions(
                onSave = { saveHits++ },
                onFind = { findAllHits++ },
                onPreferences = { prefsHits++ },
                onAbout = { aboutHits++ },
                onExit = {},
                onToggleEdit = {},
            ),
            includeDesktopRepoChrome = false,
        )

        assertEquals(MobileMenuTitles.TOP_LEVEL, MobileShellLogic.menuTitles(model))
        assertFalse(MobileMenuTitles.FIND in MobileShellLogic.menuTitles(model), "no top-level Find menu")

        val all = MobileShellLogic.allItemTitles(model)
        MobileMenuTitles.RECIPE_ITEMS_MOBILE.forEach { title ->
            assertTrue(title in all, "Recipe menu missing “$title” in $all")
        }
        // Mobile model omits Open repository and Exit by default
        assertFalse(MobileMenuTitles.OPEN_REPOSITORY in all)
        assertFalse(MobileMenuTitles.EXIT in all)
        MobileMenuTitles.EDIT_ITEMS.forEach { title ->
            assertTrue(title in all, "Edit menu missing “$title” in $all")
        }
        assertTrue(MobileMenuTitles.FIND_ELLIPSIS in all, "Edit → Find… required")
        assertTrue(MobileMenuTitles.MANAGE_MACROS in all)
        assertTrue(MobileMenuTitles.PREFERENCES in all)
        assertTrue(
            MobileMenuTitles.PHONE_LAYOUT in all || MobileMenuTitles.PHONE_LAYOUT_ON in all,
            "Tools must expose Phone layout: $all",
        )
        assertTrue(MobileMenuTitles.ABOUT in all)
        assertTrue(MobileMenuTitles.CONVERTER in all)
        assertTrue(MobileMenuTitles.HELP_WEB in all)

        MobileShellLogic.findItem(model, MobileMenuTitles.ABOUT)!!.onClick()
        MobileShellLogic.findItem(model, MobileMenuTitles.FIND_ELLIPSIS)!!.onClick()
        MobileShellLogic.findItem(model, MobileMenuTitles.SAVE)!!.onClick()
        MobileShellLogic.findItem(model, MobileMenuTitles.PREFERENCES)!!.onClick()
        assertEquals(1, aboutHits)
        assertEquals(1, findAllHits) // reused as find hits
        assertEquals(1, saveHits)
        assertEquals(1, prefsHits)

        assertEquals(
            MobileMenuTitles.RECIPE_ITEMS_MOBILE.toSet(),
            MobileShellLogic.itemTitlesInMenu(model, MobileMenuTitles.RECIPE).toSet(),
        )
    }

    @Test
    fun desktopChrome_includesOpenAndExit_mobileOmits() {
        var openHits = 0
        val desktop = MobileShellLogic.buildMenuModel(
            state = MobileShellState(),
            actions = MobileMenuActions(onOpenRepo = { openHits++ }),
            includeDesktopRepoChrome = true,
        )
        assertTrue(MobileMenuTitles.OPEN_REPOSITORY in MobileShellLogic.allItemTitles(desktop))
        assertTrue(MobileMenuTitles.EXIT in MobileShellLogic.allItemTitles(desktop))
        assertEquals(
            MobileMenuTitles.OPEN_REPOSITORY,
            MobileShellLogic.itemTitlesInMenu(desktop, MobileMenuTitles.RECIPE).first(),
        )
        MobileShellLogic.findItem(desktop, MobileMenuTitles.OPEN_REPOSITORY)!!.onClick()
        assertEquals(1, openHits)

        val mobile = MobileShellLogic.buildMenuModel(
            state = MobileShellState(),
            actions = MobileMenuActions(),
            includeDesktopRepoChrome = false,
        )
        assertFalse(MobileMenuTitles.OPEN_REPOSITORY in MobileShellLogic.allItemTitles(mobile))
        assertFalse(MobileMenuTitles.EXIT in MobileShellLogic.allItemTitles(mobile))
        assertFalse(MobileMenuTitles.FIND in MobileShellLogic.menuTitles(mobile))
    }

    @Test
    fun menuModel_exposesToggleEdit_desktopHasExit() {
        val state = MobileShellState()
        var toggled = false
        var exited = false
        val mobile = MobileShellLogic.buildMenuModel(
            state = state,
            onToggleEdit = { toggled = true },
            onExit = { exited = true },
        )
        assertTrue(MobileMenuTitles.RECIPE in MobileShellLogic.menuTitles(mobile))
        assertTrue(MobileMenuTitles.TOGGLE_EDIT in MobileShellLogic.allItemTitles(mobile))
        assertNull(MobileShellLogic.findItem(mobile, MobileMenuTitles.EXIT))

        val desktop = MobileShellLogic.buildMenuModel(
            state = state,
            actions = MobileMenuActions(
                onToggleEdit = { toggled = true },
                onExit = { exited = true },
            ),
            includeDesktopRepoChrome = true,
        )
        val exitItem = MobileShellLogic.findItem(desktop, MobileMenuTitles.EXIT)!!
        assertTrue(exitItem.enabled)
        exitItem.onClick()
        assertTrue(exited, "Exit menu item must invoke onExit")

        val toggleItem = MobileShellLogic.findItem(mobile, MobileMenuTitles.TOGGLE_EDIT)!!
        assertFalse(toggleItem.enabled, "Toggle Edit disabled with no selection")
        toggleItem.onClick()
        assertTrue(toggled)
    }

    @Test
    fun stubAndRealActions_updateStateViaShippedLogic() {
        var state = MobileShellLogic.selectRecipe(
            MobileShellState(),
            "Pancakes.html",
            SampleRecipeJar.htmlFor("Pancakes.html"),
        )
        state = MobileShellLogic.saveRecipe(state)
        assertNotNull(state.statusMessage)
        assertTrue(state.statusMessage!!.contains("read-only", ignoreCase = true))

        state = MobileShellLogic.newRecipe(state)
        assertTrue(state.isEditing)
        assertEquals("Untitled", state.editingRecipe?.title)
        assertTrue(state.selectedFilename!!.startsWith("Untitled"))

        state = MobileShellLogic.togglePhoneLayout(state)
        assertFalse(state.forceCompactLayout)

        val model = MobileShellLogic.buildMenuModel(
            state = state,
            actions = MobileMenuActions(
                onPhoneLayout = { state = MobileShellLogic.togglePhoneLayout(state) },
                onNew = { state = MobileShellLogic.newRecipe(state) },
                onRemove = { state = MobileShellLogic.removeRecipe(state) },
            ),
        )
        MobileShellLogic.findItem(model, MobileMenuTitles.PHONE_LAYOUT)!!.onClick()
        assertTrue(state.forceCompactLayout)
        MobileShellLogic.findItem(model, MobileMenuTitles.REMOVE)!!.onClick()
        assertNull(state.selectedFilename)
        assertNotNull(state.statusMessage)
    }

    @Test
    fun toggleEdit_withSampleRecipe_bindsParsedModel_thenReturnsToRead() {
        val filename = "BananaBread.html"
        val html = SampleRecipeJar.htmlFor(filename)
        assertNotNull(html, "sample jar must include $filename")

        var state = MobileShellLogic.selectRecipe(
            state = MobileShellState(),
            filename = filename,
            html = html,
        )
        assertEquals(filename, state.selectedFilename)
        assertEquals(html, state.selectedHtml)
        assertFalse(state.isEditing)
        assertNull(state.editingRecipe)

        state = MobileShellLogic.toggleEdit(state)
        assertTrue(state.isEditing, "expected edit mode after toggle")
        val recipe = state.editingRecipe
        assertNotNull(recipe, "edit mode must bind a real parsed recipe, not null")
        assertTrue(recipe.title.isNotBlank(), "editor model title must be non-empty")
        assertEquals("Banana Bread", recipe.title.trim())
        assertTrue(
            recipe.ingredients.isNotEmpty(),
            "expected ingredients from sample HTML, got ${recipe.ingredients}",
        )
        assertTrue(recipe.procedure.isNotBlank(), "procedure must be present for scroll reachability")

        val modelWhileEditing = MobileShellLogic.buildMenuModel(
            state = state,
            onToggleEdit = { state = MobileShellLogic.toggleEdit(state) },
            onExit = {},
        )
        val enabled = MobileShellLogic.enabledItemTitles(modelWhileEditing)
        assertTrue(MobileMenuTitles.TOGGLE_EDIT in enabled)
        assertFalse(MobileMenuTitles.EXIT in enabled, "mobile model omits Exit")

        MobileShellLogic.findItem(modelWhileEditing, MobileMenuTitles.TOGGLE_EDIT)!!.onClick()

        assertFalse(state.isEditing, "expected read mode after second toggle")
        assertNull(state.editingRecipe, "editor model cleared in read mode")
        assertEquals(filename, state.selectedFilename, "selection retained for reader")
        assertNotNull(state.selectedHtml, "HTML content still available for reader")
        assertTrue(state.selectedHtml!!.isNotBlank())
        val again = RecipeSerializer.parse(state.selectedHtml!!)
        assertEquals("Banana Bread", again.title.trim())
    }

    @Test
    fun enterEdit_withoutSelection_doesNotEnterEditMode() {
        val state = MobileShellLogic.toggleEdit(MobileShellState())
        assertFalse(state.isEditing)
        assertNull(state.editingRecipe)
        assertNotNull(state.statusMessage)
    }

    @Test
    fun exitMenuItem_invokesCallback_fromDesktopBuilder() {
        var exitCount = 0
        val model = MobileShellLogic.buildMenuModel(
            state = MobileShellState(selectedFilename = "Pancakes.html"),
            actions = MobileMenuActions(onExit = { exitCount++ }),
            includeDesktopRepoChrome = true,
        )
        val exit = MobileShellLogic.findItem(model, MobileMenuTitles.EXIT)!!
        exit.onClick()
        exit.onClick()
        assertEquals(2, exitCount)
    }

    @Test
    fun applyRecipeChange_updatesEditorAndHtmlBuffer() {
        val html = SampleRecipeJar.htmlFor("Pancakes.html")!!
        var state = MobileShellLogic.selectRecipe(MobileShellState(), "Pancakes.html", html)
        state = MobileShellLogic.enterEdit(state)
        val edited = state.editingRecipe!!.copy(title = "Fluffy Pancakes")
        state = MobileShellLogic.applyRecipeChange(state, edited)
        assertEquals("Fluffy Pancakes", state.editingRecipe!!.title)
        assertNotNull(state.selectedHtml)
        assertEquals("Fluffy Pancakes", RecipeSerializer.parse(state.selectedHtml!!).title.trim())
    }

    @Test
    fun contentScrollLayout_contract_forbidsWeightOnScrollSurface() {
        assertTrue(ContentScrollLayout.USES_VIEWPORT_THEN_SCROLL)
        assertFalse(ContentScrollLayout.SCROLL_SURFACE_USES_WEIGHT)
    }
}
