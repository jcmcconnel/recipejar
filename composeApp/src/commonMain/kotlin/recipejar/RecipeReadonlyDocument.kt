package recipejar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import recipejar.domain.Recipe
import recipejar.html.RecipeSerializer
import recipejar.recipe.Ingredient

/**
 * Always-on rendered readonly recipe body (not HTML source, not dependent on KCEF).
 * Works identically in Desktop wide, Phone/compact, Android, and iOS.
 *
 * Parses with the shipped [RecipeSerializer] so content matches edit/save round-trips.
 * Notes and procedure use [htmlFragmentToAnnotatedString] so macro markup
 * (`<strong>`, `<em>`, underline/color spans, links, `<br/>`, `<p>`) is visible.
 */
@Composable
fun RecipeReadonlyDocument(
    html: String,
    modifier: Modifier = Modifier,
) {
    val recipe = remember(html) {
        try {
            RecipeSerializer.parse(html)
        } catch (_: Exception) {
            Recipe(title = titleFromRecipeHtml(html))
        }
    }
    RecipeReadonlyDocument(
        recipe = recipe,
        modifier = modifier,
    )
}

@Composable
fun RecipeReadonlyDocument(
    recipe: Recipe,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Column(
        ContentScrollLayout.contentScrollSurface(modifier, scroll)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            recipe.title.ifBlank { "Untitled" },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))

        if (recipe.notes.isNotBlank()) {
            SectionHeading("Notes")
            val notesText = remember(recipe.notes) { htmlFragmentToAnnotatedString(recipe.notes) }
            Text(
                text = notesText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
        }

        if (recipe.ingredients.isNotEmpty()) {
            SectionHeading("You will need")
            recipe.ingredients.forEach { ing ->
                Text(
                    formatIngredientLine(ing),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        if (recipe.procedure.isNotBlank()) {
            SectionHeading("Procedure")
            val procedureText = remember(recipe.procedure) {
                htmlFragmentToAnnotatedString(recipe.procedure)
            }
            Text(
                text = procedureText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Bottom padding so last lines stay clear of gesture/home areas on phone.
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
    )
    HorizontalDivider(modifier = Modifier.padding(bottom = 6.dp))
}

internal fun formatIngredientLine(ing: Ingredient): String {
    val parts = listOf(ing.quantity, ing.unit, ing.name)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    return if (parts.isEmpty()) "•" else "• " + parts.joinToString(" ")
}
