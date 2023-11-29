/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recipejar;

import javax.swing.JComboBox;

/**
 *
 * @author James McConnel
 */
public class UnitCellEditor extends JComboBox<recipejar.recipe.Unit> {

   public UnitCellEditor() {
      super();
      for (int i = 0; i < recipejar.recipe.Unit.getUnitCount(); i++) {
         addItem(recipejar.recipe.Unit.getUnit(i));
      }
   }

   public void reload() {
      removeAllItems();
      for (int i = 0; i < recipejar.recipe.Unit.getUnitCount(); i++) {
         addItem(recipejar.recipe.Unit.getUnit(i));
      }
      setSelectedIndex(0);
   }

   /**
    * Without this, the first character of keyboard input would be discarded.
    * @param keyChar
    */
   public void takeKeyStroke(char keyChar) {
      int index = getSelectedIndex();
      for (int i = 0; i < recipejar.recipe.Unit.getUnitCount(); i++) {
         //If not the empty unit and the passed char is the same as the first char in unit name
         if (!recipejar.recipe.Unit.getUnit(i).toString().isEmpty() && Character.toUpperCase(keyChar) ==
                 Character.toUpperCase(recipejar.recipe.Unit.getUnit(i).toString().charAt(0))) {
            index = i;
            break;
         }
      }
      //No selection had been previously made.
      if (getSelectedItem().toString().isEmpty()) {
         setSelectedIndex(index);
         return;
      }
      if (Character.toUpperCase(getSelectedItem().toString().charAt(0)) != Character.toUpperCase(keyChar)) {
         //Not currently within the correct block.
         setSelectedIndex(index);
      } else {
         //Currently within the correct block.
         if (getSelectedIndex() != getItemCount() - 1) {
            //Not at the end of the list.
            if (Character.toUpperCase(getItemAt(getSelectedIndex() + 1).toString().charAt(0)) == Character.toUpperCase(keyChar)) {
               //Next item starts with same char.
               setSelectedIndex(getSelectedIndex() + 1);
            } else {
               setSelectedIndex(index);
            }
         } else {
            setSelectedIndex(index);
         }
      }
   }
}
