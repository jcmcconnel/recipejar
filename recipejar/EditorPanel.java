/*
 * EditorPanel.java
 * @author James McConnel
 * Created on September 18, 2008, 9:19 AM
 * @version %I%, %G%
 */
package recipejar;
 
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import recipejar.recipe.Recipe;
import recipejar.IngredientTableModel;
//
////TODO: Remove these imports and have something else listen on the panel.
//import recipejar.manager.actions.ApplicationEvent;
//import recipejar.manager.action.ApplicationEventSource;
//import recipejar.manager.action.Event_Type;

/**
 *
 * @author  James McConnel
 */
public class EditorPanel extends JPanel {

   /************Instance Variables*****************/
    // private ApplicationEventSource aes = new ApplicationEventSource();

    // public ApplicationEventSource getApplicationEventSource() {
    //    return aes;
    // }

   private IngredientTableModel table = null;

   /**
    * Creates new EditorPanel
    * 
    */
   public EditorPanel() {
      initComponents();
      jScrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
   }


   /**
    *
    *
    */
   public void clear() {
      //stopListening();
      titleField.setText("Open a Recipe, or click \"New\".");
      titleField.setEditable(false);
      notesField.setText("");
      notesField.setEditable(false);
      iListTable1.setEnabled(false);
      procedureField.setText("");
      procedureField.setEditable(false);
      saveButton.setEnabled(false);

      //startListening();
   }

    /**
     * Returns this file's ingredient table model
     *
     * @return
     */
    public IngredientTableModel getIngredientTableModel(Recipe r) {
        if (table == null) {
            return table = new IngredientTableModel(r);
        } else {
            return table;
        }
    }


   /**
    *
    * @return
    * @throws java.io.FileNotFoundException
    * @throws java.io.IOException
    *
   public boolean save() throws FileNotFoundException, IOException {
   if (isTitleChanged()) { //Saving a new recipe.

   if (rjUtil.isBadTitle(titleField.getText())) {
   JOptionPane.showMessageDialog((JPanel) this, "Please enter a valid title for your recipe.");
   return false;
   } else {
   ApplicationScope.getInstance().setCurrentlyOpenRecipe(
   new RecipeFile(rjUtil.buildAbsoluteFileNameFrom(titleField.getText())));
   open.setTitle(titleField.getText().trim());
   }
   }
   open.setLabels(this.labelField.getText());
   open.setNotes(rjUtil.convertToXMLLineBreaks(rjUtil.fixInformalAnchors(notesField.getText())));

   open.setProcedure(rjUtil.convertToXMLLineBreaks(rjUtil.fixInformalAnchors(procedureField.getText())));

   updateMetaData();

   open.save();
   recipeChanged = false;
   this.fireEditorChangedUpdate(EditEvent.SAVE);
   return true;
   }

   /**
    *
    * @return
    *
   public boolean startNew() {
      if (!isRecipeChanged() || (isRecipeChanged() && JOptionPane.showConfirmDialog(this.getParent(),
              "You have unsaved changes.\n"
              + "Are you sure you want to leave this recipe?\n"
              + "Any unsaved changes will be discarded.") == JOptionPane.YES_OPTION)) {
         stopListening();
         // open = null;
         titleField.setText("");
         titleField.setEditable(true);
         notesField.setText(rjUtil.convertToASCIILinebreaks(RecipeFile.getTemplate().getNotes()));
         notesField.setEditable(true);
         procedureField.setText(rjUtil.convertToASCIILinebreaks(RecipeFile.getTemplate().getProcedure()));
         procedureField.setEditable(true);
         this.labelField.setText("");
         saveButton.setEnabled(true);

         this.fireEditorChangedUpdate(EditEvent.NEW);
         startListening();
         return true;
      } else {
         return false;
      }
   }

   /***************Getters and Setters**********************/

   /**
    *
    * @return
    *
   public boolean setOpen(RecipeFile recipeFile) {
      if (recipeFile == null) {
         return startNew();
      }
      if (!recipeChanged || //Recipe was not edited, so no reason to ask to save.
              (recipeChanged && JOptionPane.showConfirmDialog(this.getParent(),//Does user want to save?
              "You have unsaved changes.\nIf you choose \"Yes\" they will be discarded.\n"
              + "Are you sure you want to leave this recipe?", "Are You Sure?",
              JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)) {
         stopListening();
         //open = recipeFile;
         //titleField.setText(getOpen().getTitle());
         titleField.setEditable(true);
         //labelField.setText(open.getMetaData("labels"));
         //notesField.setText(rjUtil.convertToASCIILinebreaks(getOpen().getNotes()));
         notesField.setEditable(true);
         //procedureField.setText(rjUtil.convertToASCIILinebreaks(getOpen().getProcedure()));
         procedureField.setEditable(true);
         saveButton.setEnabled(true);
         this.fireEditorChangedUpdate(EditEvent.OPEN);
         recipeChanged = false;
         startListening();
         return true;
      } else {
         return false;
      }
   }*/

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
   public JButton getCancelButton() {
      return this.cancelButton;
   }

   public JButton getSaveButton() {
      return saveButton;
   }

   public void setNotesPopup(JPopupMenu notesPopup) {
   }

   public void setProcedurePopup(JPopupMenu procedurePopup) {
   }

   /****************Other********************

   public boolean isTitleChanged() {
      if (true) {//open == null) {
         if (titleField.getText().isEmpty()) {
            return false;
         } else {
            return true;
         }
      } else {
         return true; //!open.getTitle().equals(titleField.getText());
      }
   }

   public boolean isRecipeChanged() {
      return recipeChanged;
   }

   /*******************non public******************/
   
  // /**
  //  * Adds this as a document listener on all the fields.
  //  */
  // private void startListening() {
  //    this.titleField.getDocument().addDocumentListener(this);
  //    this.notesField.getDocument().addDocumentListener(this);
  //    this.iListTable1.getModel().addTableModelListener(this);
  //    this.procedureField.getDocument().addDocumentListener(this);
  //    this.labelField.getDocument().addDocumentListener(this);
  // }

  // /**
  //  * Removes this as a DocumentListener on all the fields.
  //  * This is so that DocumentEvents fired not as a result of
  //  * user input are ignored.
  //  */
  // private void stopListening() {
  //    this.titleField.getDocument().removeDocumentListener(this);
  //    this.notesField.getDocument().removeDocumentListener(this);
  //    //TODO this.ingredientList1.getTable().getModel().removeTableModelListener(this);
  //    this.procedureField.getDocument().removeDocumentListener(this);
  //    this.labelField.getDocument().removeDocumentListener(this);
  // }

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

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

      cancelButton.setText("Cancel");
      cancelButton.setPreferredSize(new java.awt.Dimension(90, 29));
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            fireCancelEvent(evt);
         }
      });

      saveButton.setText("Save");
      saveButton.setPreferredSize(new java.awt.Dimension(90, 29));
      saveButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            fireSaveEvent(evt);
         }
      });

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
   }// </editor-fold>//GEN-END:initComponents

private void showPopup(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showPopup
    //if (evt.isPopupTrigger()) {
    //   aes.fireApplicationEvent(new ApplicationEvent(Event_Type.POPUP, evt));
    //}
}//GEN-LAST:event_showPopup

private void fireCancelEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireCancelEvent
    //aes.fireApplicationEvent(new ApplicationEvent(Event_Type.EDIT_CANCEL, evt));
}//GEN-LAST:event_fireCancelEvent

private void fireSaveEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireSaveEvent
    //aes.fireApplicationEvent(new ApplicationEvent(Event_Type.SAVE, evt));
}//GEN-LAST:event_fireSaveEvent

   // Variables declaration - do not modify//GEN-BEGIN:variables
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
   // End of variables declaration//GEN-END:variables
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

}
