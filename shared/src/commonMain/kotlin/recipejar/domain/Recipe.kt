package recipejar.domain

import recipejar.recipe.Ingredient

/**
 * Core domain model, ported/adapted from original Recipe/RecipeFile data.
 * Focus on fields needed for HTML roundtrip: title, notes, ingredients, procedure, labels, meta.
 * No UI/docs listeners.
 * NOTE: ingredients/labels/meta are mutable for parse/serialize efficiency (per port of orig ArrayList/HashMap); do not share instances across recipes.
 */
data class Recipe(
    var title: String = "",
    var notes: String = "",
    val ingredients: MutableList<Ingredient> = mutableListOf(),
    var procedure: String = "",
    val labels: MutableList<String> = mutableListOf(),
    val meta: MutableMap<String, String> = mutableMapOf()
) {
    fun setLabels(text: String) {
        labels.clear()
        if (text.isNotBlank()) {
            labels.addAll(text.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
    }

    fun getLabelsAsString(): String = labels.joinToString(", ")

    fun addLabel(s: String) {
        if (s.isNotBlank() && !labels.contains(s.trim())) labels.add(s.trim())
    }
}
