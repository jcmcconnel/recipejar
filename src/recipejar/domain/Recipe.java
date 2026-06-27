package recipejar.domain;

import java.util.ArrayList;
import java.util.List;
import recipejar.recipe.Ingredient;
import recipejar.util.LabelUtils;

/**
 * Simple domain model for a recipe (data only, no UI or persistence concerns).
 * Per plan Phase 3.
 */
public class Recipe {

    private String title = "";
    private String notes = "";
    private String procedure = "";
    private List<Ingredient> ingredients = new ArrayList<>();
    private List<String> labels = new ArrayList<>();

    public Recipe() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title != null ? title : ""; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes != null ? notes : ""; }

    public String getProcedure() { return procedure; }
    public void setProcedure(String procedure) { this.procedure = procedure != null ? procedure : ""; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
    }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) {
        this.labels = labels != null ? labels : new ArrayList<>();
    }

    public void addLabel(String label) {
        if (label != null && !label.trim().isEmpty() && !labels.contains(label.trim())) {
            labels.add(label.trim());
        }
    }

    public boolean isLabeled(String label) {
        for (String existing : labels) {
            if (LabelUtils.matches(existing, label)) {
                return true;
            }
        }
        return false;
    }
}