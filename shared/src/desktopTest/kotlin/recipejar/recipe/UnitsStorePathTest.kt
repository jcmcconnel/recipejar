package recipejar.recipe

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Drives the shipped [UnitsCatalog] load/edit/save path used by the units UI:
 * write to a temp file (same format as ~/.recipejar/units.txt), reload, assert dropdown.
 */
class UnitsStorePathTest {

    @Test
    fun loadEditSave_unitsFile_dropdownSeesNewUnit() {
        val dir = Files.createTempDirectory("rj-units").toFile()
        try {
            val file = File(dir, "units.txt")
            // Seed like bundled catalog
            file.writeText(
                UnitsCatalog.serialize(
                    listOf(
                        UnitDef("Cups", "Cup"),
                        UnitDef("oz", "oz"),
                    ),
                ),
                Charsets.UTF_8,
            )
            var catalog = UnitsCatalog.parse(file.readText(Charsets.UTF_8))
            assertEquals(2, catalog.size)

            // User adds a unit (same path as UnitsManagerPanel → save)
            catalog = UnitsCatalog.upsert(
                catalog,
                UnitDef(plural = "pinches", singular = "pinch"),
            )
            file.writeText(UnitsCatalog.serialize(catalog), Charsets.UTF_8)

            val reloaded = UnitsCatalog.parse(file.readText(Charsets.UTF_8))
            assertTrue(UnitsCatalog.contains(reloaded, "pinches"))
            assertTrue(UnitsCatalog.contains(reloaded, "pinch"))
            val labels = UnitsCatalog.dropdownLabels(reloaded)
            assertTrue(labels.contains("pinches"), labels.toString())
            assertTrue(labels.contains("Cups"))
        } finally {
            dir.deleteRecursively()
        }
    }
}
