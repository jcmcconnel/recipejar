/*********************************
 * Main frame for the Recipe Jar *
 * James McConnel
 */
package recipejar;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.net.URISyntaxException;

import javax.swing.text.BadLocationException;

import recipejar.filetypes.*;
import recipejar.actions.ActionIds;
import recipejar.actions.ActionRegistry;
import recipejar.persistence.RecipeRepository;
import recipejar.util.Debug;

public class MainFrame extends JFrame {

   public CustomTextPane readerPane;
   public EditorPanel ePanel;
   public recipejar.PreferencesDialog prefDialog;
   public AlphaTab tabbedPane;

   private final ActionRegistry actionRegistry;
   private JSplitPane splitPane;
   private SearchDialog searchDialog;
   private RecipeRepository recipeRepository;

   public void setRecipeRepository(RecipeRepository repo) {
      this.recipeRepository = repo;
   }

   public ActionRegistry getActionRegistry() {
      return actionRegistry;
   }

   /**
    * Frame initializer.
    */
   public MainFrame(String name) {
      super(name);

      Kernel.topLevelFrame = this;
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.setLocation(new Point(10, 20));
      this.setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemClassLoader().getResource("recipejar.gif")));
      if(ProgramVariables.LAF.toString().equals(recipejar.lib.LAFType.METAL.toString())) this.setUndecorated(true);
      this.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

      // Phase 1: action registry
      actionRegistry = new ActionRegistry();

      // Phase 2: editor panel registers edit.* and macro.*
      ePanel = new EditorPanel(actionRegistry);

      // Phase 3: search dialog registers find.* and edit.find
      searchDialog = new SearchDialog(this, false, actionRegistry);
      UnitConverterDialog converterDialog = new UnitConverterDialog(this, false);

      tabbedPane = new AlphaTab(IndexFile.getIndexFile());
      tabbedPane.addHyperlinkListener(ePanel);

      // Phase 4: file, tools, and help actions
      registerFileActions(converterDialog);
      registerToolsActions(converterDialog);
      registerHelpActions();

      // Phase 5: bind buttons and create reader pane after file actions exist
      ePanel.bindButtons(actionRegistry);
      readerPane = new CustomTextPane(actionRegistry);
      tabbedPane.addHyperlinkListener(readerPane);

      splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, readerPane);
      splitPane.setOneTouchExpandable(true);
      this.getContentPane().add(splitPane, BorderLayout.CENTER);

      // Phase 6: menu bar from registry only
      buildMenuBar();

      this.addWindowListener(new WindowAdapter() {
         public void windowClosed(WindowEvent e) {
            actionRegistry.require(ActionIds.FILE_EXIT)
               .actionPerformed(new ActionEvent(e.getWindow(), ActionEvent.ACTION_PERFORMED, "Exit"));
         }
      });

      pack();
   }

   private void registerFileActions(UnitConverterDialog converterDialog) {
      AbstractAction newAction = new AbstractAction("New") {
         public void actionPerformed(ActionEvent e) {
            actionRegistry.require(ActionIds.FILE_TOGGLE_EDIT).actionPerformed(e);
            ePanel.startNew();
         }
      };
      newAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
      newAction.putValue(
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
      );
      actionRegistry.register(ActionIds.FILE_NEW, newAction);

      AbstractAction toggleEditAction = new AbstractAction("Open") {
         public void actionPerformed(ActionEvent e) {
            if (splitPane.getRightComponent().equals(ePanel)) {
               splitPane.setRightComponent(readerPane);
               this.putValue(AbstractAction.NAME, "Open");
               this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
               actionRegistry.require(ActionIds.FILE_SAVE).setEnabled(false);
            } else {
               splitPane.setRightComponent(ePanel);
               this.putValue(AbstractAction.NAME, "Close");
               this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
            }
         }
      };
      toggleEditAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
      toggleEditAction.putValue(
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
      );
      actionRegistry.register(ActionIds.FILE_TOGGLE_EDIT, toggleEditAction);

      AbstractAction saveAction = new AbstractAction("Save") {
         public void actionPerformed(ActionEvent e) {
            try{
               if (!ePanel.save()) {
                  return;
               }
               RecipeFile saved = ePanel.getCurrentRecipeFile();
               if (recipeRepository != null) {
                  recipeRepository.updateIndexFor(saved);
               } else {
                  IndexFile.getIndexFile().updateCategoriesOf(saved);
                  IndexFile.getIndexFile().save();
               }
               Debug.log("MainFrame", "Save Action: " + IndexFile.getIndexFile().getAbsolutePath());
               ePanel.afterSave(readerPane, () -> tabbedPane.reload());
               actionRegistry.require(ActionIds.FILE_TOGGLE_EDIT).actionPerformed(e);
            }
            catch (FileNotFoundException fne) {}
            catch (IOException ioe) {
               System.out.println("there has been an error saving: Mainframe");
               System.out.println(ioe.getCause());
               System.out.println(ioe.getMessage());
            }
            catch (BadLocationException ble) {}
         }
      };
      saveAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
      saveAction.putValue(
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
      );
      saveAction.setEnabled(false);
      actionRegistry.register(ActionIds.FILE_SAVE, saveAction);

      AbstractAction renameAction = new AbstractAction("Rename"){
         public void actionPerformed(ActionEvent e) {
            ePanel.reTitle(JOptionPane.showInputDialog(Kernel.topLevelFrame, "New Title", "New Title Text:", JOptionPane.INFORMATION_MESSAGE));
         }
      };
      renameAction.setEnabled(false);
      actionRegistry.register(ActionIds.FILE_RENAME, renameAction);

      AbstractAction importAction = new AbstractAction("Import"){
         public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(false);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if(fc.showOpenDialog(Kernel.topLevelFrame) == JFileChooser.APPROVE_OPTION){
               System.out.println(fc.getSelectedFile());
               try{
                  RecipeFile f = new RecipeFile(fc.getSelectedFile());
                  RecipeFile imported = f.importRecipe();
                  if (recipeRepository != null) {
                     recipeRepository.addToIndex(imported);
                  } else {
                     IndexFile.getIndexFile().add(imported);
                  }
                  System.out.println(f.getTitle());
               }
               catch(IOException ex){
                  System.out.println("Import failed");
                  System.out.println(ex.getMessage());
               }
               tabbedPane.reload();
            }
         }
      };
      actionRegistry.register(ActionIds.FILE_IMPORT, importAction);

      AbstractAction exportAction = new AbstractAction("Export"){
         public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(false);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if(fc.showOpenDialog(Kernel.topLevelFrame) == JFileChooser.APPROVE_OPTION){
               System.out.println(fc.getSelectedFile());
               File f = fc.getSelectedFile();
               try{
                  ePanel.getCurrentRecipeFile().export(f);
               }
               catch(IOException ex){
                  System.out.println("Export failed");
                  System.out.println(ex.getMessage());
               }
            }
         }
      };
      exportAction.setEnabled(false);
      actionRegistry.register(ActionIds.FILE_EXPORT, exportAction);

      AbstractAction deleteAction = new AbstractAction("Remove") {
         public void actionPerformed(ActionEvent e) {
            Debug.log("MainFrame", "Attempted to delete");
            RecipeFile df = ePanel.getCurrentRecipeFile();
            if (df != null) {
               if (recipeRepository != null) {
                  recipeRepository.deleteRecipeFile(df);
               } else {
                  try {
                     IndexFile.getIndexFile().remove(df);
                     df.delete();
                     IndexFile.getIndexFile().save();
                  } catch (IOException ex) {
                     Debug.log("MainFrame", "Delete failed: " + ex.getMessage());
                  }
               }
            }
            ePanel.clear();
            tabbedPane.reload();
            readerPane.setPage("");
         }
      };
      deleteAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
      deleteAction.putValue(
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)
      );
      deleteAction.setEnabled(false);
      actionRegistry.register(ActionIds.FILE_DELETE, deleteAction);

      AbstractAction printAction = new AbstractAction("Print"){
         public void actionPerformed(ActionEvent e) {
            //TODO
         }
      };
      printAction.setEnabled(false);
      printAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
      printAction.putValue(
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
      );
      actionRegistry.register(ActionIds.FILE_PRINT, printAction);

      AbstractAction exitAction = new AbstractAction("Exit") {
         public void actionPerformed(ActionEvent e) {
            System.exit(0);
         }
      };
      exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
      actionRegistry.register(ActionIds.FILE_EXIT, exitAction);
   }

   private void registerToolsActions(UnitConverterDialog converterDialog) {
      AbstractAction converterAction = new AbstractAction("Unit Converter") {
         public void actionPerformed(ActionEvent e) {
            converterDialog.setLocationRelativeTo(Kernel.topLevelFrame);
            converterDialog.setVisible(!converterDialog.isVisible());
         }
      };
      converterAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
      actionRegistry.register(ActionIds.TOOLS_CONVERTER, converterAction);

      AbstractAction preferencesAction = new AbstractAction("Preferences"){
         public void actionPerformed(ActionEvent e) {
            if(prefDialog == null) {
               prefDialog = new recipejar.PreferencesDialog(Kernel.topLevelFrame, true);
            }
            prefDialog.setLocationRelativeTo(Kernel.topLevelFrame);
            prefDialog.setVisible(true);
         }
      };
      preferencesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
      actionRegistry.register(ActionIds.TOOLS_PREFERENCES, preferencesAction);
   }

   private void registerHelpActions() {
      AbstractAction helpAction = new AbstractAction("On the Web") {
         public void actionPerformed(ActionEvent e) {
            if (java.awt.Desktop.isDesktopSupported()) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(ProgramVariables.HELP_URL.toString()));
                } catch (URISyntaxException ex) {
                    JOptionPane.showMessageDialog(Kernel.topLevelFrame, "You can get help, by visiting \"" + ProgramVariables.HELP_URL.toString() + "\"\n Thanks, \n   -mgmt");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Kernel.topLevelFrame, "You can get help, by visiting \"" + ProgramVariables.HELP_URL.toString() + "\"\n Thanks, \n   -mgmt");
                }
            } else {
                JOptionPane.showMessageDialog(Kernel.topLevelFrame, "You can get help, by visiting \"" + ProgramVariables.HELP_URL.toString() + "\"\n Thanks, \n   -mgmt");
            }
         }
      };
      helpAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_W);
      actionRegistry.register(ActionIds.HELP_WEB, helpAction);

      AbstractAction aboutAction = new AbstractAction("About") {
         public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(Kernel.topLevelFrame, ProgramVariables.ABOUT+"\n"+ProgramVariables.VERSION);
         }
      };
      aboutAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
      actionRegistry.register(ActionIds.HELP_ABOUT, aboutAction);
   }

   private void buildMenuBar() {
      ArrayList<JMenu> menus = new ArrayList<JMenu>();

      JMenu fileMenu = new JMenu("Recipe");
      fileMenu.setMnemonic('R');
      fileMenu.add(actionRegistry.require(ActionIds.FILE_NEW));
      fileMenu.add(actionRegistry.require(ActionIds.FILE_TOGGLE_EDIT));
      fileMenu.add(actionRegistry.require(ActionIds.FILE_SAVE));
      fileMenu.add(actionRegistry.require(ActionIds.FILE_RENAME));
      fileMenu.addSeparator();
      fileMenu.add(actionRegistry.require(ActionIds.FILE_IMPORT));
      fileMenu.add(actionRegistry.require(ActionIds.FILE_EXPORT));
      fileMenu.addSeparator();
      fileMenu.add(actionRegistry.require(ActionIds.FILE_DELETE));
      fileMenu.add(actionRegistry.require(ActionIds.FILE_PRINT));
      fileMenu.addSeparator();
      fileMenu.add(actionRegistry.require(ActionIds.FILE_EXIT));
      menus.add(fileMenu);

      JMenu editMenu = new JMenu("Edit");
      editMenu.setMnemonic('E');
      editMenu.add(actionRegistry.require(ActionIds.EDIT_CUT));
      editMenu.add(actionRegistry.require(ActionIds.EDIT_COPY));
      editMenu.add(actionRegistry.require(ActionIds.EDIT_PASTE));
      editMenu.add(actionRegistry.require(ActionIds.EDIT_SELECT_ALL));
      editMenu.addSeparator();
      editMenu.add(actionRegistry.requireMenu(ActionIds.EDIT_MACROS));
      editMenu.addSeparator();
      editMenu.add(actionRegistry.requireMenu(ActionIds.EDIT_FIND));
      menus.add(editMenu);

      JMenu toolsMenu = new JMenu("Tools");
      toolsMenu.setMnemonic('T');
      toolsMenu.add(actionRegistry.require(ActionIds.TOOLS_CONVERTER));
      toolsMenu.addSeparator();
      toolsMenu.add(actionRegistry.require(ActionIds.TOOLS_PREFERENCES));
      menus.add(toolsMenu);

      JMenu helpMenu = new JMenu("Help");
      helpMenu.setMnemonic('H');
      helpMenu.add(actionRegistry.require(ActionIds.HELP_WEB));
      helpMenu.add(actionRegistry.require(ActionIds.HELP_ABOUT));
      menus.add(helpMenu);

      JMenu[] menuArray = menus.toArray(new JMenu[menus.size()]);
      this.setJMenuBar(Kernel.getJMenuBar(menuArray));
   }

   /********** Main ***********/
   public static void main(String[] argv) {
      MainFrame f = ApplicationBootstrap.bootstrap(argv);
      if (f != null) {
         f.setVisible(true);
      }
   }


   /**
    * Extracts files from the system resources
    * @param test
    * @see #unpack(java.lang.String)
    */
   public static void extractFile(File test, String rsc) {
      //Directories
      if (test.getName().indexOf(".") == -1) {
         test.mkdir();
      } else {
         //Files
         try {
            InputStream in = ClassLoader.getSystemResourceAsStream(test.getName());
            if (in == null) {
               throw new NullPointerException("Failed to access: \"" + test.getName() + "\"");
            }
            if (test.createNewFile()) {
               //emphasis on the successfully.
               String format;
               if ((format = getImageType(test)) != null) {//If the file is an image.
                  ImageIO.write(ImageIO.read(in), format, test);
               } else {//Text files
                  OutputStream out = new FileOutputStream(test);
                  int c = in.read();
                  while (c != -1) {
                     out.write(c);
                     c = in.read();
                  }
                  out.close();
               }
            }
         } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Fatal Error.  See \"errorLog.txt\" for more information.");
            System.exit(1);
         } catch (NullPointerException npe) {
            JOptionPane.showMessageDialog(null, "Fatal Error.  See \"errorLog.txt\" for more information.");
            System.exit(1);
         }
      }
   }

   /**
    * Called by extractFile
    * @param test
    * @return
    * @see #extractFile(java.io.File)
    */
   private static String getImageType(File test) {
      String[] suffixes = ImageIO.getWriterFormatNames();
      int x = test.getName().indexOf(".");
      if (x == -1) {
         return null;//file does not have an extension
      }
      for (int i = 0; i < suffixes.length; i++) {
         if (test.getName().substring(x + 1).equals(suffixes[i])) {
            return suffixes[i];
         }
      }
      return null;
   }

}