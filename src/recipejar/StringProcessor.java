/**
 * @author James McConnel
 *
 * Defines a class with general string processing functions for the Recipejar program.
 */
package recipejar;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import javax.swing.KeyStroke;

/**
 *
 * @author james
 */
public class StringProcessor {

   /**
    * Changes spaces to underscores
    *
    * @param cat category name
    **/
   public static String underscoreSpaces(String cat) {
      StringWriter s = new StringWriter();
      for (int i = 0; i < cat.length(); i++) {
         int c = cat.charAt(i);
         if (c == ' ') {
            s.write('_');
         } else {
            s.write(c);
         }
      }
      return s.toString();
   }

   /**
    * Changes underscores to spaces
    *
    * @param cat category name
    **/
   public static String spaceUnderscores(String cat) {
      StringWriter s = new StringWriter();
      for (int i = 0; i < cat.length(); i++) {
         int c = cat.charAt(i);
         if (c == '_') {
            s.write(' ');
         } else {
            s.write(c);
         }
      }
      return s.toString();

   }

   /**
    * Removes carriage returns.
    * For some reason, some of my recipes have been showing up with carriage returns
    * along with newlines.  No big deal, but it causes some weird caret issues
    * in the text areas.  Specifically, the cursor is placed half a character width
    * to the right of where it should be when that line is preceded by the '\r'
    * character.  This method simply removes the '\r's.
    *
    * @param content
    * @return
    */
   public static String removeCarriageReturns(String content) {
      if (content.indexOf("\r") != -1) {
         String parts[] = content.split("\r");
         content = "";
         for (int i = 0; i < parts.length; i++) {
            content = content + parts[i];
         }
      }
      return content;
   }

   /**
    * Strips out characters that would not be allowed in a filename.
    *
    * @param s The input string
    * @return The new string
    */
   public static String removeBadChars(String s) {
      String inProcess;
      inProcess =
              removeChar(' ', s);
      inProcess =
              removeChar('\t', inProcess);
      inProcess =
              removeChar('\n', inProcess);
      inProcess =
              removeChar('\r', inProcess);
      inProcess =
              removeChar('\'', inProcess);
      inProcess =
              removeChar('\\', inProcess);
      inProcess =
              removeChar('/', inProcess);
      inProcess =
              removeChar('*', inProcess);
      inProcess =
              removeChar('?', inProcess);
      return (inProcess.trim());
   }

   /**
    * Removes all instances of a given character from a given string.
    *
    * @param x The given char
    * @param from The given string
    * @return The string without the char
    */
   private static String removeChar(char x, String from) {
      String newString = new String();
      for (int i = 0; i
              < from.length(); i++) {
         if (from.charAt(i) != x) {
            newString = newString + from.charAt(i);
         }

      }
      return (newString);
   }

   /**
    * Turns anchor tags with recipe names into anchor tags with file names.
    * @param string
    * @return
    */
   public static String fixInformalAnchors(String string) {
      for (int i = 0; i < string.length(); i++) {
         if (string.charAt(i) == '<') {
            if (string.charAt(i + 1) == 'a' && string.charAt(i + 2) == ' ') {
               //For anchor tags...
               i = string.indexOf("href=\"", i) + 6;
               int j = string.indexOf("\">", i);
               String filename = string.substring(i, j);
               if (!filename.contains(".") &&//no dot or slash means not a path.
                       !(filename.contains("/") || filename.contains("\\")) &&//a path, but not in recipes folder.
                       !filename.contains("#")) {//# means a fragment identifier
                  string = string.substring(0, i) + removeBadChars(filename)
                          + ".html" + string.substring(j);
               }
            }
         }
      }
      return string;
   }

   /**
    * Removes &lt;br/&gt; tags and replaces them with '\n' chars.
    * @param string
    * @return
    */
   public static String convertToASCIILinebreaks(String string) {
      StringReader in = new StringReader(string);
      StringWriter toReturn = new StringWriter();
      try {
         int c = in.read();
         while (c != -1) {
            if (c == '<') {
               StringWriter temp = new StringWriter();
               c = in.read();
               while (c != '>') {
                  temp.write(c);
                  c = in.read();
               }
               if (temp.toString().equals("br/")) {
                  toReturn.write("\n");
                  c = in.read();
               } else {
                  toReturn.write("<" + temp.toString() + ">");
                  c = in.read();
               }
            } else if (c == '\n') {
               //Do nothing; i.e. remove the newline.
               c = in.read();
            } else {
               toReturn.write(c);
               c = in.read();
            }
         }
      } catch (IOException ex) {
      }
      return toReturn.toString();
   }

   /**
    * Changes '\n's to &lt;br/&gt;s
    * @param string
    * @return
    */
   public static String convertToXMLLineBreaks(String string) {
      String toReturn = new String();
      for (int i = 0; i < string.length(); i++) {
         if (string.charAt(i) == '\n') {
            toReturn = toReturn + "<br/>\n";
         } else {
            toReturn = toReturn + string.charAt(i);
         }
      }
      return toReturn;
   }

   /**
    *
    * @param f
    * @return
    */
   public static String decimalToFraction(float f) {
      if (f == 0.5) {
         return "1/2";
      } else {
         float numerator = 1, denominator = 1;

         for (int i = 0; i < 1000; i++) {
            if ((numerator / denominator) < f) {
               numerator++;
            } else if ((numerator / denominator) > f) {
               denominator++;
               numerator = 1;
            } else {
               if (((int) denominator) == 1) {
                  return Integer.toString((int) numerator);
               } else if (((int) numerator) > ((int) denominator)) {
                  int whole = (int) (numerator / denominator);
                  return whole + " " + (((int) numerator) - whole * ((int) denominator)) + "/" + ((int) denominator);
               } else {
                  return ((int) numerator) + "/" + ((int) denominator);
               }
            }
         }
         return new DecimalFormat("0.##").format(f);
      }
   }

   public static boolean isBadTitle(String text) {
      return removeBadChars(text).trim().toUpperCase().equals("TEMPLATE")
              || removeBadChars(text).trim().toUpperCase().equals("INDEX")
              || removeBadChars(text).trim().toUpperCase().equals("CINDEX")
              || text.isEmpty();
   }

}
