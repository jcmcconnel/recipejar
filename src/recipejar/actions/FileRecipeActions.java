package recipejar.actions;

public final class FileRecipeActions {

    private FileRecipeActions() {}

    public static void setRecipeOpen(ActionRegistry registry, boolean open) {
        registry.require(ActionIds.FILE_DELETE).setEnabled(open);
        registry.require(ActionIds.FILE_RENAME).setEnabled(open);
        registry.require(ActionIds.FILE_EXPORT).setEnabled(open);
    }
}