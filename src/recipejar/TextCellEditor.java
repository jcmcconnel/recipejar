/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recipejar;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 *
 * @author James McConnel
 */
public class TextCellEditor extends JTextField {

   private int unfocusedBuffer;

   TextCellEditor() {
      super();
      this.addFocusListener(new FocusListener() {
         @Override
         public void focusGained(FocusEvent e) {
            System.out.println("text cell editor focus gained");
            if (unfocusedBuffer != -1) {
               if (Character.getType(unfocusedBuffer) != Character.CONTROL) {
                  setText("" + (char) unfocusedBuffer);
               } else {
                  setText("");
               }
               unfocusedBuffer = -1;
            } else {
               selectAll();
            }
         }

         @Override
         public void focusLost(FocusEvent e) {
            System.out.println("focus lost in text cell editor");
         }
      });
      unfocusedBuffer = -1;
   }


   void putToBuffer(char keyChar) {
      unfocusedBuffer = keyChar;
   }
}
