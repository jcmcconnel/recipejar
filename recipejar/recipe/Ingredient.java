package recipejar.recipe;

import java.io.StringWriter;
import java.text.DecimalFormat;


public class Ingredient {

   private String quantity = null;
   private Unit unit = null;
   private String name = null;

   public Ingredient(String qty, Unit unt, String n) {
      super();
      unit = unt;
      name = n;
      setQuantity(qty);
   }

   /**
    * Takes a string (ostensibly the contents of an HTML list item),
    * and returns the three pieces of data:
    * number, Unit, and name as an Ingredient.
    *
    * @param s The list item contents
    * @return the new Ingredient
    **/
   public static Ingredient parse(String s) {
      String[] data = new String[3];
      data[0] = new String();
      int quantity = s.indexOf("<span class=\"qty\">");
      if (quantity == -1) {
         for (int i = 0; i < s.length(); i++) {
            char c = s.trim().charAt(i);
            if (Character.isDigit(c) || Character.isSpaceChar(c) || c == '/' || c == '-' || c == '.') {
               data[0] = data[0] + c;
            } else {
               s = s.substring(i);
               break;
            }
         }
      } else {
         quantity = s.indexOf(">", quantity) + 1;
         data[0] = s.substring(quantity, s.indexOf("</span>", quantity));
         s = s.substring(s.indexOf("</span>", quantity) + 7);
      }

      data[1] = new String();
      int unit = s.indexOf("<span class=\"unit\">");
      if (unit == -1) {
         //Not unit if there aren't at least two more words in the data.
         if (s.trim().contains(" ") && !data[0].isEmpty()) {
            String u = new String();
            for (int i = 0; i < s.trim().length(); i++) {
               char c = s.trim().charAt(i);
               if (!Character.isSpaceChar(c)) {
                  u = u + c;
               } else {
                  break;
               }
            }
            if (!u.isEmpty()) {
               //If unit does exist
               if (Unit.getUnit(u) != null) {
                  data[1] = u;
                  s = s.substring(u.length());
               }
            }
         }
      } else {
         unit = s.indexOf(">", unit) + 1;
         String data1String = s.substring(unit, s.indexOf("</span>", unit));
         s = s.substring(s.indexOf("</span>", unit) + 7);
         if (Unit.getUnit(data1String) != null) {
            data[1] = data1String;
         } else {
            //add unit text to the name field
            s = s.substring(0, s.indexOf("<span class=\"name\">") + 19) + data1String + s.substring(s.indexOf("<span class=\"name\">") + 19);
         }
      }

      data[2] = new String();
      int name = s.indexOf("<span class=\"name\">");
      if (name == -1) {
         data[2] = s.trim();
      } else {
         name = s.indexOf(">", name) + 1;
         if (data[1].isEmpty()) {
            //The Unit field is empty
            String u = new String();
            for (int i = 0; i < s.substring(name).trim().length(); i++) {
               char c = s.substring(name).trim().charAt(i);
               if (!Character.isSpaceChar(c)) {
                  u = u + c;
               } else {
                  break;
               }
            }
            if (!u.isEmpty()) {
               //If unit does not exist
               if (Unit.getUnit(u) != null) {
                  data[1] = u;
                  while (Character.isSpaceChar(s.charAt(name))) {
                     name++;
                  }
                  name = name + u.length();
               }
            }
         }
         data[2] = s.substring(name, s.indexOf("</span>", name));
         //All the data was put the last cell.
         if (data[0].isEmpty() && data[1].isEmpty()
                 && Character.isDigit(data[2].trim().charAt(0))) {
            return parse(data[2]);
         }
      }
      return new Ingredient(data[0], new Unit(data[1]), data[2]);
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getQuantity() {
      return quantity;
   }

   /**
    * Sets the quantity field of the unit object.  Also parses the given
    * quantity to determine if the value is singular or plural, and
    * sets the useSingular flag accordingly.
    * 
    * @param quantity The value of the quantity field
    */
   public final void setQuantity(String quantity) {
      String temp;
      if (quantity.trim().contains("-")) {
         //A range
         temp = quantity.substring(quantity.indexOf("-") + 1).trim();
      } else {
         temp = quantity.trim().toUpperCase();
      }
      if (temp.equals("1") || temp.equals("ONE")) {
         unit.setSingular(true);
      } else if (!temp.contains(" ") && temp.contains("/")) {
         //A fraction; if does contain a " " then fraction is greater than 1 and therefore plural
         try {
            int num = Integer.parseInt(temp.substring(0, temp.indexOf("/")));
            int denom = Integer.parseInt(temp.substring(temp.indexOf("/") + 1));
            if (num <= denom) {
               unit.setSingular(true);
            }
         } catch (NumberFormatException numberFormatException) {
            //Integer not parsable.
         }
      } else {
         unit.setSingular(false);
      }
      this.quantity = quantity;
   }

   /**
    * 
    * @return The unit.
    */
   public Unit getUnit() {
      return unit;
   }

   public void setUnit(Unit unit) {
      this.unit = unit;
   }

   public Object getObject(int i) {
      switch (i) {
         case 0:
            return getQuantity();
         case 1:
            //return the unit that should be listed in the table; plural.
            return unit.toString();
         case 2:
            return getName();
         default:
            throw new NullPointerException("Column Index out of bounds.");
      }
   }

   /**
    * Returns this ingredient as an HTML <li>
    * @return
    */
   public String toXHTMLString() {
      StringWriter s = new StringWriter();
      s.write("         <li>");
      s.write("<span class=\"qty\">" + getQuantity() + "</span> ");
      s.write("<span class=\"unit\">" + getUnit() + "</span> ");
      s.write("<span class=\"name\">" + getName() + "</span>");
      s.write("</li>\n");
      return s.toString();
   }

   /**
    * Returns this ingredient as a String.
    * @return
    */
   @Override
   public String toString() {
      StringWriter s = new StringWriter();
      s.write(getQuantity()+" ");
      s.write(getUnit() + " ");
      s.write(getName() + " ");
      return s.toString();
   }

   public boolean contains(String s) {
      if(quantity.contains(s)){
         return true;
      }
      if(name.contains(s)){
         return true;
      }
      if(unit.getPlural().contains(s) || unit.getSingular().contains(s)){
         return true;
      }
      return false;
   }



}
