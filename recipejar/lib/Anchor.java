package recipejar.lib;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author james
 */
public class Anchor implements Comparable<Anchor> {

   private RecipeFile source;

   public Anchor(RecipeFile newfile) {
      source = newfile;
   }

   public Anchor(String l, String t) {
      try {
         if (!l.contains(java.io.File.pathSeparator)) {
            source = new RecipeFile(Prefs.DIR_DB.toString() + l);
         } else {
            source = new RecipeFile(l);
         }
      } catch (IOException ex) {
         Logger.getLogger(Anchor.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   public String getText() {
      return source.getTitle();
   }

   public String getLink() {
      return getSource().getName();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final Anchor other = (Anchor) obj;
      if (this.getSource() == null || !this.source.equals(other.getSource())) {
         return false;
      }
      return true;
   }

   /**
    * Computed thus: both fields null: 0
    *                link is null: -1
    *                text is null: -2
    *                otherwise link.hashcode() + text.hashcode()
    * @return
    */
   @Override
   public int hashCode() {
      int hash;
      if (getSource() == null) {
         hash = 0;
      } else if (getSource() == null) {
         hash = -1;
      } else {
         hash = getSource().hashCode();
      }
      return hash;
   }

   @Override
   public int compareTo(Anchor o) {
      if (this.equals(o)) {
         return 0;
      } else {//
         return source.compareTo(o.getSource());
      }
   }

   @Override
   public String toString() {
      return "<a href=\"" + source.getName() + "\">" + source.getTitle() + "</a>";
   }

   /**
    * @return the source
    */
   public RecipeFile getSource() {
      return source;
   }
}
