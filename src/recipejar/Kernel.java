/*
 * The purpose of this system, is to have a globally accessible place to call menu handlers, or
 * other application general functions without requiring any one system to be aware of where
 * the request is actually handled.
 *
 * To add an action call: kernel.programActions.put([NAME], <AbstractAction>);
 * To call an action call: kernel.programActions.get([NAME]).actionPerformed(<ActionEvent>);
 * To get a reference to the AbstractAction to for instance, bind it to a button...
 * call: kernel.programActions.get([NAME])
 *
 * You can have a hashmap of lambda functions by defining an interface with one function prototype.
 * The syntax is [type/interface name] varName = (param1, param2, ... paramn)->{ function here...};
*/
package recipejar;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.HashMap;
public class Kernel {


   protected static HashMap<String, AbstractAction> programActions = new HashMap<String, AbstractAction>();
   protected static JMenuBar menuBar = new JMenuBar();

    //public static JMenuBar getJMenuBar(){
    //     JMenu fileMenu = new JMenu("File");
    //     fileMenu.add(kernel.programActions.get("toggle-edit-mode"));
    //     fileMenu.add(kernel.programActions.get("exit-program"));
    //     menuBar.add(fileMenu);
    //     return menuBar;
    //}

    public static JMenuBar getJMenuBar(JMenu[] menus){
        JMenuBar menuBar = new JMenuBar();
        for(int i=0; i<menus.length; i++){
            menuBar.add(menus[i]); 
        }
        return menuBar;
    }

}
