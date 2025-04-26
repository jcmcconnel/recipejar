/*
 * EditorPanel.java
 * @author James McConnel
 * Created on September 18, 2008, 9:19 AM
 * @version %I%, %G%
 */
package recipejar;
 
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.util.Collections;
import java.util.ArrayList;
import java.io.IOException;
import java.io.FileNotFoundException;
import recipejar.filetypes.RecipeFile;
import recipejar.filetypes.IndexFile;
import recipejar.lib.AbstractCharDelineatedFile;
import recipejar.recipe.Recipe;
import recipejar.lib.AbstractTextAction;
import recipejar.lib.MacroTextAction;
import javax.swing.text.BadLocationException;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

import javax.swing.DefaultCellEditor;

/**
 *
 * @author  James McConnel
 */
public class EditorPanel extends JPanel implements HyperlinkListener {

   /************Instance Variables*****************/
    private Recipe recipeModel;
    private RecipeFile diskFile;
    private Boolean recipeChanged;

    private AbstractCharDelineatedFile macroTextActionsFile;
    private ArrayList<MacroTextAction> macroActions;
    private JMenu textActionsMenu;
    private JMenu popupTextActionsMenu;
    private JPopupMenu popupMenu;

   /**
    * Creates new EditorPanel
    * 
    */
   public EditorPanel() {
      initComponents();
      jScrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      try {
         textActionsMenu = new JMenu("Macros");
         textActionsMenu.setMnemonic('M');
         popupTextActionsMenu = new JMenu("Macros");
         popupTextActionsMenu.setMnemonic('M');
         popupMenu = new JPopupMenu();

         AbstractTextAction creationBuffer = new AbstractTextAction("Cut"){
            public void actionPerformed(ActionEvent e) {
               this.text.cut();
            }
         };
         creationBuffer.addField(titleField);
         creationBuffer.addField(notesField);
         creationBuffer.addField(procedureField);
         creationBuffer.setOnlyActiveOnSelection(true);
         creationBuffer.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
         creationBuffer.putValue(
             Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
         );
         Kernel.programActions.put("edit-cut", creationBuffer);
         popupMenu.add(Kernel.programActions.get("edit-cut"));

         creationBuffer = new AbstractTextAction("Copy"){
            public void actionPerformed(ActionEvent e) {
               this.text.copy();
            }
         };
         creationBuffer.addField(titleField);
         creationBuffer.addField(notesField);
         creationBuffer.addField(procedureField);
         creationBuffer.setOnlyActiveOnSelection(true);
         creationBuffer.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
         creationBuffer.putValue(
             Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
         );
         Kernel.programActions.put("edit-copy", creationBuffer);
         popupMenu.add(Kernel.programActions.get("edit-copy"));

         creationBuffer = new AbstractTextAction("Paste"){
            public void actionPerformed(ActionEvent e) {
               this.text.paste();
            }
         };
         creationBuffer.addField(titleField);
         creationBuffer.addField(notesField);
         creationBuffer.addField(procedureField);
         creationBuffer.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
         creationBuffer.putValue(
             Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
         );
         Kernel.programActions.put("edit-paste", creationBuffer);
         popupMenu.add(Kernel.programActions.get("edit-paste"));

         creationBuffer = new AbstractTextAction("Select All"){
            public void actionPerformed(ActionEvent e) {
               this.text.selectAll();
            }
         };
         creationBuffer.addField(titleField);
         creationBuffer.addField(notesField);
         creationBuffer.addField(procedureField);
         creationBuffer.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
         creationBuffer.putValue(
             Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
         );
         Kernel.programActions.put("edit-select-all", creationBuffer);
         popupMenu.add(Kernel.programActions.get("edit-select-all"));
         popupMenu.addSeparator();

         readMacrosFromFile(ProgramVariables.FILE_MACRO.toString());
         popupMenu.add(popupTextActionsMenu);
      } catch(FileNotFoundException fnf) {

      }
   }


   public void setRecipePage(String fileName){
        try {
            diskFile = new recipejar.filetypes.RecipeFile(recipejar.filetypes.IndexFile.getDatabaseLocation()+"/"+fileName);
            recipeModel = new Recipe(diskFile);
            titleField.setDocument(recipeModel.getTitleModel());
            titleField.setEditable(true);
            notesField.setDocument(recipeModel.getNotesModel());
            iListTable1.setModel(recipeModel.getTableModel());
            procedureField.setDocument(recipeModel.getProcedureModel());
            Kernel.topLevelFrame.setTitle(diskFile.getTitle()+" - "+"RecipeJar");

            Kernel.programActions.get("save").setEnabled(true);
            Kernel.programActions.get("delete").setEnabled(true);
            Kernel.programActions.get("change-title").setEnabled(true);
        } 
        catch(IOException ioe){}
        catch(BadLocationException ble){}
   }

    /**
     * Intended as a listener for the IndexPane.
     */
   public void hyperlinkUpdate(HyperlinkEvent e){
       if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
          setRecipePage(e.getDescription());
       }
   }

   /**
    *
    *
    */
   public void clear() {
      titleField.setText("Open a Recipe, or click \"New\".");
      titleField.setEditable(false);
      notesField.setText("");
      notesField.setEditable(false);
      iListTable1.setEnabled(false);
      procedureField.setText("");
      procedureField.setEditable(false);
      saveButton.setEnabled(false);
      recipeModel = null;
   }


   /**
   *
   * @return
   * @throws java.io.FileNotFoundException
   * @throws java.io.IOException
   **/
   public boolean save() throws FileNotFoundException, IOException, BadLocationException {
      if (isTitleChanged()) { //Saving a new recipe.
         System.out.println("Title change detected");
         if (StringProcessor.isBadTitle(titleField.getText())) {
            JOptionPane.showMessageDialog(Kernel.topLevelFrame, "Please enter a valid title for your recipe.");
            return false;
         } else {
            if(diskFile.getPath().equals(ProgramVariables.TEMPLATE_RECIPE.toString())){
               System.out.println("Saving new");
            }
            diskFile = RecipeFile.newFromTemplate(ProgramVariables.buildAbsoluteFileNameFrom(titleField.getText()));
            System.out.println("Name of file to be saved: "+diskFile.getName());
            recipeModel.setDiskFile(diskFile);
            recipeModel.writeToDisk();
            IndexFile.getIndexFile().add(diskFile);
         }
      } else recipeModel.writeToDisk();

      recipeChanged = false;
      return true;
   }

   public boolean retitle(String newTitle) {
      // Changing the name of this recipe.
      if (StringProcessor.isBadTitle(newTitle)) {
         JOptionPane.showMessageDialog(Kernel.topLevelFrame, "Please enter a valid title for your recipe.");
         return false;
      } else {
         try {
            recipeModel.getTitleModel().remove(0, recipeModel.getTitleModel().getLength());
            recipeModel.getTitleModel().insertString(0, newTitle, null);
            //recipeModel.writeToDisk();
            //IndexFile.getIndexFile().add(diskFile);
         }
         catch(BadLocationException e){}
      }
      return true;
   }


   /**
      *
      * @return
      */
   public boolean startNew() {
      System.out.println("Starting new");
      if (recipeModel == null || !recipeModel.hasRecipeChanged() ||
          (recipeModel.hasRecipeChanged() && JOptionPane.showConfirmDialog(this.getParent(),
               "You have unsaved changes.\n"
               + "Are you sure you want to leave this recipe?\n"
               + "Any unsaved changes will be discarded.") == JOptionPane.YES_OPTION)) {
         try {
            diskFile = new recipejar.filetypes.RecipeFile(ProgramVariables.TEMPLATE_RECIPE.toString());
            recipeModel = new Recipe(diskFile);
            titleField.setDocument(recipeModel.getTitleModel());
            titleField.setEditable(true);
            notesField.setDocument(recipeModel.getNotesModel());
            iListTable1.setModel(recipeModel.getTableModel());
            procedureField.setDocument(recipeModel.getProcedureModel());
         } 
         catch(IOException ioe){}
         catch(BadLocationException ble){}

         Kernel.programActions.get("save").setEnabled(false);
         Kernel.programActions.get("delete").setEnabled(false);
         Kernel.programActions.get("change-title").setEnabled(false);

         return true;
      } else {
         return false;
      }
   }

   /***************Getters and Setters**********************/


   public JTextArea getNotesField() {
      return notesField;
   }

   public JTextArea getProcedureField() {
      return procedureField;
   }

   /**
    *
    * @return
    */
   public void setSaveAction(Action a) {
      saveButton.setAction(a);
   }
   public void setCancelAction(Action a) {
      cancelButton.setAction(a);
   }

   /****************Other********************/

   public boolean isTitleChanged() {
      if (recipeModel == null) {
         if (titleField.getText().isEmpty()) {
            return false;
         } else {
            return true;
         }
      } else {
         return recipeModel.hasTitleChanged();
      }
   }

   /*******************non public******************/
   

   /***************Interface implementations************************/
   /**
    * @param listener
    */
   @Override
   public void addKeyListener(KeyListener listener) {
      notesField.addKeyListener(listener);
      //TODO ingredientList1.addKeyListener(listener);
      procedureField.addKeyListener(listener);
   }

   @SuppressWarnings("unchecked")
   private void initComponents() {
      java.awt.GridBagConstraints gridBagConstraints;

      titlePanel = new javax.swing.JPanel();
      titleField = new javax.swing.JTextField();
      notesPanel = new javax.swing.JPanel();
      jScrollPane3 = new javax.swing.JScrollPane();
      notesField = new javax.swing.JTextArea();
      procedurePanel = new javax.swing.JPanel();
      jScrollPane1 = new javax.swing.JScrollPane();
      procedureField = new javax.swing.JTextArea();
      labelsPanel = new javax.swing.JPanel();
      labelField = new javax.swing.JTextField();
      buttonsPanel = new javax.swing.JPanel();
      cancelButton = new javax.swing.JButton();
      saveButton = new javax.swing.JButton();
      jPanel1 = new javax.swing.JPanel();
      jScrollPane2 = new javax.swing.JScrollPane();
      iListTable1 = new recipejar.IListTable();

      setLayout(new java.awt.GridBagLayout());

      titlePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Title"));
      titlePanel.setMaximumSize(new java.awt.Dimension(200, 50));
      titlePanel.setMinimumSize(new java.awt.Dimension(200, 50));
      titlePanel.setPreferredSize(new java.awt.Dimension(200, 50));
      titlePanel.setLayout(new java.awt.BorderLayout());
      titlePanel.add(titleField, java.awt.BorderLayout.PAGE_END);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 0;
      add(titlePanel, gridBagConstraints);

      notesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Notes"));
      notesPanel.setMinimumSize(new java.awt.Dimension(300, 100));
      notesPanel.setPreferredSize(new java.awt.Dimension(400, 125));

      jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

      notesField.setColumns(20);
      notesField.setRows(2);
      notesField.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mousePressed(java.awt.event.MouseEvent evt) {
            showPopup(evt);
         }
         public void mouseReleased(java.awt.event.MouseEvent evt) {
            showPopup(evt);
         }
      });
      jScrollPane3.setViewportView(notesField);

      javax.swing.GroupLayout notesPanelLayout = new javax.swing.GroupLayout(notesPanel);
      notesPanel.setLayout(notesPanelLayout);
      notesPanelLayout.setHorizontalGroup(
         notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
      );
      notesPanelLayout.setVerticalGroup(
         notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
      );

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.weightx = 1.0;
      add(notesPanel, gridBagConstraints);

      procedurePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Procedure"));
      procedurePanel.setMinimumSize(new java.awt.Dimension(200, 125));
      procedurePanel.setPreferredSize(new java.awt.Dimension(400, 125));

      jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

      procedureField.setColumns(20);
      procedureField.setRows(5);
      procedureField.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mousePressed(java.awt.event.MouseEvent evt) {
            showPopup(evt);
         }
         public void mouseReleased(java.awt.event.MouseEvent evt) {
            showPopup(evt);
         }
      });
      jScrollPane1.setViewportView(procedureField);

      javax.swing.GroupLayout procedurePanelLayout = new javax.swing.GroupLayout(procedurePanel);
      procedurePanel.setLayout(procedurePanelLayout);
      procedurePanelLayout.setHorizontalGroup(
         procedurePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
      );
      procedurePanelLayout.setVerticalGroup(
         procedurePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
      );

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.weighty = 1.0;
      add(procedurePanel, gridBagConstraints);

      labelsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Labels"));
      labelsPanel.setToolTipText("Seperate labels with commas.");
      labelsPanel.setPreferredSize(new java.awt.Dimension(448, 55));

      labelField.setToolTipText("Seperate labels with commas.");

      javax.swing.GroupLayout labelsPanelLayout = new javax.swing.GroupLayout(labelsPanel);
      labelsPanel.setLayout(labelsPanelLayout);
      labelsPanelLayout.setHorizontalGroup(
         labelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(labelField, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
      );
      labelsPanelLayout.setVerticalGroup(
         labelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(labelField, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
      );

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 4;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      add(labelsPanel, gridBagConstraints);

      buttonsPanel.setMinimumSize(new java.awt.Dimension(200, 30));
      buttonsPanel.setPreferredSize(new java.awt.Dimension(200, 30));

      cancelButton.setPreferredSize(new java.awt.Dimension(90, 29));

      saveButton.setPreferredSize(new java.awt.Dimension(90, 29));

      javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
      buttonsPanel.setLayout(buttonsPanelLayout);
      buttonsPanelLayout.setHorizontalGroup(
         buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(buttonsPanelLayout.createSequentialGroup()
            .addGap(6, 6, 6)
            .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(308, 308, 308))
      );
      buttonsPanelLayout.setVerticalGroup(
         buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      );

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 5;
      add(buttonsPanel, gridBagConstraints);

      jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Ingredients"));
      jPanel1.setMinimumSize(new java.awt.Dimension(100, 150));
      jPanel1.setPreferredSize(new java.awt.Dimension(503, 150));

      jScrollPane2.setViewportView(iListTable1);

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 491, Short.MAX_VALUE)
         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE))
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 122, Short.MAX_VALUE)
         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
      );

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      add(jPanel1, gridBagConstraints);
   }

   private void showPopup(java.awt.event.MouseEvent evt) {
      if (evt.isPopupTrigger()) {
         popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
      }
   }


   private javax.swing.JPanel buttonsPanel;
   private javax.swing.JButton cancelButton;
   private recipejar.IListTable iListTable1;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JScrollPane jScrollPane3;
   private javax.swing.JTextField labelField;
   private javax.swing.JPanel labelsPanel;
   private javax.swing.JTextArea notesField;
   private javax.swing.JPanel notesPanel;
   private javax.swing.JTextArea procedureField;
   private javax.swing.JPanel procedurePanel;
   private javax.swing.JButton saveButton;
   private javax.swing.JTextField titleField;
   private javax.swing.JPanel titlePanel;
/*
   private void updateMetaData() {
   if (ApplicationScope.getInstance().getState().getValue("AUTHOR") != null) {
   String author = ApplicationScope.getInstance().getState().getValue("AUTHOR");
   recipejar.fileDefs.XHTML.Element e = open.getMetaElement("author");
   if (e == null) {
   open.setMetaData("author", author);
   } else if (!e.getAttribute("content").contains(author)) {
   open.setMetaData("author", open.getMetaData("author") + ", " + author);
   }
   }
   if (ApplicationScope.getInstance().getState().getValue("USERPHONE") != null) {
   String author = ApplicationScope.getInstance().getState().getValue("USERPHONE");
   recipejar.fileDefs.XHTML.Element e = open.getMetaElement("userphone");
   if (e == null) {
   open.setMetaData("userphone", author);
   } else if (!e.getAttribute("content").contains(author)) {
   open.setMetaData("userphone", open.getMetaData("userphone"));
   }
   }
   if (ApplicationScope.getInstance().getState().getValue("USEREMAIL") != null) {
   String author = ApplicationScope.getInstance().getState().getValue("USEREMAIL");
   recipejar.fileDefs.XHTML.Element e = open.getMetaElement("useremail");
   if (e == null) {
   open.setMetaData("useremail", author);
   } else if (!e.getAttribute("content").contains(author)) {
   open.setMetaData("useremail", open.getMetaData("useremail"));
   }
   }
   if (ApplicationScope.getInstance().getState().getValue("CUSTOM") != null) {
   String author = ApplicationScope.getInstance().getState().getValue("CUSTOM");
   recipejar.fileDefs.XHTML.Element e = open.getMetaElement("custom");
   if (e == null) {
   open.setMetaData("custom", author);
   } else if (!e.getAttribute("content").contains(author)) {
   open.setMetaData("custom", open.getMetaData("custom"));
   }
   }

   }
    */

   public JTextField getTitleField() {
      return titleField;
   }

   public IListTable getTable() {
      return this.iListTable1;
   }

   public void readMacrosFromFile(String s) throws FileNotFoundException{
      macroTextActionsFile = new AbstractCharDelineatedFile(s, ",", ';'){
         @Override
         public void save() {
            FileWriter out = null;
            try {
               out = new FileWriter(macroTextActionsFile);
               out.close();
            } catch (IOException ex) {
               //do nothing
            }
         }
      };

      macroActions = new ArrayList<MacroTextAction>();
      for(int i=0; i<macroTextActionsFile.getDataLength(); i++){
          MacroTextAction newMacro = new MacroTextAction(
                                                         macroTextActionsFile.getLine(i)[0],
                                                         macroTextActionsFile.getLine(i)[1].trim().charAt(0),
                                                         macroTextActionsFile.getLine(i)[2].trim().charAt(0),
                                                         macroTextActionsFile.getLine(i)[3],
                                                         macroTextActionsFile.getLine(i)[4]
                                                         );
         newMacro.addField(notesField);
         newMacro.addField(procedureField);
         macroActions.add(newMacro);
         textActionsMenu.add(newMacro);
         popupTextActionsMenu.add(newMacro);
      }

   }

   public JMenu getTextActionsMenu() {
      return textActionsMenu;
   }

   public RecipeFile getDiskFile() {
      return diskFile;
   }
}
