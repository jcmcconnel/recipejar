package recipejar.persistence;

import java.util.List;
import recipejar.domain.Recipe;
import recipejar.filetypes.RecipeFile;
import recipejar.lib.Anchor;

/**
 * Repository for recipes - abstracts persistence and index management.
 * Per approved plan.
 */
public interface RecipeRepository {

    /** Get the list of recipe anchors for the index (for AlphaTab etc). */
    List<Anchor> getAllAnchors();

    /** Load a recipe by its link/filename. */
    Recipe loadRecipe(String link);

    /** Save (create or update) a recipe. Handles title rename etc. */
    void saveRecipe(Recipe recipe);

    /** Delete a recipe. */
    void deleteRecipe(Recipe recipe);

    /** Trigger a rescan if needed (for future). */
    void rescanIfNeeded();

    /** Get the underlying directory for current impl (temporary bridge). */
    String getDatabaseLocation();

    /** Transition: add a RecipeFile to the index (will be internalized later). */
    void addToIndex(RecipeFile rf);

    /** Update index categories for an existing recipe file. */
    void updateIndexFor(RecipeFile rf);

    /** Remove from index and delete the recipe file from disk. */
    void deleteRecipeFile(RecipeFile rf);
}