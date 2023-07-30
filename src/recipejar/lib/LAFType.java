package recipejar.lib;

import javax.swing.UIManager;

public enum LAFType {

   SYSTEM(UIManager.getSystemLookAndFeelClassName()),
   METAL(UIManager.getCrossPlatformLookAndFeelClassName()),
   MOTIF("com.sun.java.swing.plaf.motif.MotifLookAndFeel"),
   NIMBUS("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
   private String value;

   @Override
   public String toString() {
      return value;
   }

   private LAFType(String s) {
      value = s;
   }
}
