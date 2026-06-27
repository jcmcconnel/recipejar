package recipejar.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JMenu;

public class ActionRegistry {

    private final Map<String, Action> actions = new HashMap<>();
    private final Map<String, JMenu> menus = new HashMap<>();

    public void register(String id, Action action) {
        if (actions.containsKey(id)) {
            throw new IllegalStateException("Duplicate action id: " + id);
        }
        actions.put(id, action);
    }

    public void registerMenu(String id, JMenu menu) {
        JMenu existing = menus.get(id);
        if (existing != null) {
            if (existing == menu) {
                return;
            }
            throw new IllegalStateException("Duplicate menu id: " + id);
        }
        menus.put(id, menu);
    }

    public Action require(String id) {
        Action action = actions.get(id);
        if (action == null) {
            throw new IllegalStateException("Missing action: " + id);
        }
        return action;
    }

    public JMenu requireMenu(String id) {
        JMenu menu = menus.get(id);
        if (menu == null) {
            throw new IllegalStateException("Missing menu: " + id);
        }
        return menu;
    }

    public Optional<Action> find(String id) {
        return Optional.ofNullable(actions.get(id));
    }

    public Set<String> ids() {
        return Collections.unmodifiableSet(new HashSet<>(actions.keySet()));
    }

    public Collection<Action> actions() {
        return Collections.unmodifiableCollection(actions.values());
    }

    public void unregister(String id) {
        actions.remove(id);
    }

    public void clearPrefix(String prefix) {
        Set<String> actionKeys = new HashSet<>(actions.keySet());
        for (String key : actionKeys) {
            if (key.startsWith(prefix)) {
                actions.remove(key);
            }
        }
        Set<String> menuKeys = new HashSet<>(menus.keySet());
        for (String key : menuKeys) {
            if (key.startsWith(prefix)) {
                menus.remove(key);
            }
        }
    }

    public static String sanitizeId(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}