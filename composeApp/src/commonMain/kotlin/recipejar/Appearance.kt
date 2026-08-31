package recipejar

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * User-selectable appearance. Default is Forest — not Material3's pink/purple seed.
 */
enum class AppearanceId(val id: String, val label: String) {
    FOREST("forest", "Forest (green)"),
    OCEAN("ocean", "Ocean (blue)"),
    SLATE("slate", "Slate (neutral)"),
    WARM("warm", "Warm (amber)"),
    ROSE("rose", "Rose (Material pink)"),
    COOKBOOK("cookbook", "Cookbook"),
}

object AppearanceTheme {
    const val DEFAULT_ID: String = "cookbook"

    fun all(): List<AppearanceId> = AppearanceId.entries

    fun parse(id: String?): AppearanceId =
        AppearanceId.entries.firstOrNull { it.id.equals(id?.trim(), ignoreCase = true) }
            ?: AppearanceId.COOKBOOK

    fun schemeFor(id: String?, dark: Boolean = false): ColorScheme {
        val appearance = parse(id)
        return if (dark) darkScheme(appearance) else lightScheme(appearance)
    }

    /**
     * True when [primary] looks like Material3's default pink/purple seed
     * (`#6750A4`) or a similarly magenta-leaning hue.
     */
    fun isPinkOrPurplePrimary(primary: Color): Boolean {
        val r = primary.red
        val g = primary.green
        val b = primary.blue
        val magentaLean = r > 0.30f && b > 0.40f && g < 0.48f && (r + b) > (g * 2.2f)
        val materialPurple = almostHex(primary, 0x6750A4) || almostHex(primary, 0xD0BCFF)
        return magentaLean || materialPurple
    }

    fun isPinkOrPurplePrimary(scheme: ColorScheme): Boolean =
        isPinkOrPurplePrimary(scheme.primary)

    private fun almostHex(c: Color, rgb: Int): Boolean {
        val tr = ((rgb shr 16) and 0xFF) / 255f
        val tg = ((rgb shr 8) and 0xFF) / 255f
        val tb = (rgb and 0xFF) / 255f
        return kotlin.math.abs(c.red - tr) < 0.04f &&
            kotlin.math.abs(c.green - tg) < 0.04f &&
            kotlin.math.abs(c.blue - tb) < 0.04f
    }

    private fun lightScheme(id: AppearanceId): ColorScheme = when (id) {
        AppearanceId.COOKBOOK -> lightColorScheme(
            primary = Color(0xFFC9562A),
            onPrimary = Color(0xFFF7E9D4),
            primaryContainer = Color(0xFFECD6BA),
            onPrimaryContainer = Color(0xFF3A261C),
            secondary = Color(0xFF5C4030),
            secondaryContainer = Color(0xFFEEDCC2),
            tertiary = Color(0xFF8A6F55),
            background = Color(0xFFF7E9D4),
            onBackground = Color(0xFF3A261C),
            surface = Color(0xFFFAF0E0),
            onSurface = Color(0xFF3A261C),
            surfaceVariant = Color(0xFFEEDCC2),
            onSurfaceVariant = Color(0xFF5C4030),
            outline = Color(0xFFD6BA96),
        )
        AppearanceId.FOREST -> lightColorScheme(
            primary = Color(0xFF2E7D32),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFC8E6C9),
            onPrimaryContainer = Color(0xFF1B5E20),
            secondary = Color(0xFF558B2F),
            secondaryContainer = Color(0xFFDCEDC8),
            tertiary = Color(0xFF00695C),
        )
        AppearanceId.OCEAN -> lightColorScheme(
            primary = Color(0xFF1565C0),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFBBDEFB),
            onPrimaryContainer = Color(0xFF0D47A1),
            secondary = Color(0xFF0277BD),
            secondaryContainer = Color(0xFFB3E5FC),
            tertiary = Color(0xFF00838F),
        )
        AppearanceId.SLATE -> lightColorScheme(
            primary = Color(0xFF455A64),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFCFD8DC),
            onPrimaryContainer = Color(0xFF263238),
            secondary = Color(0xFF546E7A),
            secondaryContainer = Color(0xFFECEFF1),
            tertiary = Color(0xFF37474F),
        )
        AppearanceId.WARM -> lightColorScheme(
            primary = Color(0xFF6D4C41),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD7CCC8),
            onPrimaryContainer = Color(0xFF3E2723),
            secondary = Color(0xFFEF6C00),
            secondaryContainer = Color(0xFFFFE0B2),
            tertiary = Color(0xFF8D6E63),
        )
        AppearanceId.ROSE -> lightColorScheme()
    }

    private fun darkScheme(id: AppearanceId): ColorScheme = when (id) {
        AppearanceId.COOKBOOK -> darkColorScheme(
            primary = Color(0xFFC9562A),
            onPrimary = Color(0xFFF7E9D4),
            primaryContainer = Color(0xFF5C4030),
            onPrimaryContainer = Color(0xFFF7E9D4),
            secondary = Color(0xFFEEDCC2),
            background = Color(0xFF3A261C),
            onBackground = Color(0xFFF7E9D4),
            surface = Color(0xFF3A261C),
            onSurface = Color(0xFFF7E9D4),
        )
        AppearanceId.FOREST -> darkColorScheme(
            primary = Color(0xFF81C784),
            onPrimary = Color(0xFF1B5E20),
            primaryContainer = Color(0xFF2E7D32),
            onPrimaryContainer = Color(0xFFC8E6C9),
            secondary = Color(0xFFAED581),
        )
        AppearanceId.OCEAN -> darkColorScheme(
            primary = Color(0xFF90CAF9),
            onPrimary = Color(0xFF0D47A1),
            primaryContainer = Color(0xFF1565C0),
            onPrimaryContainer = Color(0xFFE3F2FD),
            secondary = Color(0xFF4FC3F7),
        )
        AppearanceId.SLATE -> darkColorScheme(
            primary = Color(0xFFB0BEC5),
            onPrimary = Color(0xFF263238),
            primaryContainer = Color(0xFF455A64),
            onPrimaryContainer = Color(0xFFECEFF1),
            secondary = Color(0xFF90A4AE),
        )
        AppearanceId.WARM -> darkColorScheme(
            primary = Color(0xFFBCAAA4),
            onPrimary = Color(0xFF3E2723),
            primaryContainer = Color(0xFF6D4C41),
            onPrimaryContainer = Color(0xFFEFEBE9),
            secondary = Color(0xFFFFB74D),
        )
        AppearanceId.ROSE -> darkColorScheme()
    }

    /** Contrast sanity: onPrimary should not match primary luminance class. */
    fun schemeHasReadablePrimary(scheme: ColorScheme): Boolean {
        val p = scheme.primary.luminance()
        val on = scheme.onPrimary.luminance()
        return kotlin.math.abs(p - on) > 0.2f
    }
}
