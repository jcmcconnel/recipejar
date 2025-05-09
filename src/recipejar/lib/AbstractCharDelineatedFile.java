/*
 * AbstractCharDelineatedFile.java
 *
 * @Author James McConnel
 *
 * License: Artistic, http://www.opensource.org/licenses/artistic-license-2.0.php
 */

package recipejar.lib;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
 
/**
 *
 * @author james
 */
public abstract class AbstractCharDelineatedFile extends File {
   private final String delineator;
   private final char commentCharacter;

   /**
    * All the non-comment lines.
    */
   private ArrayList<String[]> DataLines;


   public AbstractCharDelineatedFile(String filename, String d, char commentChar) {
      super(filename);
      delineator = d;
      commentCharacter = commentChar;
      DataLines = new ArrayList<String[]>();
      if (this.exists()) {
         try {
            load();
         } catch (IOException ex) {
         }
      }
   }

   /**
    *
    * @throws IOException
    */
   private void load() throws IOException {
      FileReader in = new FileReader(this);
      int c = in.read();
      while (c != -1) {
         //remove comments
         if (c == commentCharacter) {//Comments
            c = removeCommentedLine(in);
         } else if (c == '\n' || c == '\r') {//carriage returns cause problems if left in.
            //Remove multiple newlines
            while (c == '\n' || c == '\r') {
               c = in.read();
            }
         } else {
            //Data lines
            String line = new String();
            while (c != '\n' && c != -1) {
               line = line + (char) c;
               c = in.read();
            }
            String[] data = line.split(delineator);
            DataLines.add(data);
         }

         if(c != -1 && c == '\n'){//If not a newline, somebody already got the first char of the line.
            c = in.read();
         }
      }
   }

   private int removeCommentedLine(FileReader in) throws IOException {
      int c = in.read();
      //remove commented line
      while (c != '\n' && c != -1) {
         c = in.read();
      }
      return c;
   }

   public int getDataLength() {
      return DataLines.size();
   }

   public String[] getLine(int n) {
      return DataLines.get(n);
   }

   public abstract void save();
}
