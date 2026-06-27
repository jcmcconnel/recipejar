package recipejar.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import recipejar.domain.Recipe;
import recipejar.filetypes.IndexFile;
import recipejar.filetypes.RecipeFile;
import recipejar.lib.Anchor;
import recipejar.ProgramVariables;
import recipejar.StringProcessor;

/**
 * File system based repository using existing HTML files.
 * Delegates to current IndexFile/RecipeFile during transition (per plan).
 */
public class FileSystemRecipeRepository implements RecipeRepository {

    private final String baseDir;
    private IndexFile indexFile;  // internal for now

    public FileSystemRecipeRepository(String baseDir) {
        this.baseDir = baseDir.endsWith("/") || baseDir.endsWith("\\") ? baseDir : baseDir + "/";
        IndexFile.setIndexFileLocation(this.baseDir);
        this.indexFile = IndexFile.getIndexFile();
        try {
            RecipeFile.setTemplate(new RecipeFile(ProgramVariables.TEMPLATE_RECIPE.toString()));
        } catch (Exception ignored) {}
    }

    @Override
    public List<Anchor> getAllAnchors() {
        // Transition only: AlphaTab currently uses IndexFile directly for rich sectioned anchors.
        // Full migration will populate this from internal model.
        return new ArrayList<>();
    }

    @Override
    public Recipe loadRecipe(String link) {
        try {
            RecipeFile rf = new RecipeFile(baseDir + link);
            // Convert to domain (simple copy for now)
            Recipe d = new Recipe();
            d.setTitle(rf.getTitle());
            d.setNotes(rf.getNotes());
            d.setProcedure(rf.getProcedure());
            d.setIngredients(rf.getIngredients());
            List<String> lbls = rf.getLabels();
            if (lbls != null) d.setLabels(lbls);
            return d;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + link, e);
        }
    }

    @Override
    public void saveRecipe(Recipe recipe) {
        try {
            String safeName = StringProcessor.removeBadChars(recipe.getTitle()) + ".html";
            String path = baseDir + safeName;
            RecipeFile rf = RecipeFile.newFromTemplate(path);  // or load if exists
            rf.setTitle(recipe.getTitle());
            rf.setNotes(recipe.getNotes());
            rf.setProcedure(recipe.getProcedure());
            rf.setIngredients(new ArrayList<>(recipe.getIngredients()));
            if (recipe.getLabels() != null) {
                String joined = String.join(", ", recipe.getLabels());
                rf.setLabels(joined);
            }
            rf.save();

            // Update index
            indexFile.add(rf);  // this handles category update
            indexFile.save();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save recipe", e);
        }
    }

    @Override
    public void deleteRecipe(Recipe recipe) {
        try {
            String safeName = StringProcessor.removeBadChars(recipe.getTitle()) + ".html";
            java.io.File f = new java.io.File(baseDir + safeName);
            if (f.exists()) f.delete();
            // Note: proper impl would use the file from anchor
            indexFile.remove( /* need the RecipeFile */ null); // placeholder - real impl needs better id
            indexFile.save();
        } catch (Exception e) {
            // log
        }
    }

    @Override
    public void rescanIfNeeded() {
        // TODO: implement merge if index stale
    }

    @Override
    public String getDatabaseLocation() {
        return baseDir;
    }

    @Override
    public void addToIndex(RecipeFile rf) {
        if (indexFile != null && rf != null) {
            try {
                indexFile.add(rf);
                indexFile.save();
            } catch (Exception ignored) {}
        }
    }

    /** Transition helper. */
    public void updateIndexFor(RecipeFile rf) {
        if (indexFile != null && rf != null) {
            try {
                indexFile.updateCategoriesOf(rf);
                indexFile.save();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void deleteByName(String name) {
        if (name == null) return;
        java.io.File f = new java.io.File(baseDir + name);
        if (f.exists()) f.delete();
        // index update done by caller for transition
    }
}