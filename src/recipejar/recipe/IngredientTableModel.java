/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recipejar.recipe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.table.AbstractTableModel;
import recipejar.recipe.Recipe;
import recipejar.recipe.Ingredient;
import recipejar.recipe.Unit;

/**
 *
 * @author James McConnel
 */
public class IngredientTableModel extends AbstractTableModel {

   private String[] cnames = {"Qty", "Unit", "Name"};
   private Recipe recipe;
   private String[] rowBuffer = new String[3];

   public IngredientTableModel(Recipe r) {
      recipe = r;
      rowBuffer[0] = "";
      rowBuffer[1] = "";
      rowBuffer[2] = "";
   }

   @Override
   public String getColumnName(int col) {
      return cnames[col];
   }

   @Override
   public int getRowCount() {
      return recipe.getNumberOfIngredients() + 1;
   }

   @Override
   public int getColumnCount() {
      return cnames.length;
   }

   @Override
   public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex == recipe.getNumberOfIngredients()) {
         return rowBuffer[columnIndex];
      } else {
         return recipe.getIngredient(rowIndex).getObject(
                 columnIndex).toString();
      }
   }

   @Override
   public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (rowIndex == recipe.getNumberOfIngredients()) {
         if (columnIndex == 2) {
            recipe.addIngredient(new Ingredient(rowBuffer[0], Unit.getUnit(rowBuffer[1]), 
                   recipejar.StringProcessor.fixInformalAnchors(aValue.toString())));
            rowBuffer[0] = "";
            rowBuffer[1] = "";
            rowBuffer[2] = "";
            this.fireTableCellUpdated(rowIndex, columnIndex);
            this.fireTableRowsInserted(rowIndex + 1, rowIndex + 1);
         } else {
            rowBuffer[columnIndex] = recipejar.StringProcessor.fixInformalAnchors(aValue.toString());
            this.fireTableCellUpdated(rowIndex, columnIndex);
         }
      } else {
         Ingredient i = recipe.getIngredient(rowIndex);
         if (columnIndex == 0) {
            i.setQuantity(aValue.toString());
         } else if (columnIndex == 1) {
            i.setUnit(Unit.getUnit(aValue.toString()));
         } else if (columnIndex == 2) {
            i.setName(recipejar.StringProcessor.fixInformalAnchors(aValue.toString()));
         }
         this.fireTableCellUpdated(rowIndex, columnIndex);
      }
   }

   @Override
   public boolean isCellEditable(int rowIndex, int columnIndex) {
      return true;
   }

   /**
    *
    * @param selectedRow
    * @return
    */
   public boolean moveRowDown(int selectedRow) {
      if (selectedRow < recipe.getNumberOfIngredients() - 1 && selectedRow >= 0) {
         recipe.swapIngredients(selectedRow, selectedRow + 1);
         this.fireTableDataChanged();
         return true;
      } else {
         return false;
      }
   }

   public boolean moveRowUp(int selectedRow) {
      if (selectedRow >= 1 && selectedRow < recipe.getNumberOfIngredients()) {
         recipe.swapIngredients(selectedRow, selectedRow - 1);
         this.fireTableDataChanged();
         return true;
      } else {
         return false;
      }
   }

   /**
    *
    * @param row
    * @return
    */
   public JMenu getConvertMenu(final int row) {
      JMenu menu = new JMenu("Convert To:");
      if (recipe.getIngredient(row).getUnit().isConvertable()) {
         Set<String> keys = recipe.getIngredient(row).getUnit().getConversionUnitKeySet();
         for (Iterator<String> i = keys.iterator(); i.hasNext();) {
            final String k = i.next();
            JMenuItem mi = new JMenuItem(k);
            mi.addActionListener(new ActionListener() {

               public void actionPerformed(ActionEvent e) {
                  Unit u = recipe.getIngredient(row).getUnit();
                  String qty = recipe.getIngredient(row).getQuantity();
                  recipe.getIngredient(row).setQuantity(Unit.convert(qty, u, Unit.getUnit(e.getActionCommand())));
                  recipe.getIngredient(row).setUnit(Unit.getUnit(e.getActionCommand()));
                  fireTableRowsUpdated(row, row);
               }
            });
            menu.add(mi);
         }
      } else {
         menu.setEnabled(false);
      }
      return menu;
   }
}
