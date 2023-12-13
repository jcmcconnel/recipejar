package recipejar.lib;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author james
 */
public class Anchor implements Comparable<Anchor> {

   private File source;
   private String link;
   private String text;

   public Anchor(File newfile) {
      source = newfile;
      link = newfile.getPath();
      text = newfile.getName();
   }

   public Anchor(recipejar.filetypes.RecipeFile newfile) {
      source = newfile;
      link = newfile.getName();
      text = newfile.getTitle();
   }

   public Anchor(String l, String t) {
       link = l;
       text = t;
       source = new File(l);
   }

   public String getText() {
      return text;
      //return source.getTitle();
   }

   public String getLink() {
      return link;
      //return getSource().getName();
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
    * TODO: This is not what it is currently doing.
    * Computed thus: both fields null: 0
    *                link is null: -1
    *                text is null: -2
    *                otherwise link.hashcode() + text.hashcode()
    * @return
    */
   @Override
   public int hashCode() {
      int hash;
      if (link == null && text == null) {
         hash = 0;
      } else if (link == null) {
         hash = -1;
      } else if (text == null) {
         hash = -2;
      } else {
         hash = link.hashCode() + text.hashCode();
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
      return "<a href=\"" + link + "\">" + text + "</a>";
   }

   /**
    * @return the source
    */
   public File getSource() {
      return source;
   }
}
