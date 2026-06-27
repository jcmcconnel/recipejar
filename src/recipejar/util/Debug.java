package recipejar.util;

/**
 * Simple centralized debug/reporting utility (Phase 5).
 * Can be extended later for levels, file logging, etc.
 * Currently just wraps println but centralizes all debug output.
 */
public final class Debug {

    private static boolean enabled = false;

    private Debug() {}

    public static void log(String msg) {
        if (enabled) {
            System.out.println("[RecipeJar] " + msg);
        }
    }

    public static void log(String tag, String msg) {
        if (enabled) {
            System.out.println("[RecipeJar:" + tag + "] " + msg);
        }
    }

    public static void setEnabled(boolean enabled) {
        Debug.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}