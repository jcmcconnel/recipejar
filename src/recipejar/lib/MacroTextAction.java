/*
  An action that can process and insert text according to a certain definition.
 */
package recipejar.lib;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Vector;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author James McConnel
 */
public class MacroTextAction extends AbstractTextAction {


   private Vector<Stroke> parts;

   public MacroTextAction(String name, char mnem, String macroString) {
      super(name, mnem);
      parts = new Vector<Stroke>();
      parse(macroString);
   }

   public MacroTextAction(String name, char mnem, char accel, String mask, String macroString) {
      super(name, mnem, accel, mask);
      parts = new Vector<Stroke>();
      parse(macroString);
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (text != null) {
         int putCaretAt = 0;
         boolean noSelectionCMD = true;
         StringWriter s = new StringWriter();
         for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).getStrokeType() == Command.TEXT) {
               s.write(parts.get(i).getTextStroke());
               if(noSelectionCMD){
                  putCaretAt = s.toString().length();
               }
            } else if (parts.get(i).getStrokeType() == Command.COLOR) {
               Color c = JColorChooser.showDialog(null, parts.get(i).getDialogName(), Color.BLACK);

               String R = Integer.toHexString(c.getRed()).toUpperCase();
               if (R.length() == 1) {
                  R = "0" + R;
               }
               String G = Integer.toHexString(c.getGreen()).toUpperCase();
               if (G.length() == 1) {
                  G = "0" + G;
               }
               String B = Integer.toHexString(c.getBlue()).toUpperCase();
               if (B.length() == 1) {
                  B = "0" + B;
               }
               s.write("#" + R + G + B);
               if(noSelectionCMD){
                  putCaretAt = s.toString().length();
               }
            } else if (parts.get(i).getStrokeType() == Command.INPUT) {
               String input = JOptionPane.showInputDialog(parts.get(i).getDialogName());
               s.write(input);
               if(noSelectionCMD){
                  putCaretAt = s.toString().length();
               }
            } else if (parts.get(i).getStrokeType() == Command.SELECTION) {
               if (text.getSelectedText() != null) {
                  s.write(text.getSelectedText());
                  putCaretAt = s.toString().length();
               } else {
                  putCaretAt = s.toString().length();
               }
                  noSelectionCMD = false;
            }
         }
         text.replaceSelection(s.toString());
         text.setCaretPosition(text.getCaretPosition() - (s.toString().length() - putCaretAt));
      }
   }

   private void parse(String macroString) {
      try {
         StringReader in = new StringReader(macroString);
         int c = in.read();
         StringWriter textStroke = new StringWriter();
         while (c != -1) {
            if (c == '[') {
               StringWriter s = new StringWriter();
               s.write(c);
               c = in.read();
               while (c != -1 && c != ']') {
                  //in case of an unpaired bracket, makes what came before a text stroke.
                  if (c == '[') {
                     textStroke.write(s.toString());
                     s = new StringWriter();
                  }
                  s.write(c);
                  c = in.read();
               }
               //picks up the closing bracket.
               if (c != -1) {
                  s.write(c);
               }
               if (isCommand(s.toString())) {
                  //If a command is encountered, the last text block is terminated.
                  //and added to the parts vector.
                  if (!textStroke.toString().isEmpty()) {
                     parts.add(new Stroke(textStroke.toString(), Command.TEXT));
                     textStroke = new StringWriter();
                  }
                  parts.add(new Stroke(s.toString(), getCommand(s.toString())));
               } else {
                  //If it isn't a command, then it is text.
                  textStroke.write(s.toString());
               }
            } else {
               textStroke.write(c);
            }
            c = in.read();
         }
         if (!textStroke.toString().isEmpty()) {
            parts.add(new Stroke(textStroke.toString(), Command.TEXT));
         }
      } catch (IOException ex) {
         //do nothing.  Just won't have an action.
      }
   }

   private enum Command {

      TEXT, COLOR, SELECTION, INPUT;
   }

   public static MacroTextAction buildFromConfigString(String configString) {
      return null;
   }

   private static boolean isCommand(String s) {
      if (s.toUpperCase().equals("[SELECTION]")) {
         return true;
      } else if (s.toUpperCase().equals("[COLOR]") || s.toUpperCase().startsWith("[COLOR:")) {
         return true;
      } else if (s.toUpperCase().equals("[INPUT]") || s.toUpperCase().startsWith("[INPUT:")) {
         return true;
      } else {
         return false;
      }
   }

   private Command getCommand(String s) {
      if (s.toUpperCase().equals("[SELECTION]")) {
         return Command.SELECTION;
      } else if (s.toUpperCase().equals("[COLOR]") || s.toUpperCase().startsWith("[COLOR:")) {
         return Command.COLOR;
      } else if (s.toUpperCase().equals("[INPUT]") || s.toUpperCase().startsWith("[INPUT:")) {
         return Command.INPUT;
      } else {
         return Command.TEXT;
      }
   }

   private class Stroke {

      private String textStroke;
      private String DialogName;

      public String getDialogName() {
         return DialogName;
      }
      private Command strokeType;

      public Command getStrokeType() {
         return strokeType;
      }

      public String getTextStroke() {
         return textStroke;
      }

      private Stroke(String commandString, Command c) {
         textStroke = null;
         DialogName = null;
         strokeType = c;
         if (strokeType == Command.TEXT) {
            textStroke = commandString;
         } else if (strokeType == Command.COLOR) {
            //[COLOR:  <-- 7 chars
            if (commandString.toUpperCase().equals("[COLOR]")) {
               DialogName = "Select Color";
            } else {
               DialogName = commandString.substring(7, commandString.length() - 1);
            }
         } else if (strokeType == Command.INPUT) {
            //[INPUT:  <-- 7 chars
            if (commandString.toUpperCase().equals("[INPUT]")) {
               DialogName = "Input:";
            } else {
               DialogName = commandString.substring(7, commandString.length() - 1);
            }
         }
      }
   }
}
