/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SearchDialog.java
 *
 * Created on Nov 5, 2009, 9:14:53 PM
 */
package recipejar;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import recipejar.filetypes.*;

/**
 *
 * @author james
 */
public class SearchDialog extends javax.swing.JDialog {


   ArrayList<recipejar.lib.Identifier> results;

   /** Creates new form SearchDialog
    * @param parent
    * @param modal
    */
   public SearchDialog(MainFrame parent, boolean modal) {
      super(parent);
      if(ProgramVariables.LAF.toString().equals(recipejar.lib.LAFType.METAL.toString())) this.setUndecorated(true);
      this.getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.PLAIN_DIALOG);
      initComponents();
      jTextField1.setText("");
      results = new ArrayList<recipejar.lib.Identifier>();
      jList1.setEnabled(false);
      jList1.setCellRenderer(new ListRenderer());

   }


   private Element getListElement(HTMLDocument doc, String address) {
      javax.swing.text.Element e = doc.getElement("letter" + address.trim().toUpperCase().charAt(0));
      if (e != null) {
         javax.swing.text.Element f = e.getElement(1);//Default list
         if (f != null && f.getName().toLowerCase().equals("ul")) {
            return f;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private Element getAnchorElement(Element e, String name){
      for (int i = 0; i < e.getElementCount(); i++) {
         javax.swing.text.Element f = e.getElement(i);
         if (f.getName().toLowerCase().equals("li")) {
            try {
               if (f.getDocument().getText(f.getStartOffset(), f.getEndOffset()-f.getStartOffset()).trim().toLowerCase().startsWith(name.trim().toLowerCase())) {
                  return f;
               }
            } catch (BadLocationException ex) {
            }
         }
      }
      return null;
   }

   private class ListRenderer extends DefaultListCellRenderer {

      @Override
      public Component getListCellRendererComponent(JList list,
              Object value,
              int index, boolean isSelected,
              boolean cellHasFocus) {

         super.getListCellRendererComponent(list,
                 value,
                 index,
                 isSelected,
                 cellHasFocus);
         if (((recipejar.lib.Identifier) value).category != null) {
            setForeground(inLabels.getForeground());
            setFont(inLabels.getFont());
         }
         return this;
      }
   }

    private void onSearch(java.awt.event.ActionEvent evt) {
       results = IndexFile.getIndexFile().search(
          jTextField1.getText(), 
          inTitle.isSelected(),
         // inLabels.isSelected(), 
          inNotes.isSelected(),
          inIngredients.isSelected(), 
          inProcedure.isSelected()
       );
       Collections.sort((List) results);
       DefaultListModel listModel = new DefaultListModel();
       for (int i = 0; i < results.size(); i++) {
          listModel.addElement(results.get(i));
       }
       jList1.setModel(listModel);
       jList1.setEnabled(true);
    }

   /** This method is called from within the constructor to
    * initialize the form.
    */
   @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        inNotes = new javax.swing.JCheckBox();
        inIngredients = new javax.swing.JCheckBox();
        inAll = new javax.swing.JCheckBox();
        inTitle = new javax.swing.JCheckBox();
        inLabels = new javax.swing.JCheckBox();
        inProcedure = new javax.swing.JCheckBox();
        jPanel15 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        setTitle("Find");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jTextField1.setText("jTextField1");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onSearch(evt);
            }
        });
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                shortcutHandler(evt);
            }
        });

        jButton1.setText("Go");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onSearch(evt);
            }
        });
        jButton1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                shortcutHandler(evt);
            }
        });

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Look In:"));

        inNotes.setText("Notes");
        inNotes.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
              //onSearchSettingsChange(evt);
           }
        });

        inIngredients.setText("Ingredients");
        inIngredients.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
              //onSearchSettingsChange(evt);
           }
        });

        inAll.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        inAll.setText("All Fields");
        inAll.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
              //onSearchSettingsChange(evt);
           }
        });

        inTitle.setSelected(true);
        inTitle.setText("Titles");
        inTitle.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
              //onSearchSettingsChange(evt);
           }
        });

        inLabels.setFont(inLabels.getFont());
        inLabels.setForeground(new java.awt.Color(255, 153, 153));
        inLabels.setSelected(true);
        inLabels.setText("Labels");
        inLabels.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
              //onSearchSettingsChange(evt);
           }
        });

        inProcedure.setText("Procedures");
        inProcedure.addActionListener(new java.awt.event.ActionListener() {
           public void actionPerformed(java.awt.event.ActionEvent evt) {
              //onSearchSettingsChange(evt);
           }
        });

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
           jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
           .addGroup(jPanel15Layout.createSequentialGroup()
              .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel15Layout.createSequentialGroup()
                    .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                       .addComponent(inTitle)
                       .addComponent(inNotes))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                       .addComponent(inIngredients)
                       .addComponent(inLabels)))
                 .addComponent(inProcedure)
                 .addComponent(inAll)))
        );
        jPanel15Layout.setVerticalGroup(
           jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
           .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
              .addComponent(inAll)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
              .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(inTitle)
                 .addComponent(inLabels))
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
              .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(inNotes)
                 .addComponent(inIngredients))
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(inProcedure)
              .addContainerGap())
        );

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                onClicked(evt);
            }
        });
        jList1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                shortcutHandler(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                ).addContainerGap()
            ).addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addGap(3, 3, 3)

        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(
                   jPanel1Layout.createParallelGroup(
                      javax.swing.GroupLayout.Alignment.BASELINE
                   ).addComponent(
                      jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE
                   ).addComponent(jButton1)
                ).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                //.addGroup(
                   //jPanel1Layout.createSequentialGroup()
                     .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGap(3, 3, 3)
               // )
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void onClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onClicked
       if (evt.getClickCount() > 0 && evt.getClickCount() < 3) {
          //Select correct Tab, select item, and if it is a recipe,
          //open it.
          int selection = jList1.getSelectedIndex();
          if (selection != -1) {
             Kernel.topLevelFrame.tabbedPane.setSelectedTab(results.get(selection).getText().charAt(0));
             //JTextPane p = index.getSelectedTextPane();
             //HTMLDocument doc = (HTMLDocument) p.getDocument();
             //Most of the following is just to get it to scroll the the right place.
             if (results.get(selection).category != null) 
             {
                //javax.swing.text.Element e = doc.getElement(StringProcessor.underscoreSpaces(results.get(selection).name));
                //if (e != null) {
                //   p.setCaretPosition(e.getStartOffset());
                //   try {
                //      Rectangle r1 = p.modelToView(e.getStartOffset());
                //      Rectangle whatIWant = new Rectangle(r1.x, r1.y, r1.width, p.getVisibleRect().height);
                //      p.scrollRectToVisible(whatIWant);
                //   } catch (BadLocationException ex) {
                //   }
                //}
             } else {
                //javax.swing.text.Element e = getListElement(doc, results.get(selection).address);
                //try {
                //   if (e != null) {
                //      javax.swing.text.Element f = getAnchorElement(e, results.get(selection).name);
                //      if (f != null) {
                //         p.setCaretPosition(f.getStartOffset());
                //         Rectangle r1 = p.modelToView(f.getStartOffset());
                //         Rectangle whatIWant = new Rectangle(r1.x, r1.y, r1.width, p.getVisibleRect().height);
                //         p.scrollRectToVisible(whatIWant);
                //      }
                //   }
                //} catch (BadLocationException badLocationException) {
                //}
                //opener.open(results.get(selection).address);
             }
          }

          if (evt.getClickCount() == 2) {//close dialog
             this.setVisible(false);
             this.dispose();
          }
       }
}

    private void shortcutHandler(java.awt.event.KeyEvent evt) {
       //if (recipejar.Util.isOS("mac")) {
       //   if (evt.isMetaDown()) {
       //      if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_W) {
       //         this.setVisible(false);
       //      }
       //   }
       //}
    }
   private void onSearchSettingsChange(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onSearchSettingsChange
      if (evt.getSource().equals(inAll)) {
         if (inAll.isSelected()) {
            inIngredients.setSelected(true);
            inLabels.setSelected(true);
            inNotes.setSelected(true);
            inProcedure.setSelected(true);
            inTitle.setSelected(true);
         }else if (inIngredients.isSelected() && inLabels.isSelected() && inNotes.isSelected()
                 && inProcedure.isSelected() && inTitle.isSelected()) {
            inAll.setSelected(true);
         }
      } else {
         if (inIngredients.isSelected() && inLabels.isSelected() && inNotes.isSelected()
                 && inProcedure.isSelected() && inTitle.isSelected()) {
            inAll.setSelected(true);
         } else {
            inAll.setSelected(false);
         }
      }
   }//GEN-LAST:event_onSearchSettingsChange


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JCheckBox inAll;
    private javax.swing.JCheckBox inIngredients;
    private javax.swing.JCheckBox inLabels;
    private javax.swing.JCheckBox inNotes;
    private javax.swing.JCheckBox inProcedure;
    private javax.swing.JCheckBox inTitle;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
