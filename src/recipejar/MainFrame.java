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
      if(ProgramVariables.LAF.toString().equals(recipejar.lib.LAFType.METAL.toString())) this.setUndecorated(true);
      this.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

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
      SearchDialog searchDialog = new SearchDialog(this, false);

      /** Action Definitions **/
      ArrayList<JMenu> menus = new ArrayList<JMenu>();

      JMenu fileMenu = new JMenu("Recipe");
      fileMenu.setMnemonic('R');
      // New 
      Kernel.programActions.put("new", new AbstractAction("New") {
         public void actionPerformed(ActionEvent e) {
            Kernel.programActions.get("toggle-edit-mode").actionPerformed(e);
            ePanel.startNew();
         }
      });
      Kernel.programActions.get("new").putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
      Kernel.programActions.get("new").putValue(
                                                Action.ACCELERATOR_KEY,
                                                KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
                                                );
      fileMenu.add(Kernel.programActions.get("new"));

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
      Kernel.programActions.get("toggle-edit-mode").putValue(
                                                Action.ACCELERATOR_KEY,
                                                KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
                                                );
      ePanel.setCancelAction(Kernel.programActions.get("toggle-edit-mode"));
      fileMenu.add(Kernel.programActions.get("toggle-edit-mode"));

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
      Kernel.programActions.get("save").putValue(
                                                 Action.ACCELERATOR_KEY,
                                                 KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
                                                 );
      ePanel.setSaveAction(Kernel.programActions.get("save"));
      Kernel.programActions.get("save").setEnabled(false);
      fileMenu.add(Kernel.programActions.get("save"));

      // Rename
      fileMenu.add(new AbstractAction("Rename"){
         public void actionPerformed(ActionEvent e) {
            //TODO
         }
      });
      fileMenu.addSeparator();

      //Import 
      fileMenu.add(new AbstractAction("Import"){
         public void actionPerformed(ActionEvent e) {
            //TODO What does "Import" mean?
         }
      });
      //Export
      fileMenu.add(new AbstractAction("Export"){
         public void actionPerformed(ActionEvent e) {
            //TODO What does "Export" mean?
         }
      });
      fileMenu.addSeparator();

      // Delete 
      Kernel.programActions.put("delete", new AbstractAction("Remove") {
         public void actionPerformed(ActionEvent e) {
            System.out.println("Attempted to delete");
            IndexFile.getIndexFile().remove(ePanel.getDiskFile());
            ePanel.getDiskFile().delete();
            ePanel.clear();
            tabbedPane.reload();
            readerPane.setPage("");
         }
      });
      Kernel.programActions.get("delete").putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
      Kernel.programActions.get("delete").setEnabled(false);
      fileMenu.add(Kernel.programActions.get("delete"));

      //Print
      fileMenu.add(new AbstractAction("Print"){
         public void actionPerformed(ActionEvent e) {
            //TODO
         }
      });
      fileMenu.addSeparator();

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
      //Cut
      editMenu.add(Kernel.programActions.get("edit-cut"));
      //Copy
      editMenu.add(Kernel.programActions.get("edit-copy"));
      //Paste
      editMenu.add(Kernel.programActions.get("edit-paste"));
      //Select All
      editMenu.add(Kernel.programActions.get("edit-select-all"));
      editMenu.addSeparator();

      editMenu.add(ePanel.getTextActionsMenu());
      menus.add(editMenu);

      //Find
      //editMenu.add(new JMenu("Find"));
      Kernel.programActions.put("find-dialog", new AbstractAction("Find") {
         public void actionPerformed(ActionEvent e) {
            searchDialog.setLocationRelativeTo(Kernel.topLevelFrame);
            searchDialog.setVisible(!searchDialog.isVisible());
         }
      });
      editMenu.add(Kernel.programActions.get("find-dialog"));

      JMenu toolsMenu = new JMenu("Tools");
      toolsMenu.setMnemonic('T');
      Kernel.programActions.put("toggle-converter-dialog", new AbstractAction("Unit Converter") {
         public void actionPerformed(ActionEvent e) {
            converterDialog.setLocationRelativeTo(Kernel.topLevelFrame);
            converterDialog.setVisible(!converterDialog.isVisible());
         }
      });
      Kernel.programActions.get("toggle-converter-dialog").putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
      toolsMenu.add(Kernel.programActions.get("toggle-converter-dialog"));

      toolsMenu.addSeparator();
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
      menus.add(toolsMenu);

      JMenu helpMenu = new JMenu("Help");
      helpMenu.setMnemonic('H');

      //On the Web
      //TODO

      //About
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
          //UIManager.setLookAndFeel(ProgramVariables.LAF.toString());
          //UIManager.setLookAndFeel(recipejar.lib.LAFType.MOTIF.toString());
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
