package recipejar.test.actions;

import javax.swing.JMenuBar;
import recipejar.ApplicationBootstrap;
import recipejar.MainFrame;
import recipejar.actions.ActionRegistry;

/**
 * Headless boot harness for action registry tests.
 * Test classes receive ActionRegistry only; MainFrame stays internal.
 */
public final class ActionTestHarness {

    private MainFrame frame;
    private ActionRegistry registry;

    private ActionTestHarness() {}

    public static ActionTestHarness create() {
        return new ActionTestHarness();
    }

    /**
     * Boot the application headlessly using the given config directory.
     *
     * @param configDir path to RecipeJar config (e.g. ../Test when run from build/)
     * @return the frame's ActionRegistry
     */
    public ActionRegistry boot(String configDir) {
        String[] argv = {"-d", configDir};
        frame = ApplicationBootstrap.bootstrap(argv);
        if (frame == null) {
            throw new IllegalStateException("ApplicationBootstrap returned null for config: " + configDir);
        }
        registry = frame.getActionRegistry();
        return registry;
    }

    public ActionRegistry getRegistry() {
        if (registry == null) {
            throw new IllegalStateException("Harness not booted; call boot(configDir) first");
        }
        return registry;
    }

    /** Open a recipe in the editor panel. */
    public void openRecipe(String filename) {
        requireFrame().ePanel.setRecipePage(filename);
    }

    /** Open a recipe in the reader pane. */
    public void openRecipeReader(String filename) {
        requireFrame().readerPane.setRecipePage(filename);
    }

    /** Clear the editor panel (no recipe loaded). */
    public void clearRecipe() {
        requireFrame().ePanel.clear();
    }

    /** Mark the current recipe dirty so save enablement can be verified. */
    public void markRecipeDirty() {
        requireFrame().ePanel.getNotesField().append(" test-dirty");
    }

    /** Production menu bar for structural identity checks. */
    public JMenuBar getJMenuBar() {
        return requireFrame().getJMenuBar();
    }

    private MainFrame requireFrame() {
        if (frame == null) {
            throw new IllegalStateException("Harness not booted; call boot(configDir) first");
        }
        return frame;
    }

}