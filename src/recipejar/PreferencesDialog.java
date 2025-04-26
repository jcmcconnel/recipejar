/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PreferencesDialog.java
 *
 * Created on Apr 5, 2009, 6:35:24 PM
 */
package recipejar;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Dimension;
import recipejar.lib.LAFType;
import recipejar.FilePrefPanel;

/**
 *
 * @author james
 */
public class PreferencesDialog extends javax.swing.JDialog {

   /** Creates new form PreferencesDialog
    * @param parent
    * @param modal
    */
   public PreferencesDialog(java.awt.Frame parent, boolean modal) {
      super(parent, modal);
      if(ProgramVariables.LAF.toString().equals(recipejar.lib.LAFType.METAL.toString())) this.setUndecorated(true);
      this.getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.PLAIN_DIALOG);
      initComponents();
      try {
         File macroFile = new File(ProgramVariables.FILE_MACRO.toString());
         File unitFile = new File(ProgramVariables.FILE_UNIT.toString());
         FileReader in = new FileReader(macroFile);
         loadField(this.MacroTextArea, in);
         in = new FileReader(unitFile);
         loadField(this.UnitsTextArea, in);
         String author = ProgramVariables.USER_NAME.toString();
         if (author != null) {
            authorField.setText(author);
         } else {
            authorField.setText(author);
         }
          
      } catch (java.io.IOException ex) {
      }

   }

   /** This method is called from within the constructor to
    * initialize the form.
    */
   private void initComponents() {

      buttonGroup1 = new javax.swing.ButtonGroup();
      jTabbedPane1 = new javax.swing.JTabbedPane();
      jPanel1 = new javax.swing.JPanel();
      jPanel2 = new javax.swing.JPanel();
      jRadioButton1 = new javax.swing.JRadioButton();
      jRadioButton2 = new javax.swing.JRadioButton();
      jRadioButton3 = new javax.swing.JRadioButton();
      jRadioButton4 = new javax.swing.JRadioButton();
      jRadioButton5 = new javax.swing.JRadioButton();
      jPanel11 = new javax.swing.JPanel();
      filePrefPanel1 = new FilePrefPanel();
      jPanel7 = new javax.swing.JPanel();
      filePrefPanel3 = new FilePrefPanel();
      jPanel13 = new javax.swing.JPanel();
      jPanel14 = new javax.swing.JPanel();
      authorField = new javax.swing.JTextField();
      jPanel16 = new javax.swing.JPanel();
      phoneField = new javax.swing.JTextField();
      jPanel17 = new javax.swing.JPanel();
      emailField = new javax.swing.JTextField();
      jPanel18 = new javax.swing.JPanel();
      customField = new javax.swing.JTextField();
      jPanel3 = new javax.swing.JPanel();
      jPanel10 = new javax.swing.JPanel();
      filePrefPanel7 = new FilePrefPanel();
      jScrollPane1 = new javax.swing.JScrollPane();
      MacroTextArea = new javax.swing.JTextArea();
      jPanel4 = new javax.swing.JPanel();
      jPanel12 = new javax.swing.JPanel();
      filePrefPanel8 = new FilePrefPanel();
      jScrollPane2 = new javax.swing.JScrollPane();
      UnitsTextArea = new javax.swing.JTextArea();
      jPanel20 = new javax.swing.JPanel();
      jPanel8 = new javax.swing.JPanel();
      filePrefPanel4 = new FilePrefPanel();
      jPanel9 = new javax.swing.JPanel();
      filePrefPanel5 = new FilePrefPanel();
      jPanel5 = new javax.swing.JPanel();
      filePrefPanel6 = new FilePrefPanel();
      jPanel6 = new javax.swing.JPanel();
      filePrefPanel2 = new FilePrefPanel();
      fileChooser = new javax.swing.JFileChooser();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setModal(true);
      //setPreferredSize(new java.awt.Dimension(360, 350));
      setResizable(false);

      jTabbedPane1.setName("Preferences"); // NOI18N
      jTabbedPane1.setOpaque(true);
      //jTabbedPane1.setPreferredSize(new java.awt.Dimension(360, 285));
      jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            resizeDialog(evt);
         }
      });

      jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Look and Feel"));

      buttonGroup1.add(jRadioButton1);
      jRadioButton1.setText("System");
      if(ProgramVariables.LAF.toString().equals(LAFType.SYSTEM.toString())) jRadioButton1.setSelected(true);
      jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            LAFChange(evt);
         }
      });

      buttonGroup1.add(jRadioButton2);
      jRadioButton2.setText("Metal");
      if(ProgramVariables.LAF.toString().equals(LAFType.METAL.toString())) jRadioButton2.setSelected(true);
      jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            LAFChange(evt);
         }
      });

      buttonGroup1.add(jRadioButton3);
      jRadioButton3.setText("Motif");
      if(ProgramVariables.LAF.toString().equals(LAFType.MOTIF.toString())) jRadioButton3.setSelected(true);
      jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            LAFChange(evt);
         }
      });

      buttonGroup1.add(jRadioButton4);
      jRadioButton4.setText("Nimbus");
      if(ProgramVariables.LAF.toString().equals(LAFType.NIMBUS.toString())) jRadioButton4.setSelected(true);
      jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            LAFChange(evt);
         }
      });

      buttonGroup1.add(jRadioButton5);
      jRadioButton4.setText("GTK");
      if(ProgramVariables.LAF.toString().equals(LAFType.GTK.toString())) jRadioButton5.setSelected(true);
      jRadioButton5.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            LAFChange(evt);
         }
      });

      javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jRadioButton1)
               .addComponent(jRadioButton2)
               .addComponent(jRadioButton3)
               .addComponent(jRadioButton4))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            //.addContainerGap()
            .addComponent(jRadioButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jRadioButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jRadioButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jRadioButton4)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            //.addContainerGap())
      );

      jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Program Directory"));

      javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
      jPanel11.setLayout(jPanel11Layout);
      jPanel11Layout.setHorizontalGroup(
         jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
      );
      jPanel11Layout.setVerticalGroup(
         jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      filePrefPanel1.setBoundPref(ProgramVariables.DIR_PROGRAM);
      filePrefPanel1.setFileChooser(fileChooser);

      jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Welcome Message"));

      javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
      jPanel7.setLayout(jPanel7Layout);
      jPanel7Layout.setHorizontalGroup(
         jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
      );
      jPanel7Layout.setVerticalGroup(
         jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      filePrefPanel3.setBoundPref(ProgramVariables.FILE_WELCOME);
      filePrefPanel3.setFileChooser(fileChooser);

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(jPanel1Layout.createSequentialGroup()
                  .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
               .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            )
            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(14, 14, 14))
      );

      jTabbedPane1.addTab("Settings", jPanel1);

      jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("Author"));

      authorField.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            onNameChange(evt);
         }
      });

      javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
      jPanel14.setLayout(jPanel14Layout);
      jPanel14Layout.setHorizontalGroup(
         jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(authorField, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
      );
      jPanel14Layout.setVerticalGroup(
         jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(authorField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder("Phone"));

      phoneField.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            onNewNumber(evt);
         }
      });

      javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
      jPanel16.setLayout(jPanel16Layout);
      jPanel16Layout.setHorizontalGroup(
         jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(phoneField, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
      );
      jPanel16Layout.setVerticalGroup(
         jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(phoneField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder("Email"));

      emailField.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            onNewEmail(evt);
         }
      });

      javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
      jPanel17.setLayout(jPanel17Layout);
      jPanel17Layout.setHorizontalGroup(
         jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(emailField, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
      );
      jPanel17Layout.setVerticalGroup(
         jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder("Custom"));

      customField.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            onNewCustom(evt);
         }
      });

      javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
      jPanel18.setLayout(jPanel18Layout);
      jPanel18Layout.setHorizontalGroup(
         jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(customField, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
      );
      jPanel18Layout.setVerticalGroup(
         jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(customField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
      jPanel13.setLayout(jPanel13Layout);
      jPanel13Layout.setHorizontalGroup(
         jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      );
      jPanel13Layout.setVerticalGroup(
         jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel13Layout.createSequentialGroup()
            .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(55, Short.MAX_VALUE))
      );

      jTabbedPane1.addTab("User Information", jPanel13);

      jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Macro File"));

      javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
      jPanel10.setLayout(jPanel10Layout);
      jPanel10Layout.setHorizontalGroup(
         jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      );
      jPanel10Layout.setVerticalGroup(
         jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      filePrefPanel7.setBoundPref(ProgramVariables.FILE_MACRO);
      filePrefPanel7.setFileChooser(fileChooser);

      MacroTextArea.setColumns(20);
      MacroTextArea.setRows(5);
      MacroTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            handleMacroChanges(evt);
         }
      });
      jScrollPane1.setViewportView(MacroTextArea);

      javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
      jPanel3.setLayout(jPanel3Layout);
      jPanel3Layout.setHorizontalGroup(
         jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      );
      jPanel3Layout.setVerticalGroup(
         jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel3Layout.createSequentialGroup()
            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      jTabbedPane1.addTab("Macros", jPanel3);

      jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Unit File"));

      javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
      jPanel12.setLayout(jPanel12Layout);
      jPanel12Layout.setHorizontalGroup(
         jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      );
      jPanel12Layout.setVerticalGroup(
         jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      filePrefPanel8.setBoundPref(ProgramVariables.FILE_UNIT);
      filePrefPanel8.setFileChooser(fileChooser);

      UnitsTextArea.setColumns(20);
      UnitsTextArea.setRows(5);
      UnitsTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            handleUnitsChange(evt);
         }
      });
      jScrollPane2.setViewportView(UnitsTextArea);

      javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
      jPanel4.setLayout(jPanel4Layout);
      jPanel4Layout.setHorizontalGroup(
         jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      );
      jPanel4Layout.setVerticalGroup(
         jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel4Layout.createSequentialGroup()
            .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      jTabbedPane1.addTab("Units", jPanel4);

      jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Recipe Style Sheet"));

      javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
      jPanel8.setLayout(jPanel8Layout);
      jPanel8Layout.setHorizontalGroup(
         jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
      );
      jPanel8Layout.setVerticalGroup(
         jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      filePrefPanel4.setBoundPref(ProgramVariables.CSS_RECIPE);
      filePrefPanel4.setFileChooser(fileChooser);

      jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Index Style Sheet"));

      javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
      jPanel9.setLayout(jPanel9Layout);
      jPanel9Layout.setHorizontalGroup(
         jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
      );
      jPanel9Layout.setVerticalGroup(
         jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      filePrefPanel5.setBoundPref(ProgramVariables.CSS_INDEX);
      filePrefPanel5.setFileChooser(fileChooser);

      jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Index Template"));

      javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
      jPanel5.setLayout(jPanel5Layout);
      jPanel5Layout.setHorizontalGroup(
         jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
      );
      jPanel5Layout.setVerticalGroup(
         jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      filePrefPanel6.setBoundPref(ProgramVariables.TEMPLATE_INDEX);
      filePrefPanel6.setFileChooser(fileChooser);

      jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Recipe Template"));

      javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
      jPanel6.setLayout(jPanel6Layout);
      jPanel6Layout.setHorizontalGroup(
         jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
      );
      jPanel6Layout.setVerticalGroup(
         jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(filePrefPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );

      filePrefPanel2.setBoundPref(ProgramVariables.TEMPLATE_RECIPE);
      filePrefPanel2.setFileChooser(fileChooser);

      javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
      jPanel20.setLayout(jPanel20Layout);
      jPanel20Layout.setHorizontalGroup(
         jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      );
      jPanel20Layout.setVerticalGroup(
         jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel20Layout.createSequentialGroup()
            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(51, 51, 51))
      );

      jTabbedPane1.addTab("Files", jPanel20);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
      );

      pack();
   }

   /**
    * Changes to the "Settings" Tab
    * @param evt
    */   /**
    * This is called when the user requests that a different file be used
    * to define the macros, by changing the toString in the Macro File Text field
    * 
    * @param evt
    */
   /**
    * This is called when the user navigates away from the text area listing
    * the contents of the currently active macro file.  This action may indicate
    * that the user has changed the contents.  This will save the file.
    * @param evt
    */
   private void handleMacroChanges(java.awt.event.FocusEvent evt) {
      saveFile(ProgramVariables.FILE_MACRO, MacroTextArea);
   }

   private void handleUnitsChange(java.awt.event.FocusEvent evt) {
      saveFile(ProgramVariables.FILE_UNIT, UnitsTextArea);
   }

   private void LAFChange(java.awt.event.ActionEvent evt) {
      ProgramVariables.LAF.set(LAFType.valueOf(evt.getActionCommand().trim().toUpperCase()));
   }

   private void resizeDialog(javax.swing.event.ChangeEvent evt) {
      this.setMinimumSize(new Dimension(421, 422));
      switch (((javax.swing.JTabbedPane) evt.getSource()).getSelectedIndex()) {
         case 0: //settings
         case 1: //user
            //this.setMinimumSize(new Dimension(421, 422));
            //jTabbedPane1.setSize(new Dimension(345, 325));
            //jPanel1.setSize(new Dimension(340, 280));
            break;
         case 2: //Macros
         case 3: //Units
            //this.setMinimumSize(new Dimension(700, 700));
            //jTabbedPane1.setSize(new Dimension(700, 700));
            //jPanel4.setSize(new Dimension(500, 350));
            break;
         case 4: //files
            //this.setMinimumSize(new Dimension(421, 422));
            //jTabbedPane1.setSize(new Dimension(345, 325));
            //jPanel1.setSize(new Dimension(340, 280));
            break;
         default:
      }
      this.pack();
   }

   private void onNameChange(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_onNameChange
      ProgramVariables.USER_NAME.set(authorField.getText());
   }//GEN-LAST:event_onNameChange

   private void onNewNumber(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_onNewNumber
      ProgramVariables.USER_PHONE.set(this.phoneField.getText());
   }//GEN-LAST:event_onNewNumber

   private void onNewEmail(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_onNewEmail
      ProgramVariables.USER_EMAIL.set(emailField.getText());
   }//GEN-LAST:event_onNewEmail

   private void onNewCustom(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_onNewCustom
      ProgramVariables.USER_CUSTOM.set(customField.getText());
   }//GEN-LAST:event_onNewCustom

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JTextArea MacroTextArea;
   private javax.swing.JTextArea UnitsTextArea;
   private javax.swing.JTextField authorField;
   private javax.swing.ButtonGroup buttonGroup1;
   private javax.swing.JTextField customField;
   private javax.swing.JTextField emailField;
   private FilePrefPanel filePrefPanel1;
   private FilePrefPanel filePrefPanel2;
   private FilePrefPanel filePrefPanel3;
   private FilePrefPanel filePrefPanel4;
   private FilePrefPanel filePrefPanel5;
   private FilePrefPanel filePrefPanel6;
   private FilePrefPanel filePrefPanel7;
   private FilePrefPanel filePrefPanel8;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel10;
   private javax.swing.JPanel jPanel11;
   private javax.swing.JPanel jPanel12;
   private javax.swing.JPanel jPanel13;
   private javax.swing.JPanel jPanel14;
   private javax.swing.JPanel jPanel16;
   private javax.swing.JPanel jPanel17;
   private javax.swing.JPanel jPanel18;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JPanel jPanel20;
   private javax.swing.JPanel jPanel3;
   private javax.swing.JPanel jPanel4;
   private javax.swing.JPanel jPanel5;
   private javax.swing.JPanel jPanel6;
   private javax.swing.JPanel jPanel7;
   private javax.swing.JPanel jPanel8;
   private javax.swing.JPanel jPanel9;
   private javax.swing.JRadioButton jRadioButton1;
   private javax.swing.JRadioButton jRadioButton2;
   private javax.swing.JRadioButton jRadioButton3;
   private javax.swing.JRadioButton jRadioButton4;
   private javax.swing.JRadioButton jRadioButton5;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JTabbedPane jTabbedPane1;
   private javax.swing.JTextField phoneField;
   private javax.swing.JFileChooser fileChooser;

   private void loadField(javax.swing.JTextArea field, FileReader in) throws IOException {
      String fileText = new String();
      int c = in.read();
      while (c != -1) {
         fileText = fileText + (char) c;
         c = in.read();
      }
      field.setText(fileText);
      field.setCaretPosition(0);
   }

   private void saveFile(ProgramVariables prefField, javax.swing.JTextArea textArea) {
      try {
         FileWriter out = new FileWriter(new File(prefField.toString()));
         out.write(textArea.getText());
         out.close();
      } catch (IOException ex) {
         Logger.getLogger(PreferencesDialog.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
}
