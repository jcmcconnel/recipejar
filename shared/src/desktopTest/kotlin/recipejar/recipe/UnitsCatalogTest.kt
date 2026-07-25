package recipejar.recipe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnitsCatalogTest {

    @Test
    fun parseSkipsCommentsAndBlanks() {
        val text = """
            ; comment
            oz,oz,ml(29.57)
            
            Cups,Cup,oz(8)|pt(0.5)
        """.trimIndent()
        val units = UnitsCatalog.parse(text)
        assertEquals(2, units.size)
        assertEquals("Cups", units[0].plural) // sorted
        assertEquals("oz", units[1].plural)
        assertEquals("Cup", units[0].singular)
        assertEquals("29.57", units[1].conversions["ml"])
        assertEquals("8", units[0].conversions["oz"])
        assertEquals("0.5", units[0].conversions["pt"])
    }

    @Test
    fun parseSingularOnlyAndNoConversions() {
        val units = UnitsCatalog.parse("bags,bag\nCount,Count\n")
        assertEquals(2, units.size)
        val bags = units.first { it.plural == "bags" }
        assertEquals("bag", bags.singular)
        assertTrue(bags.conversions.isEmpty())
    }

    @Test
    fun dropdownLabelsBlankFirstAndPreserveUnknown() {
        val units = UnitsCatalog.parse("oz,oz\nCups,Cup\n")
        val labels = UnitsCatalog.dropdownLabels(units, current = "handful")
        assertEquals("", labels.first())
        assertTrue(labels.contains("oz"))
        assertTrue(labels.contains("Cups"))
        assertTrue(labels.contains("handful"))
    }
}
