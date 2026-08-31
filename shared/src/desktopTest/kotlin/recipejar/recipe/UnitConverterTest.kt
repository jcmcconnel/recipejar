package recipejar.recipe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UnitConverterTest {

    private fun catalogFromBundledSnippet(): List<UnitDef> = UnitsCatalog.parse(
        """
        oz,oz,Tbsp(0.5)|lbs(0.0625)|Cups(0.125)|ml(29.57)
        Cups,Cup,oz(8)|pt(0.5)|qt(0.25)
        ml,ml,liters(0.001)
        bags,bag
        """.trimIndent(),
    )

    @Test
    fun cupsToOz_usesCatalogFactor() {
        val catalog = catalogFromBundledSnippet()
        val cups = UnitConverter.findUnit(catalog, "Cups")
        val oz = UnitConverter.findUnit(catalog, "oz")
        assertNotNull(cups)
        assertNotNull(oz)
        assertEquals("8", UnitConverter.conversionFactor(cups, oz))
        assertEquals("8", UnitConverter.convert("1", cups, oz))
        assertEquals("16", UnitConverter.convert("2", cups, oz))
        assertEquals("4", UnitConverter.convert("1/2", cups, oz))
    }

    @Test
    fun ozToMl_usesCatalogFactor() {
        val catalog = catalogFromBundledSnippet()
        val oz = UnitConverter.findUnit(catalog, "oz")!!
        val ml = UnitConverter.findUnit(catalog, "ml")!!
        val out = UnitConverter.convert("1", oz, ml)
        assertNotNull(out)
        // 29.57 — decimal, not a kitchen fraction
        assertTrue(out.startsWith("29.5"), "expected ~29.57, got $out")
    }

    @Test
    fun addedUnit_isConvertableAfterUpsert() {
        var catalog = catalogFromBundledSnippet()
        catalog = UnitsCatalog.upsert(
            catalog,
            UnitDef(plural = "handfuls", singular = "handful", conversions = mapOf("oz" to "2")),
        )
        val handfuls = UnitConverter.findUnit(catalog, "handfuls")!!
        val oz = UnitConverter.findUnit(catalog, "oz")!!
        assertEquals("4", UnitConverter.convert("2", handfuls, oz))
        assertTrue(UnitsCatalog.dropdownLabels(catalog).contains("handfuls"))
    }

    @Test
    fun noFactor_returnsNull() {
        val catalog = catalogFromBundledSnippet()
        val bags = UnitConverter.findUnit(catalog, "bags")!!
        val oz = UnitConverter.findUnit(catalog, "oz")!!
        assertNull(UnitConverter.convert("1", bags, oz))
        assertTrue(UnitConverter.convertableUnits(catalog).none { it.matches("bags") })
    }

    @Test
    fun mixedNumberAndRange() {
        assertEquals(2.5f, UnitConverter.parseMixedNumber("2 1/2"))
        assertEquals(0.5f, UnitConverter.parseMixedNumber("1/2"))
        val catalog = catalogFromBundledSnippet()
        val cups = UnitConverter.findUnit(catalog, "Cups")!!
        val oz = UnitConverter.findUnit(catalog, "oz")!!
        assertEquals("8-16", UnitConverter.convert("1-2", cups, oz))
    }

    @Test
    fun conversionTargets_resolveCatalogUnits() {
        val catalog = catalogFromBundledSnippet()
        val cups = UnitConverter.findUnit(catalog, "Cups")!!
        val targets = UnitConverter.conversionTargets(cups, catalog)
        assertTrue(targets.any { it.matches("oz") })
        assertFalse(targets.any { it.matches("bags") })
    }
}
