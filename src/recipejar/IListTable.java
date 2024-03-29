/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 
/*
 * IListTable.java
 *
 * Created on Apr 11, 2011, 9:44:44 AM
 */
package recipejar;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumn;
import recipejar.recipe.IngredientTableModel;

/**
 *
 * @author james
 */
public class IListTable extends JTable {

   private final TextCellEditor textEditor;
   private final UnitCellEditor unitEditor;
   private IngredientTableModel tmodel;
   private javax.swing.table.DefaultTableCellRenderer coloredRenderer;

   /** Creates new form IListTable */
   public IListTable() {
      initComponents();
      FocusListener cellEditorFocusEventHandler = new FocusListener() {

          @Override
         public void focusGained(FocusEvent e) {
            //Resizes row for comfortable editing.
            setRowHeight(getSelectedRow(), getFont().getSize() + 12);
         }

          @Override
         public void focusLost(FocusEvent e) {
            System.out.println("cell editor focuslost");
            if (!e.isTemporary()) {
               if (isEditing()) {
                  int r = getEditingRow();
                  int c = getEditingColumn();
                  getCellEditor(r, c).stopCellEditing();
                  revalidate();
               }
               setRowHeight(getFont().getSize() + 4);
            }
         }
      };
      this.addFocusListener(new FocusListener() {
          @Override
         public void focusGained(FocusEvent e) {
            System.out.println("table focus gained");
         }

          @Override
         public void focusLost(FocusEvent e) {
            System.out.println("table focus lost");
         }
      
      });
      textEditor = new TextCellEditor();
      textEditor.addFocusListener(cellEditorFocusEventHandler);//TODO Make Listener class(es)
      unitEditor = new UnitCellEditor();
      unitEditor.addFocusListener(cellEditorFocusEventHandler);


      if (!recipejar.Kernel.isOS("mac")) {
         this.setShowGrid(true);
         this.setGridColor(Color.BLACK);
      }
   }

   /**
    * Makes the table appear os appropriate by coloring every other row
    * on os x.
    * @param row
    * @param column
    * @return
    */
   @Override
   public javax.swing.table.TableCellRenderer getCellRenderer(int row, int column) {
      if (recipejar.Kernel.isOS("mac")) {
         if (coloredRenderer == null) {
            coloredRenderer = new javax.swing.table.DefaultTableCellRenderer();
            coloredRenderer.setBackground(new Color(241, 245, 250));
         }
         if (row % 2 == 0) {
            return coloredRenderer;
         } else {
            return super.getCellRenderer(row, column);
         }
      } else {
         return super.getCellRenderer(row, column);
      }
   }

   public TextCellEditor getTextEditor(){
      return textEditor;
   }

   /**
    */
   @Override
   public void setModel(TableModel dataModel){
      super.setModel(dataModel);
      if(unitEditor != null && textEditor != null) {
         TableColumn qtyCol = getColumnModel().getColumn(0);
         qtyCol.setPreferredWidth(30);
         qtyCol.setCellEditor(new DefaultCellEditor(textEditor));

         TableColumn unitCol = getColumnModel().getColumn(1);
         unitCol.setPreferredWidth(45);
         unitCol.setCellEditor(new DefaultCellEditor(unitEditor));

         TableColumn nameCol = getColumnModel().getColumn(2);
         nameCol.setPreferredWidth(260);
         nameCol.setCellEditor(new DefaultCellEditor(textEditor));
      }
   }
   /** 
    * 
    *
    *
    */
   @SuppressWarnings("unchecked")
    private void initComponents() {

        setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"", "", null}
            },
            new String [] {
                "Qty", "Unit", "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        setCellSelectionEnabled(true);
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        setSurrendersFocusOnKeystroke(true);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                onMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                onMouseReleased(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                keyStrokeFix(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                shortcutHandler(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

   private void keyStrokeFix(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keyStrokeFix
      /*If a cell is selected, but the editor is not initiated i.e. double clicked,
      the table doesn't pass the first character typed to the editor.
      This fixes the problem.*/
      if (getSelectedColumn() == 0) {
         textEditor.putToBuffer(evt.getKeyChar());
      } else if (getSelectedColumn() == 1) {
         unitEditor.takeKeyStroke(evt.getKeyChar());
      } else {
         textEditor.putToBuffer(evt.getKeyChar());
      }
   }//GEN-LAST:event_keyStrokeFix

   private void shortcutHandler(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_shortcutHandler
      /*The following code enables shortcuts keys for the table.*/
      if (evt.isShiftDown()) {
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
            //SHIFT-DOWN: Move row down.
            int sr = getSelectedRow();
            int sc = getSelectedColumn();
            if (tmodel.moveRowDown(sr)) {
               changeSelection(sr, sc, false, false);
            }

         } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
            //SHIFT-UP: move row up
            int sr = getSelectedRow();
            int sc = getSelectedColumn();
            if (tmodel.moveRowUp(sr)) {
               changeSelection(sr, sc, false, false);
            }

         } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE
                 || evt.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            //SHIFT-DELETE/BACKSPACE: Clear Row.
            if (evt.isAltDown()) {
               //     ALT-SHIFT-DELETE/BACKSPACE: Clear table.
               for (int i = 0; i < this.getRowCount(); i++) {
                  this.setValueAt("", i, 0);
                  this.setValueAt("", i, 1);
                  this.setValueAt("", i, 2);
               }
            } else {
               int sr = getSelectedRow();
               this.setValueAt("", sr, 0);
               this.setValueAt("", sr, 1);
               this.setValueAt("", sr, 2);
            }
         }
      }
   }

   private void onMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onMousePressed
      int c = getSelectedColumn();
      int r = getSelectedRow();
      if (c == 1) {//the Unit column
          System.out.println(getValueAt(r, c).toString().equals(unitEditor.getSelectedItem().toString()));
          if (!getValueAt(r, c).toString().equals(unitEditor.getSelectedItem().toString())) {
             int i = recipejar.recipe.Unit.indexOf(getValueAt(r, c).toString());
                   unitEditor.setSelectedIndex(i);
          }
      }
      if (evt.isPopupTrigger()) {
         if ((c == 0 && r < getRowCount() - 1) &&
                 (columnAtPoint(new Point(evt.getX(), evt.getY())) == c &&
                 rowAtPoint(new Point(evt.getX(), evt.getY())) == r)) {
            JPopupMenu menu = new JPopupMenu("Convert to...");
            menu.add(tmodel.getConvertMenu(r));
            menu.show(evt.getComponent(), evt.getX(), evt.getY());
            System.gc();
         }

      }
   }

   private void onMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onMouseReleased
      int c = getSelectedColumn();
      int r = getSelectedRow();
      if (c == 1) {
         if (!unitEditor.isPopupVisible()) {
             editCellAt(r, c);
             unitEditor.showPopup();
         }
      }
   }
}
