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
      String qtyToken = "<span class=\"qty\">";
      int quantity = s.indexOf(qtyToken)+qtyToken.length();
      int endQuantity = s.indexOf("</span>", quantity);
      if (quantity != -1) {
         data[0] = s.substring(quantity, endQuantity);
         s = s.substring(endQuantity+7);
      }

      data[1] = new String();
      String unitToken = "<span class=\"unit\">";
      int unit = s.indexOf(unitToken)+unitToken.length();
      int endUnit = s.indexOf("</span>", unit);
      if (unit != -1) {
         data[1] = s.substring(unit, endUnit);
         s = s.substring(endUnit+7);
      }
      
      data[2] = new String();
      String nameToken = "<span class=\"name\">";
      int name = s.indexOf(nameToken)+nameToken.length();
      int endName = s.indexOf("</span>", name);
      if (name != -1) {
         data[2] = s.substring(name, endName);
         s = s.substring(endName+7);
      }
      
      return new Ingredient(data[0], Unit.getUnit(data[1]), data[2]);
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getQuantity() {
      if(quantity != null) return quantity;
      else return "";
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
      if(unit != null) {
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
      }
      if(quantity != null) this.quantity = quantity;
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
            if(unit == null) return "";
            else return unit.toString();
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
      if(unit != null) s.write("<span class=\"unit\">" + getUnit().toString() + "</span> ");
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
