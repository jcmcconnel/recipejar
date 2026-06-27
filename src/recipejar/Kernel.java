/*
 * The purpose of this system, is to have a globally accessible place to call menu handlers, or
 * other application general functions without requiring any one system to be aware of where
 * the request is actually handled.
 *
 * Action registration is handled by ActionRegistry (instance-scoped per MainFrame).
 *
 * You can have a hashmap of lambda functions by defining an interface with one function prototype.
 * The syntax is [type/interface name] varName = (param1, param2, ... paramn)->{ function here...};
*/
package recipejar;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;

public class Kernel {


   protected static File configDir;
   protected static JMenuBar menuBar = new JMenuBar();
   protected static MainFrame topLevelFrame;

   public static JMenuBar getJMenuBar(JMenu[] menus){
       JMenuBar menuBar = new JMenuBar();
       for(int i=0; i<menus.length; i++){
           menuBar.add(menus[i]); 
       }
       return menuBar;
   }

   /**
    * General functions
    */

   /**
    *
    * @param k
    * @return
    */
   public static boolean isAllowableAccelerator(KeyStroke k) {
      if (isOS("mac os x")) {
         if (k.getModifiers() == KeyEvent.META_DOWN_MASK) {
            if (k.getKeyCode() == KeyEvent.VK_H || k.getKeyCode() == KeyEvent.VK_COMMA
                    || k.getKeyCode() == KeyEvent.VK_Q) {
               return false;
            }
         } else if (k.getModifiers() == (KeyEvent.META_DOWN_MASK + KeyEvent.ALT_DOWN_MASK)) {
            if (k.getKeyCode() == KeyEvent.VK_H) {
               return false;
            }
         }
      } else {
         return true;
      }
      return true;
   }
   public static boolean isOS(String s) {
      return (System.getProperty("os.name").toLowerCase().indexOf(s) != -1);
   }


}