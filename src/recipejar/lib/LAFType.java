package recipejar.lib;

import javax.swing.UIManager;

public enum LAFType {

   SYSTEM(UIManager.getSystemLookAndFeelClassName()),
   METAL(UIManager.getCrossPlatformLookAndFeelClassName()),
   MOTIF("com.sun.java.swing.plaf.motif.MotifLookAndFeel"),
   NIMBUS("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"),
   WINDOWS("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"),
   GTK("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
   private String value;

   @Override
   public String toString() {
      return value;
   }

   private LAFType(String s) {
      value = s;
   }
}
