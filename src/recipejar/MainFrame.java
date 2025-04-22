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
      this.setLocation(new Point(10, 20));
      this.setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemClassLoader().getResource("recipejar.gif")));
      this.setUndecorated(true);
      this.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
      //TO DO Figure out how to manage Look and Feel.  Left justify Frame Title??

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
      fileMenu.setMnemonic('F');
      // Edit Mode
      Kernel.programActions.put("toggle-edit-mode", new AbstractAction("Open") {
         public void actionPerformed(ActionEvent e) {
            if (splitPane.getRightComponent().equals(ePanel)) {
               splitPane.setRightComponent(readerPane);
               this.putValue(AbstractAction.NAME, "Open");
               this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
               Kernel.programActions.get("save").setEnabled(false);
               Kernel.programActions.get("delete").setEnabled(true);
            } else {
               splitPane.setRightComponent(ePanel);
               this.putValue(AbstractAction.NAME, "Close");
               this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
               Kernel.programActions.get("delete").setEnabled(false);
            }
         }
      });
      Kernel.programActions.get("toggle-edit-mode").putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
      ePanel.setCancelAction(Kernel.programActions.get("toggle-edit-mode"));
      fileMenu.add(Kernel.programActions.get("toggle-edit-mode"));

      // Delete 
      Kernel.programActions.put("delete", new AbstractAction("Delete") {
         public void actionPerformed(ActionEvent e) {
            System.out.println("Attempted to delete");
            IndexFile.getIndexFile().remove(ePanel.getDiskFile());
            ePanel.getDiskFile().delete();
            ePanel.clear();
            tabbedPane.reload();
            readerPane.setPage("");
         }
      });
      Kernel.programActions.get("delete").putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
      Kernel.programActions.get("delete").setEnabled(false);
      fileMenu.add(Kernel.programActions.get("delete"));

      // New 
      Kernel.programActions.put("new", new AbstractAction("New") {
         public void actionPerformed(ActionEvent e) {
            Kernel.programActions.get("toggle-edit-mode").actionPerformed(e);
            ePanel.startNew();
         }
      });
      Kernel.programActions.get("new").putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
      fileMenu.add(Kernel.programActions.get("new"));

      //Save
      Kernel.programActions.put("save", new AbstractAction("Save") {
         public void actionPerformed(ActionEvent e) {
            try{
               ePanel.save();
               readerPane.setPage(ePanel.getDiskFile());
               tabbedPane.reload();
               Kernel.programActions.get("toggle-edit-mode").actionPerformed(e);
            }
            catch (FileNotFoundException fne) {}
            catch (IOException ioe) {
               System.out.println("there has been an error saving: Mainframe");
               System.out.println(ioe.getCause());
               System.out.println(ioe.getMessage());
            }
            catch (BadLocationException ble) {}
         }
      });
      Kernel.programActions.get("save").putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
      ePanel.setSaveAction(Kernel.programActions.get("save"));
      Kernel.programActions.get("save").setEnabled(false);
      fileMenu.add(Kernel.programActions.get("save"));

      // Exit
      Kernel.programActions.put("exit-program", new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent e) {
                     System.exit(0);
            }
      });
      Kernel.programActions.get("exit-program").putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
      fileMenu.add(Kernel.programActions.get("exit-program"));
      menus.add(fileMenu);

      JMenu editMenu = new JMenu("Edit"); 
      editMenu.setMnemonic('E');
      editMenu.add(ePanel.getTextActionsMenu());
      menus.add(editMenu);
      JMenu toolsMenu = new JMenu("Tools");
      toolsMenu.setMnemonic('T');
      Kernel.programActions.put("preferences-dialog", new AbstractAction("Preferences"){
         public void actionPerformed(ActionEvent e) {
            if(prefDialog == null) {
               prefDialog = new recipejar.PreferencesDialog(Kernel.topLevelFrame, true);
            }
            prefDialog.setLocationRelativeTo(Kernel.topLevelFrame);
            prefDialog.setVisible(true);
         }
      });
      Kernel.programActions.get("preferences-dialog").putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
      toolsMenu.add(Kernel.programActions.get("preferences-dialog"));
      Kernel.programActions.put("toggle-converter-dialog", new AbstractAction("Unit Converter") {
         public void actionPerformed(ActionEvent e) {
            converterDialog.setLocationRelativeTo(Kernel.topLevelFrame);
            converterDialog.setVisible(!converterDialog.isVisible());
         }
      });
      Kernel.programActions.get("toggle-converter-dialog").putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
      toolsMenu.add(Kernel.programActions.get("toggle-converter-dialog"));
      menus.add(toolsMenu);

      JMenu helpMenu = new JMenu("Help");
      helpMenu.setMnemonic('H');
      Kernel.programActions.put("about-dialog", new AbstractAction("About") {
         public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(Kernel.topLevelFrame, "Welcome to RecipeJar!");
         }
      });
      Kernel.programActions.get("about-dialog").putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
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
          UIManager.setLookAndFeel(recipejar.lib.LAFType.NIMBUS.toString());
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
