/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recipejar.lib;

import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

/**
 *
 * @author James McConnel
 */
public abstract class AbstractTextAction extends AbstractAction implements FocusListener, CaretListener {

   protected JTextComponent text;
   private ArrayList<JTextComponent> theClub;
   private boolean onlyActiveOnSelection;

   public AbstractTextAction(String name) {
      super(name);
      this.text = null;
      this.theClub = new ArrayList<JTextComponent>();
      this.setEnabled(false);
      onlyActiveOnSelection = false;
   }

   public AbstractTextAction(String name, KeyStroke stroke){
      super(name);
      this.text = null;
      this.theClub = new ArrayList<JTextComponent>();
      this.setEnabled(false);
      this.putValue(AbstractAction.ACCELERATOR_KEY, stroke);

      onlyActiveOnSelection = false;
   }

   public AbstractTextAction(String name, int mnem) {
      super(name);
      this.text = null;
      this.theClub = new ArrayList<JTextComponent>();
      this.setEnabled(false);
      this.putValue(AbstractAction.MNEMONIC_KEY, mnem);
      onlyActiveOnSelection = false;
   }

   public AbstractTextAction(String name, int mnem, int accel, String mask) {
      super(name);
      this.text = null;
      this.theClub = new ArrayList<JTextComponent>();
      this.setEnabled(false);
      String[] keystrokemasks = mask.trim().toUpperCase().split("-");
      int totalmodifier = 0;
      for (int i = 0; i < keystrokemasks.length; i++) {
         if (keystrokemasks[i].equals("DEFAULT")) {
            totalmodifier = totalmodifier + Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
            System.out.println(name+" "+(char)totalmodifier);
         } else if (keystrokemasks[i].equals("CTRL")) {
            totalmodifier = totalmodifier + KeyEvent.CTRL_DOWN_MASK;
         } else if (keystrokemasks[i].equals("ALT") || keystrokemasks[i].equals("OPTION")) {
            totalmodifier = totalmodifier + KeyEvent.ALT_DOWN_MASK;
         } else if (keystrokemasks[i].equals("SHIFT")) {
            totalmodifier = totalmodifier + KeyEvent.SHIFT_DOWN_MASK;
         } else if (keystrokemasks[i].equals("COMMAND") || keystrokemasks[i].equals("CMD")) {
            totalmodifier = totalmodifier + KeyEvent.META_DOWN_MASK;
         }
      }
      this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accel, totalmodifier));
      this.putValue(AbstractAction.MNEMONIC_KEY, mnem);
      onlyActiveOnSelection = false;
   }

   public void addField(JTextComponent t) {
      t.addFocusListener(this);
      t.addCaretListener(this);
      theClub.add(t);
   }

   public void removeField(JTextComponent t) {
      t.removeFocusListener(this);
      t.removeCaretListener(this);
      theClub.remove(t);
   }

   @Override
   public void focusGained(FocusEvent e) {
      for (int i = 0; i < theClub.size(); i++) {
         if (theClub.get(i).equals(e.getComponent())) {
            text = theClub.get(i);
            if (!onlyActiveOnSelection) {
               this.setEnabled(true);
            }
         }
      }
   }

   @Override
   public void focusLost(FocusEvent e) {
      if (!e.isTemporary() && e.getComponent().equals(text)) {
         text = null;
         this.setEnabled(false);
      }
   }

   public void caretUpdate(CaretEvent e) {
      if (e.getDot() != e.getMark()) {//there is a selection
         if (onlyActiveOnSelection) {
            this.setEnabled(true);
         }
      }else{
         if(onlyActiveOnSelection){
            this.setEnabled(false);
         }
      }
   }

   public void setOnlyActiveOnSelection(boolean b){
      onlyActiveOnSelection = b;
   }
}
