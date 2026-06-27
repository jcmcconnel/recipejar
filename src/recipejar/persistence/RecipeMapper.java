package recipejar.persistence;

import java.util.ArrayList;
import recipejar.domain.Recipe;
import recipejar.filetypes.RecipeFile;
import recipejar.recipe.Ingredient;

/**
 * Maps between persistence-layer {@link RecipeFile} and domain {@link Recipe}.
 */
public final class RecipeMapper {

    private RecipeMapper() {}

    public static Recipe toDomain(RecipeFile recipeFile) {
        Recipe domain = new Recipe();
        domain.setTitle(recipeFile.getTitle());
        domain.setNotes(recipeFile.getNotes());
        domain.setProcedure(recipeFile.getProcedure());

        ArrayList<Ingredient> ingredients = recipeFile.getIngredients();
        if (ingredients != null) {
            domain.setIngredients(new ArrayList<>(ingredients));
        }

        ArrayList<String> labels = recipeFile.getLabels();
        if (labels != null) {
            domain.setLabels(new ArrayList<>(labels));
        }

        return domain;
    }

    public static void applyToRecipeFile(Recipe domain, RecipeFile recipeFile) {
        recipeFile.setTitle(domain.getTitle());
        recipeFile.setNotes(domain.getNotes());
        recipeFile.setProcedure(domain.getProcedure());

        if (domain.getIngredients() != null) {
            recipeFile.setIngredients(new ArrayList<>(domain.getIngredients()));
        }

        if (domain.getLabels() != null && !domain.getLabels().isEmpty()) {
            recipeFile.setLabels(String.join(", ", domain.getLabels()));
        } else {
            recipeFile.setLabels("");
        }
    }
}