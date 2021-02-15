/**
 * Unit.java 
 * Part of the RecipeJar project.
 * An unit conversion and management class, for use in a recipe construct.
 *
 * By: James McConnel (yehoodig@gmail.com) Last updated 5/8/2020 Status: Able to compile.
 * License: MIT (I think, I don't understand the legal stuff too well.)
 */
package recipejar.recipe;

import java.util.Iterator;
import java.lang.Math;
import java.text.DecimalFormat;
import java.util.Collections;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import recipejar.lib.*;

/**
 *
 * @author james
 */
public class Unit implements Comparable<Unit> {

   public static ArrayList<Unit> Units;

   protected static AbstractCharDelineatedFile diskFile;

   private HashMap<String, String> conversionUnits;
   private boolean useSingular = false;
   private String singular = "";
   private String plural = "";

   public Unit() {
      plural = "";
      singular = "";
      conversionUnits = new HashMap<String,String>();
   }

   public Unit(String p, String s) {
      plural = p;
      singular = s;
      conversionUnits = new HashMap<String,String>();
   }

   /**
    * Reads from a string of the form: [Plural Form],[Singular Form],[Convertable Unit Plural form]([Factor])|[Converatable Unit Plural form]([Factor])|...
    */
   public Unit(String combined) {
      String[] parts = combined.split(",");
      if (parts.length == 1) {
         plural = parts[0];
         singular = "";
      } else if (parts.length == 2) {
         plural = parts[0];
         singular = parts[1];
      } else if (parts.length == 3) {
         plural = parts[0];
         singular = parts[1];
         conversionUnits = new HashMap<String, String>();
         String[] convUnts;
         if (parts[2].contains("|")) {
            convUnts = parts[2].split("\\|");
         } else {
            convUnts = new String[1];
            convUnts[0] = parts[2];
         }
         for (int i = 0; i < convUnts.length; i++) {
            int endKey = convUnts[i].lastIndexOf("(");
            String key = convUnts[i].substring(0, endKey);
            String factor = convUnts[i].substring(endKey + 1, convUnts[i].length() - 1);
            conversionUnits.put(key.toUpperCase(), factor);
         }
      }
   }

   public String getPlural() {
      return plural;
   }

   public String getSingular() {
      return singular;
   }

   @Override
   public String toString() {
      if (useSingular) {
         return singular;
      } else {
         return plural;
      }
   }

   @Override
   public int compareTo(Unit o) {
      if (this.equals(o)) {
         return 0;
      } else if (plural.toUpperCase().equals(o.getPlural().toUpperCase())) {
         //
         return singular.toUpperCase().compareTo(o.getSingular().toUpperCase());
      } else {
         return plural.toUpperCase().compareTo(o.getPlural().toUpperCase());
      }
   }

   /**
    *
    * @param o
    * @return
    */
   public boolean equals(Unit o) {
      if (this.getClass().isInstance(o)) {
         //A Unit
         if (this.plural.equals(o.getPlural()) && this.singular.equals(o.getSingular())) {
            return true;
         } else {
            return false;
         }
      } else if (o.toString().equals(this.plural)) {
         //Something else, probably a string.
         return true;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 41 * hash + (this.singular != null ? this.singular.hashCode() : 0);
      hash = 41 * hash + (this.plural != null ? this.plural.hashCode() : 0);
      return hash;
   }

   public boolean isConvertable() {
      return (this.conversionUnits != null && !conversionUnits.isEmpty());
   }

   public String getConversionFactor(Unit u){
      String factor = conversionUnits.get(u.getPlural().toUpperCase());
      if(factor == null) factor = conversionUnits.get(u.getSingular().toUpperCase());
      return factor;
   }

   /**
    * @param s The name of the unit to convert to.
    * @return A String giving the formula for conversion.
    */
   public String getConversionFactor(String s) {
      String factor = conversionUnits.get(getUnit(s).getPlural().toUpperCase());
      if(factor == null) factor = conversionUnits.get(getUnit(s).getSingular().toUpperCase());
      return factor;
   }

   public int getConversionUnitCount() {
      return this.conversionUnits.size();
   }

   public Set<String> getConversionUnitKeySet() {
      return this.conversionUnits.keySet();
   }

   public void setSingular(boolean b) {
      useSingular = b;
   }




   
   /** *********************************************************************
    *  Static methods ONLY beyond this point!
    *  ********************************************************************
    */


   /**
    *
    */
   public static void readUnitsFromFile(String s){
      diskFile = new AbstractCharDelineatedFile(s,","){
         @Override
         public void save() {
            FileWriter out = null;
            try {
               out = new FileWriter(diskFile);

               out.write(";These are the units that RecipeJar recognizes.\n");
               out.write(";You can add your own simply by typing them in below.\n");
               out.write(";For more information about Units, see: [The project website...]\n");
               out.write(";Please note, units are used exactly as they are typed, spaces and all.\n");
               out.write("\n");
               out.write(";Lines beginning with \";\" are comments, and will be ignored by the program.\n");
               out.write(";Feel free to add comments, etc.  But be aware User comments will be discarded if the program saves this file.\n");
               for (int i = 0; i < Units.size(); i++) {
                  if (Units.get(i).getPlural() != null && !Units.get(i).getPlural().isEmpty()) {
                     out.write(Units.get(i).getPlural());
                     if (Units.get(i).getSingular() != null && !Units.get(i).getSingular().isEmpty()) {
                        out.write("," + Units.get(i).getSingular());
                     }
                     System.out.println(Units.get(i));
                     if(Units.get(i).isConvertable()){
                        Iterator<String> j = Units.get(i).conversionUnits.keySet().iterator();
                        if(j.hasNext()) out.write(",");
                        while(j.hasNext()){
                           String name = j.next();
                           out.write(name+"("+Units.get(i).conversionUnits.get(name)+")");
                           if(j.hasNext()) out.write("|");
                        }
                     }
                     out.write("\n");
                  }
               }
               out.close();
            } catch (IOException ex) {
               //do nothing
            }
         }
      };

      Units = new ArrayList<Unit>();
      Reader in = null;
      try {
         in = new FileReader(diskFile);
      } catch (FileNotFoundException ex) {
         in = new StringReader("Cups,Cup\nTbsp,Tbsps\ntsp,tsps\ncount\n");
      }
      try {
         int c = in.read();
         while (c != -1) {
            if (c == ';') {
               while (c != '\n' && c != -1) {
                  c = in.read();
               }
            }
            StringWriter u = new StringWriter();
            //the \r is important!
            while (c != '\n' && c != '\r' && c != -1) {
               u.write(c);
               c = in.read();
            }
            if (!u.toString().isEmpty()) {
               Units.add(new Unit(u.toString().trim()));
            }
            c = in.read();
         }
         Units.add(new Unit());
      } catch (IOException ex) {
         Units.add(new Unit());
      }
      Collections.sort(Units);
   }

   /**
    * Adds the new unit to the array.
    * @param ptext
    * @param stext
    * @return
    */
   public static Unit addUnit(String ptext, String stext) {
      Unit newUnit;
      Units.add(newUnit = new Unit(ptext, stext));
      Collections.sort(Units);
      diskFile.save(); //Warning!!! You will lose all User comments.
      return newUnit;
   }

   public static Unit getUnit(int i) {
      return Units.get(i);
   }

   /**
    * Returns the unit in the list given by the parameter "s".  If it does not
    * exist null is returned.
    *
    * @param s The unit to search for.
    * @return The unit defined by s.
    */
   public static Unit getUnit(String s) {
      for (int i = 0; i < Units.size(); i++) {
         if (Units.get(i).getPlural().toUpperCase().equals(s.toUpperCase()) || Units.get(i).getSingular().toUpperCase().equals(s.toUpperCase())) {
            return Units.get(i);
         }
      }
      return null;
   }

   public static int getUnitCount() {
      return Units.size();
   }

   public static int indexOf(String value) {
      for (int i = 0; i < Units.size(); i++) {
         if (Units.get(i).toString().equals(value)) {
            return i;
         }
      }
      return -1;
   }

   /**
    *
    * @param f
    * @return
    */
   public static String decimalToFraction(float f) {
      int wholepart = (int) f;
      f = f - wholepart;
      String output = "";
      if(Math.abs(wholepart) > 0) output = Integer.toString(wholepart)+" ";
      if(f == 0.75) output += "3/4";
      if(f == 0.5) output += "1/2";
      if(f > 0.3 && f < 0.334) output += "1/3";
      if(f == 0.25) output += "1/4";
      if(f == 0.2) output += "1/5";
      if(f == 0.125) output += "1/8";
      if(f == 0.0625) output += "1/16";
      if(!output.isEmpty()) return output;
      return new DecimalFormat("0.##").format(f);
   }


   private static float parseMixedNumber(String qty){
      if (!qty.trim().contains(" ") && qty.contains("/")) {
         //A fraction; if does contain a " " then it's a mixed number
         try {
            float num = Float.parseFloat(qty.substring(0, qty.indexOf("/")));
            float denom = Float.parseFloat(qty.substring(qty.indexOf("/") + 1));
            return num / denom;
         } catch (NumberFormatException numberFException) {
            return -1;//cannot convert
         }
      } else if (qty.trim().contains(" ") && qty.contains("/")) {
         //a mixed number
         try {
            float whole = Float.parseFloat(qty.substring(0, qty.indexOf(" ")));
            float num = Float.parseFloat(qty.substring(qty.indexOf(" ") + 1, qty.indexOf("/")));
            float denom = Float.parseFloat(qty.substring(qty.indexOf("/") + 1));
            return whole + num / denom;
         } catch (NumberFormatException numberFException) {
            return -1;//cannot convert
         }
      }
      return Float.parseFloat(qty);
   }

   /**
    * Parses the number out of the string given by qty,
    * and processes it with the function defined by factor,
    * then returns the result as a string.
    * @param qty
    * @param factor
    * @param outputFraction
    * @return
    */
   private static String convert(String qty, String factor, boolean outputFraction) {
      float x = 0;
      if (qty.isEmpty()) {
         x = 0;
      } else {//Parse out the value of qty
         if (qty.trim().contains("-")) {
            //A range
            return (convert(qty.substring(0, qty.indexOf("-")).trim(), factor, outputFraction) + "-"
                    + convert(qty.substring(qty.indexOf("-") + 1).trim(), factor, outputFraction));
         }
         try {
            x = parseMixedNumber(qty);
         } catch (NumberFormatException numberFormatException) {
            return qty;//cannot convert
         }
      }
      try {
         float result;
         //Plus or minus indicates a function.
         if (factor.contains("+")) {
            String[] formula = factor.split("\\+");
            float m = parseMixedNumber(formula[0]);
            float b = parseMixedNumber(formula[1]);
            result = m * x + b;
         } else if (factor.contains("-")) {
            String[] formula = factor.split("-");
            float m = parseMixedNumber(formula[0]);
            float b = parseMixedNumber(formula[1]);
            result = m * x - b;
         } else {
            result = Float.parseFloat(factor) * x;
         }
         if (outputFraction) {
            return decimalToFraction(result);
         } else {
            return (new DecimalFormat("0.##")).format(result);
         }
      } catch (NumberFormatException numberFormatException) {
         return qty;//Cannot parse number
      }
   }

   public static String convert(String qty, String factor) {
      if (qty.contains(".")) {
         return convert(qty, factor, false);
      } else {
         return convert(qty, factor, true);
      }
   }
   public static String convert(String qty, Unit from, Unit to) {
     String factor = from.getConversionFactor(to); 
     if(factor != null){
        return convert(qty, factor);
     }else return null;
   }


}
