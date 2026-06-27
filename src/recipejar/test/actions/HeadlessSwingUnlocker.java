package recipejar.test.actions;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

/**
 * Must run before any heavyweight AWT classes load when the JVM starts with
 * -Djava.awt.headless=true. Allows invisible Swing bootstrap for tests.
 */
final class HeadlessSwingUnlocker {

    private HeadlessSwingUnlocker() {}

    static void unlockIfNeeded() {
        if (!GraphicsEnvironment.isHeadless()) {
            return;
        }
        try {
            Field headless = GraphicsEnvironment.class.getDeclaredField("headless");
            headless.setAccessible(true);
            headless.set(null, Boolean.FALSE);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Cannot unlock Swing in headless JVM", ex);
        }
    }
}