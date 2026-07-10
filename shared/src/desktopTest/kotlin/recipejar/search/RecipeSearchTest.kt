package recipejar.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecipeSearchTest {

    private val catalog = listOf(
        SearchCatalogEntry("Pasta.html", "Pasta"),
        SearchCatalogEntry("Pancakes.html", "Pancakes"),
        SearchCatalogEntry("AppleSauceCobbler.html", "Apple Sauce Cobbler"),
        SearchCatalogEntry("Test1.html", "zzz last"),
    )

    private val fields = mapOf(
        "Pasta.html" to mapOf(
            SearchScope.LABELS to "Italian, Dinner",
            SearchScope.NOTES to "family favorite",
            SearchScope.INGREDIENTS to "1 lb spaghetti",
            SearchScope.PROCEDURE to "boil water",
        ),
        "Pancakes.html" to mapOf(
            SearchScope.LABELS to "Breakfast",
            SearchScope.NOTES to "weekend",
            SearchScope.INGREDIENTS to "flour eggs milk",
            SearchScope.PROCEDURE to "flip carefully",
        ),
        "AppleSauceCobbler.html" to mapOf(
            SearchScope.LABELS to "Dessert",
            SearchScope.NOTES to "",
            SearchScope.INGREDIENTS to "applesauce",
            SearchScope.PROCEDURE to "bake",
        ),
    )

    @Test
    fun emptyQuery_returnsNoHits() {
        assertTrue(filterRecipesByQuery(catalog, "", setOf(SearchScope.TITLES)).isEmpty())
        assertTrue(filterRecipesByQuery(catalog, "   ", setOf(SearchScope.TITLES)).isEmpty())
    }

    @Test
    fun emptyScopes_returnsNoHits() {
        assertTrue(filterRecipesByQuery(catalog, "Pasta", emptySet()).isEmpty())
    }

    @Test
    fun titlesMatch_titleAndFilename() {
        val byTitle = filterRecipesByQuery(catalog, "pancake", setOf(SearchScope.TITLES))
        assertEquals(listOf("Pancakes.html"), byTitle.map { it.filename })
        assertEquals("title", byTitle.single().matchHint)

        val byFile = filterRecipesByQuery(catalog, "AppleSauce", setOf(SearchScope.TITLES))
        assertEquals(listOf("AppleSauceCobbler.html"), byFile.map { it.filename })
    }

    @Test
    fun labelsOnly_ignoresTitle() {
        val hits = filterRecipesByQuery(
            catalog,
            "Breakfast",
            setOf(SearchScope.LABELS),
            fields,
        )
        assertEquals(listOf("Pancakes.html"), hits.map { it.filename })
        assertEquals("labels", hits.single().matchHint)
    }

    @Test
    fun multiScope_combinesHints() {
        val hits = filterRecipesByQuery(
            catalog,
            "pasta",
            setOf(SearchScope.TITLES, SearchScope.INGREDIENTS),
            fields,
        )
        // title "Pasta" + ingredients "spaghetti" only on Pasta; needle "pasta" hits title
        assertEquals(1, hits.size)
        assertEquals("Pasta.html", hits.single().filename)
        assertTrue(hits.single().matchHint.contains("title"))
    }

    @Test
    fun notesIngredientsProcedure_scopes() {
        assertEquals(
            listOf("Pasta.html"),
            filterRecipesByQuery(catalog, "family", setOf(SearchScope.NOTES), fields)
                .map { it.filename },
        )
        assertEquals(
            listOf("Pancakes.html"),
            filterRecipesByQuery(catalog, "flour", setOf(SearchScope.INGREDIENTS), fields)
                .map { it.filename },
        )
        assertEquals(
            listOf("Pancakes.html"),
            filterRecipesByQuery(catalog, "flip", setOf(SearchScope.PROCEDURE), fields)
                .map { it.filename },
        )
    }

    @Test
    fun resultsSortedByTitleKey() {
        // "Apple Sauce Cobbler" before "Pancakes" before "Pasta" before "zzz last"
        val hits = filterRecipesByQuery(catalog, "a", setOf(SearchScope.TITLES))
        // all titles/filenames containing "a" (case insensitive)
        val names = hits.map { it.filename }
        assertTrue(names.indexOf("AppleSauceCobbler.html") < names.indexOf("Pancakes.html"))
        assertTrue(names.indexOf("Pancakes.html") < names.indexOf("Pasta.html"))
    }

    @Test
    fun searchTitleSortKey_foldsAsciiUpper() {
        assertEquals("pasta", searchTitleSortKey("Pasta"))
        assertEquals("a b", searchTitleSortKey("A B"))
    }
}
