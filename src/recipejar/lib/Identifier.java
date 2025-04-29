package recipejar.lib;


/**
 *
 * @author james
 */
   public class Identifier implements Comparable<Identifier> {
      public Anchor anchor;
      public String category;

      public Identifier(Anchor a){
         anchor = a;
      }
      public Identifier(String c){
         category = c;
      }

      public String getText(){
         if(anchor == null) return category;
         return anchor.getText();
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         }
         if (getClass() != obj.getClass()) {
            return false;
         }
         final Identifier other = (Identifier) obj;
         if (this.anchor != null && this.anchor.equals(other.anchor)) {
            return true;
         }
         if (this.category != null && this.category.equals(other.category)) {
            return true;
         }
         return false;
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
         int hash = 0;
         if (anchor != null) {
           return this.anchor.hashCode();
         } else if (category != null) {
            category.hashCode();
         }
         return hash;
      }

      @Override
      public int compareTo(Identifier o) {
         if (this.equals(o)) {
            return 0;
         } else {//
            return -1;
         }
      }

      @Override
      public String toString(){
         if(anchor != null) return anchor.getText();
         return category;
      }
   };
