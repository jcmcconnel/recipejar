package recipejar.test.actions;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import recipejar.actions.ActionIds;
import recipejar.actions.ActionRegistry;

/**
 * Builds a JMenuBar from registry ids only, mirroring MainFrame menu structure.
 */
public final class MenuAssemblyVerifier {

    private MenuAssemblyVerifier() {}

    public static JMenuBar buildMenuBar(ActionRegistry registry) {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Recipe");
        fileMenu.setMnemonic('R');
        fileMenu.add(registry.require(ActionIds.FILE_NEW));
        fileMenu.add(registry.require(ActionIds.FILE_TOGGLE_EDIT));
        fileMenu.add(registry.require(ActionIds.FILE_SAVE));
        fileMenu.add(registry.require(ActionIds.FILE_RENAME));
        fileMenu.addSeparator();
        fileMenu.add(registry.require(ActionIds.FILE_IMPORT));
        fileMenu.add(registry.require(ActionIds.FILE_EXPORT));
        fileMenu.addSeparator();
        fileMenu.add(registry.require(ActionIds.FILE_DELETE));
        fileMenu.add(registry.require(ActionIds.FILE_PRINT));
        fileMenu.addSeparator();
        fileMenu.add(registry.require(ActionIds.FILE_EXIT));
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        editMenu.add(registry.require(ActionIds.EDIT_CUT));
        editMenu.add(registry.require(ActionIds.EDIT_COPY));
        editMenu.add(registry.require(ActionIds.EDIT_PASTE));
        editMenu.add(registry.require(ActionIds.EDIT_SELECT_ALL));
        editMenu.addSeparator();
        editMenu.add(registry.requireMenu(ActionIds.EDIT_MACROS));
        editMenu.addSeparator();
        editMenu.add(registry.requireMenu(ActionIds.EDIT_FIND));
        menuBar.add(editMenu);

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic('T');
        toolsMenu.add(registry.require(ActionIds.TOOLS_CONVERTER));
        toolsMenu.addSeparator();
        toolsMenu.add(registry.require(ActionIds.TOOLS_PREFERENCES));
        menuBar.add(toolsMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        helpMenu.add(registry.require(ActionIds.HELP_WEB));
        helpMenu.add(registry.require(ActionIds.HELP_ABOUT));
        menuBar.add(helpMenu);

        return menuBar;
    }

    public static List<Action> collectMenuActions(JMenuBar menuBar) {
        List<Action> actions = new ArrayList<>();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            collectFromMenu(menuBar.getMenu(i), actions);
        }
        return actions;
    }

    private static void collectFromMenu(JMenu menu, List<Action> actions) {
        for (int i = 0; i < menu.getMenuComponentCount(); i++) {
            Component component = menu.getMenuComponent(i);
            if (component instanceof JMenuItem) {
                Action action = ((JMenuItem) component).getAction();
                if (action != null) {
                    actions.add(action);
                }
            } else if (component instanceof JMenu) {
                collectFromMenu((JMenu) component, actions);
            }
        }
    }
}