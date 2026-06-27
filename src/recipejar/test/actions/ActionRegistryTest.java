package recipejar.test.actions;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import recipejar.actions.ActionIds;
import recipejar.actions.ActionRegistry;

/**
 * Headless action registry architecture test suite.
 * Tests access ActionRegistry only via ActionTestHarness.
 */
public final class ActionRegistryTest {

    private static final String CONFIG_DIR = "../Test";
    private static final String TEST_RECIPE = "AppleSauceCobbler.html";
    private static final String MACROS_FILE = "../Test/settings/macros.txt";

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        HeadlessSwingUnlocker.unlockIfNeeded();

        final ActionTestHarness harness = ActionTestHarness.create();
        final List<Throwable> errors = new ArrayList<>();

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    ActionRegistry registry = harness.boot(CONFIG_DIR);

                    runTest("1. All ActionIds static constants resolve", errors, new Runnable() {
                        @Override
                        public void run() {
                            testAllActionIdsResolve(registry);
                        }
                    });

                    runTest("2. Dynamic macro ids present; count matches macros.txt", errors, new Runnable() {
                        @Override
                        public void run() {
                            testDynamicMacroIds(registry);
                        }
                    });

                    runTest("3. Find action ids present", errors, new Runnable() {
                        @Override
                        public void run() {
                            testFindActionIds(registry);
                        }
                    });

                    runTest("4. requireMenu edit.macros and edit.find non-empty", errors, new Runnable() {
                        @Override
                        public void run() {
                            testMacroAndFindMenusNonEmpty(registry);
                        }
                    });

                    runTest("5. edit.cut != edit.copy", errors, new Runnable() {
                        @Override
                        public void run() {
                            testCutCopyDistinct(registry);
                        }
                    });

                    runTest("6. MenuAssemblyVerifier builds menu from registry", errors, new Runnable() {
                        @Override
                        public void run() {
                            testMenuAssemblyVerifier(registry);
                        }
                    });

                    runTest("7. Production menu Action identity matches registry", errors, new Runnable() {
                        @Override
                        public void run() {
                            testProductionMenuIdentity(harness, registry);
                        }
                    });

                    runTest("8. FileRecipeActions enable on open - editor path", errors, new Runnable() {
                        @Override
                        public void run() {
                            testEnableOnOpenEditor(harness, registry);
                        }
                    });

                    runTest("9. Enable on open - reader path", errors, new Runnable() {
                        @Override
                        public void run() {
                            testEnableOnOpenReader(harness, registry);
                        }
                    });

                    runTest("10. Disable on clear", errors, new Runnable() {
                        @Override
                        public void run() {
                            testDisableOnClear(harness, registry);
                        }
                    });

                    runTest("11. Save enabled on dirty; identity stable", errors, new Runnable() {
                        @Override
                        public void run() {
                            testSaveEnabledOnDirty(harness, registry);
                        }
                    });
                } catch (Throwable t) {
                    errors.add(t);
                }
            }
        });

        for (Throwable t : errors) {
            failed++;
            System.err.println("FAIL: " + t.getMessage());
            t.printStackTrace(System.err);
        }

        System.out.println();
        System.out.println("ActionRegistryTest: " + passed + " passed, " + failed + " failed");
        System.exit(failed > 0 ? 1 : 0);
    }

    private static void runTest(String name, List<Throwable> errors, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (Throwable t) {
            failed++;
            System.err.println("FAIL: " + name + " - " + t.getMessage());
            t.printStackTrace(System.err);
            errors.add(t);
        }
    }

    private static void testAllActionIdsResolve(ActionRegistry registry) {
        registry.require(ActionIds.FILE_SAVE);
        registry.require(ActionIds.FILE_DELETE);
        registry.require(ActionIds.FILE_RENAME);
        registry.require(ActionIds.FILE_EXPORT);
        registry.require(ActionIds.FILE_IMPORT);
        registry.require(ActionIds.FILE_TOGGLE_EDIT);
        registry.require(ActionIds.FILE_NEW);
        registry.require(ActionIds.FILE_PRINT);
        registry.require(ActionIds.FILE_EXIT);

        registry.require(ActionIds.EDIT_CUT);
        registry.require(ActionIds.EDIT_COPY);
        registry.require(ActionIds.EDIT_PASTE);
        registry.require(ActionIds.EDIT_SELECT_ALL);

        registry.require(ActionIds.TOOLS_CONVERTER);
        registry.require(ActionIds.TOOLS_PREFERENCES);

        registry.require(ActionIds.HELP_WEB);
        registry.require(ActionIds.HELP_ABOUT);
    }

    private static void testDynamicMacroIds(ActionRegistry registry) {
        assertTrue(registry.find("macro.Bold").isPresent(), "macro.Bold missing");
        assertTrue(registry.find("macro.Italics").isPresent(), "macro.Italics missing");
        assertTrue(registry.find("macro.Underline").isPresent(), "macro.Underline missing");
        assertTrue(registry.find("macro.Color").isPresent(), "macro.Color missing");
        assertTrue(registry.find("macro.Paragraph").isPresent(), "macro.Paragraph missing");
        assertTrue(registry.find("macro.Link").isPresent(), "macro.Link missing");

        int macroCount = 0;
        for (String id : registry.ids()) {
            if (id.startsWith("macro.")) {
                macroCount++;
            }
        }

        try {
            int expected = countMacroDataRows(MACROS_FILE);
            assertEquals(expected, macroCount, "macro.* count vs macros.txt data rows");
        } catch (Exception ex) {
            throw new AssertionError("failed to read macros file: " + ex.getMessage());
        }
    }

    private static void testFindActionIds(ActionRegistry registry) {
        registry.require(ActionIds.FIND_ALL);
        registry.require(ActionIds.FIND_TITLES);
        registry.require(ActionIds.FIND_LABELS);
        registry.require(ActionIds.FIND_NOTES);
        registry.require(ActionIds.FIND_INGREDIENTS);
        registry.require(ActionIds.FIND_PROCEDURES);
    }

    private static void testMacroAndFindMenusNonEmpty(ActionRegistry registry) {
        JMenu macrosMenu = registry.requireMenu(ActionIds.EDIT_MACROS);
        JMenu findMenu = registry.requireMenu(ActionIds.EDIT_FIND);
        assertTrue(macrosMenu.getMenuComponentCount() > 0, "edit.macros menu empty");
        assertTrue(findMenu.getMenuComponentCount() > 0, "edit.find menu empty");
    }

    private static void testCutCopyDistinct(ActionRegistry registry) {
        Action cut = registry.require(ActionIds.EDIT_CUT);
        Action copy = registry.require(ActionIds.EDIT_COPY);
        assertTrue(cut != copy, "edit.cut and edit.copy must be distinct instances");
    }

    private static void testMenuAssemblyVerifier(ActionRegistry registry) {
        JMenuBar assembled = MenuAssemblyVerifier.buildMenuBar(registry);
        assertTrue(assembled.getMenuCount() == 4, "assembled menu should have 4 top-level menus");
        List<Action> actions = MenuAssemblyVerifier.collectMenuActions(assembled);
        assertTrue(actions.size() > 0, "assembled menu should contain actions");
        assertTrue(actions.contains(registry.require(ActionIds.FILE_SAVE)), "file.save in assembled menu");
        assertTrue(actions.contains(registry.require(ActionIds.EDIT_CUT)), "edit.cut in assembled menu");
    }

    private static void testProductionMenuIdentity(ActionTestHarness harness, ActionRegistry registry) {
        JMenuBar production = harness.getJMenuBar();
        assertTrue(production != null, "production menu bar missing");

        Collection<Action> registryActions = registry.actions();
        List<Action> productionActions = MenuAssemblyVerifier.collectMenuActions(production);

        for (Action productionAction : productionActions) {
            assertTrue(registryActions.contains(productionAction),
                    "production menu action not found in registry: " + productionAction);
        }

        assertMenuContainsAction(production, registry.require(ActionIds.FILE_NEW));
        assertMenuContainsAction(production, registry.require(ActionIds.FILE_SAVE));
        assertMenuContainsAction(production, registry.require(ActionIds.EDIT_CUT));
        assertMenuContainsAction(production, registry.require(ActionIds.TOOLS_CONVERTER));
        assertMenuContainsAction(production, registry.require(ActionIds.HELP_ABOUT));

        JMenu macrosMenu = registry.requireMenu(ActionIds.EDIT_MACROS);
        for (int i = 0; i < macrosMenu.getMenuComponentCount(); i++) {
            Component component = macrosMenu.getMenuComponent(i);
            if (component instanceof JMenuItem) {
                Action macroAction = ((JMenuItem) component).getAction();
                assertTrue(registryActions.contains(macroAction), "macro action not in registry");
            }
        }
    }

    private static void testEnableOnOpenEditor(ActionTestHarness harness, ActionRegistry registry) {
        harness.clearRecipe();
        assertRecipeFileActionsDisabled(registry);

        harness.openRecipe(TEST_RECIPE);
        assertRecipeFileActionsEnabled(registry);
    }

    private static void testEnableOnOpenReader(ActionTestHarness harness, ActionRegistry registry) {
        harness.clearRecipe();
        assertRecipeFileActionsDisabled(registry);

        harness.openRecipeReader(TEST_RECIPE);
        assertRecipeFileActionsEnabled(registry);
    }

    private static void testDisableOnClear(ActionTestHarness harness, ActionRegistry registry) {
        harness.openRecipe(TEST_RECIPE);
        assertRecipeFileActionsEnabled(registry);

        harness.clearRecipe();
        assertRecipeFileActionsDisabled(registry);
        assertTrue(!registry.require(ActionIds.FILE_SAVE).isEnabled(), "file.save disabled on clear");
    }

    private static void testSaveEnabledOnDirty(ActionTestHarness harness, ActionRegistry registry) {
        harness.clearRecipe();
        harness.openRecipe(TEST_RECIPE);

        Action saveBeforeDirty = registry.require(ActionIds.FILE_SAVE);
        assertTrue(!saveBeforeDirty.isEnabled(), "file.save disabled before dirty");

        harness.markRecipeDirty();
        assertTrue(saveBeforeDirty.isEnabled(), "file.save enabled after dirty");

        harness.clearRecipe();
        assertTrue(!saveBeforeDirty.isEnabled(), "file.save disabled after clear");

        harness.openRecipe(TEST_RECIPE);
        Action saveAfterReopen = registry.require(ActionIds.FILE_SAVE);
        assertTrue(saveBeforeDirty == saveAfterReopen, "file.save identity must be stable across context changes");
        assertTrue(!saveAfterReopen.isEnabled(), "file.save disabled after reopen without dirty");
    }

    private static void assertRecipeFileActionsEnabled(ActionRegistry registry) {
        assertTrue(registry.require(ActionIds.FILE_DELETE).isEnabled(), "file.delete should be enabled");
        assertTrue(registry.require(ActionIds.FILE_RENAME).isEnabled(), "file.rename should be enabled");
        assertTrue(registry.require(ActionIds.FILE_EXPORT).isEnabled(), "file.export should be enabled");
    }

    private static void assertRecipeFileActionsDisabled(ActionRegistry registry) {
        assertTrue(!registry.require(ActionIds.FILE_DELETE).isEnabled(), "file.delete should be disabled");
        assertTrue(!registry.require(ActionIds.FILE_RENAME).isEnabled(), "file.rename should be disabled");
        assertTrue(!registry.require(ActionIds.FILE_EXPORT).isEnabled(), "file.export should be disabled");
    }

    private static void assertMenuContainsAction(JMenuBar menuBar, Action expected) {
        List<Action> actions = MenuAssemblyVerifier.collectMenuActions(menuBar);
        assertTrue(actions.contains(expected), "menu missing action: " + expected);
    }

    private static int countMacroDataRows(String path) throws Exception {
        int count = 0;
        BufferedReader reader = new BufferedReader(new FileReader(path));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith(";")) {
                    count++;
                }
            }
        } finally {
            reader.close();
        }
        return count;
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " (expected " + expected + ", got " + actual + ")");
        }
    }
}