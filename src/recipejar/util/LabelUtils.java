package recipejar.util;

/**
 * Shared label comparison (trim + case-insensitive).
 */
public final class LabelUtils {

    private LabelUtils() {}

    public static boolean matches(String label, String candidate) {
        if (label == null || candidate == null) {
            return false;
        }
        return label.trim().equalsIgnoreCase(candidate.trim());
    }
}