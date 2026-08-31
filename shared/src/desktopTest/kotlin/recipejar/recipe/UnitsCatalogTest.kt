package recipejar.recipe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun serializeRoundTrip_preservesUnitsForDropdown() {
        val original = UnitsCatalog.parse(
            """
            ; comment
            oz,oz,ml(29.57)
            Cups,Cup,oz(8)|pt(0.5)
            bags,bag
            """.trimIndent(),
        )
        val text = UnitsCatalog.serialize(original)
        val reloaded = UnitsCatalog.parse(text)
        assertEquals(original.size, reloaded.size)
        for (u in original) {
            val match = reloaded.first { it.plural == u.plural }
            assertEquals(u.singular, match.singular)
            assertEquals(u.conversions, match.conversions)
        }
        // After edit path: upsert + serialize + parse → dropdown sees new unit
        val edited = UnitsCatalog.upsert(
            reloaded,
            UnitDef(plural = "handfuls", singular = "handful"),
        )
        val saved = UnitsCatalog.serialize(edited)
        val catalog = UnitsCatalog.parse(saved)
        assertTrue(UnitsCatalog.contains(catalog, "handfuls"))
        val labels = UnitsCatalog.dropdownLabels(catalog)
        assertTrue(labels.contains("handfuls"))
    }

    @Test
    fun upsertAndRemove_maintainCatalogForPicker() {
        var units = UnitsCatalog.parse("oz,oz\n")
        units = UnitsCatalog.upsert(units, UnitDef("dashes", "dash"))
        assertTrue(UnitsCatalog.contains(units, "dashes"))
        assertTrue(UnitsCatalog.contains(units, "dash"))
        units = UnitsCatalog.remove(units, "oz")
        assertFalse(UnitsCatalog.contains(units, "oz"))
        assertEquals(listOf("", "dashes"), UnitsCatalog.dropdownLabels(units))
    }
}
