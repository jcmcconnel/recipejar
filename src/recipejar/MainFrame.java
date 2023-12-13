/*********************************
 * Main frame for the Recipe Jar *
 * James McConnel
 */
package recipejar;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.text.BadLocationException;

import recipejar.filetypes.*;

public class MainFrame extends JFrame {

   public CustomTextPane readerPane;
   public EditorPanel ePanel;
   public recipejar.PreferencesDialog prefDialog;
   
   /**
    * Frame initializer.
    */
   public MainFrame(String name) {
      super(name);

      Kernel.topLevelFrame = this;
      // Frame configuration
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.setLocationRelativeTo(null);

      // Component initialization
      AlphaTab tabbedPane = new AlphaTab(IndexFile.getIndexFile());

      readerPane = new CustomTextPane();
      ePanel = new EditorPanel();
      tabbedPane.addHyperlinkListener(readerPane);
      tabbedPane.addHyperlinkListener(ePanel);

      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, readerPane);
      splitPane.setOneTouchExpandable(true);
      this.getContentPane().add(splitPane, BorderLayout.CENTER);

      UnitConverterDialog converterDialog = new UnitConverterDialog(this, false);

      /** Action Definitions **/
      ArrayList<JMenu> menus = new ArrayList<JMenu>();

      JMenu fileMenu = new JMenu("File");
      // Edit Mode
      Kernel.programActions.put("toggle-edit-mode", new AbstractAction("Open") {
         public void actionPerformed(ActionEvent e) {
            if (splitPane.getRightComponent().equals(ePanel)) {
               splitPane.setRightComponent(readerPane);
               this.putValue(AbstractAction.NAME, "Open");
               Kernel.programActions.get("save").setEnabled(false);
               Kernel.programActions.get("delete").setEnabled(true);
            } else {
               splitPane.setRightComponent(ePanel);
               this.putValue(AbstractAction.NAME, "Close");
               Kernel.programActions.get("save").setEnabled(true);
               Kernel.programActions.get("delete").setEnabled(false);
            }
         }
      });
      ePanel.setCancelAction(Kernel.programActions.get("toggle-edit-mode"));
      fileMenu.add(Kernel.programActions.get("toggle-edit-mode"));

      // Delete 
      Kernel.programActions.put("delete", new AbstractAction("Delete") {
         public void actionPerformed(ActionEvent e) {
             System.out.println("Attempted to delete");
             //IndexFile.getIndexFile().remove(ePanel.getDiskFile());
         }
      });
      Kernel.programActions.get("delete").setEnabled(false);
      fileMenu.add(Kernel.programActions.get("delete"));

      // New 
      Kernel.programActions.put("new", new AbstractAction("New") {
         public void actionPerformed(ActionEvent e) {
            ePanel.startNew();
            Kernel.programActions.get("toggle-edit-mode").actionPerformed(e);
         }
      });
      fileMenu.add(Kernel.programActions.get("new"));

      //Save
      Kernel.programActions.put("save", new AbstractAction("Save") {
         public void actionPerformed(ActionEvent e) {
            try{
               System.out.println(ePanel.getDiskFile().getName());
               if (ePanel.getDiskFile().exists() && !ePanel.getDiskFile().getName().equals("Test1.html")) {
                  System.out.println("attempted to save existing");
               } else {
                  ePanel.save();
                  readerPane.setPage(ePanel.getDiskFile());
               }
            }
            catch (FileNotFoundException fne) {}
            catch (IOException ioe) {}
            catch (BadLocationException ble) {}
         }
      });
      ePanel.setSaveAction(Kernel.programActions.get("save"));
      Kernel.programActions.get("save").setEnabled(false);
      fileMenu.add(Kernel.programActions.get("save"));

      // Exit
      Kernel.programActions.put("exit-program", new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent e) {
                     System.exit(0);
            }
      });
      fileMenu.add(Kernel.programActions.get("exit-program"));
      menus.add(fileMenu);

      JMenu editMenu = new JMenu("Edit"); 
      editMenu.add(ePanel.getTextActionsMenu());
      menus.add(editMenu);
      JMenu toolsMenu = new JMenu("Tools");
      Kernel.programActions.put("preferences-dialog", new AbstractAction("Preferences"){
         public void actionPerformed(ActionEvent e) {
            if(prefDialog == null) {
               prefDialog = new recipejar.PreferencesDialog(Kernel.topLevelFrame, true);
            }
            prefDialog.setLocationRelativeTo(Kernel.topLevelFrame);
            prefDialog.setVisible(true);
         }
      });
      toolsMenu.add(Kernel.programActions.get("preferences-dialog"));
      Kernel.programActions.put("toggle-converter-dialog", new AbstractAction("Unit Converter") {
         public void actionPerformed(ActionEvent e) {
            converterDialog.setLocationRelativeTo(Kernel.topLevelFrame);
            converterDialog.setVisible(!converterDialog.isVisible());
         }
      });
      toolsMenu.add(Kernel.programActions.get("toggle-converter-dialog"));
      menus.add(toolsMenu);

      JMenu helpMenu = new JMenu("Help");
      Kernel.programActions.put("about-dialog", new AbstractAction("About") {
         public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(Kernel.topLevelFrame, "Welcome to RecipeJar!");
         }
      });
      helpMenu.add(Kernel.programActions.get("about-dialog"));
      menus.add(helpMenu);
      /*** End Menubar ***/

      // Close window button
      this.addWindowListener(new WindowAdapter() {
         public void windowClosed(WindowEvent e) {
            Kernel.programActions.get("exit-program")
               .actionPerformed(new ActionEvent(e.getWindow(), ActionEvent.ACTION_PERFORMED, "Exit"));
         }
      });

      JMenu[] menuArray = new JMenu[menus.size()];
      menuArray = menus.toArray(menuArray);
      this.setJMenuBar(Kernel.getJMenuBar(menuArray));
      pack();
   }

   /********** Main ***********/
   public static void main(String[] argv) {
      Kernel.configDir = new File("%HOME/.RecipeJar");
      if (argv.length > 1) {
            if (argv[0].contains("-d")) {
                     Kernel.configDir = new File(argv[1]);
            }
      }
      System.setProperty("java.util.prefs.userRoot", Kernel.configDir.getAbsolutePath());
      ProgramVariables.DIR_PROGRAM.set(Kernel.configDir.getAbsolutePath()+"/");
      try {
      UIManager.setLookAndFeel(ProgramVariables.LAF.toString());
      } 
      catch(UnsupportedLookAndFeelException e){}
      catch(ClassNotFoundException e){}
      catch(InstantiationException e){}
      catch(IllegalAccessException e){}

      try {
         recipejar.recipe.Unit.readUnitsFromFile(ProgramVariables.FILE_UNIT.toString());
      } catch (FileNotFoundException e) {
         e.printStackTrace();
         return;
      }
      IndexFile.setIndexFileLocation(ProgramVariables.DIR_DB.toString());
      try {
         RecipeFile.setTemplate(new RecipeFile(ProgramVariables.TEMPLATE_RECIPE.toString()));
      } catch (IOException e) {
      } catch (NullPointerException e) {
      }

      MainFrame f = new MainFrame("RecipeJar");
      f.setVisible(true);
   }
}
