/**
 * Unit.java 
 * Part of the RecipeJar project.
 * An unit conversion and management class, for use in a recipe construct.
 *
 * By: James McConnel (yehoodig@gmail.com) Last updated 5/8/2020 Status: Able to compile.
 * License: MIT (I think, I don't understand the legal stuff too well.)
 */

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
      conversionUnits = null;
   }

   public Unit(String p, String s) {
      plural = p;
      singular = s;
      conversionUnits = null;
   }

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
            conversionUnits.put(key, factor);
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

   public String getConversionFactor(String s) {
      return conversionUnits.get(s);
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
   
   //Unitfile
   protected static void readUnitsFromFile(String s){
      diskFile = new AbstractCharDelineatedFile(s,","){
         @Override
         public void save() {
            FileWriter out = null;
            try {
               out = new FileWriter(diskFile);
               for (int i = 0; i < Units.size(); i++) {
                  if (Units.get(i).getPlural() != null && !Units.get(i).getPlural().isEmpty()) {
                     out.write(Units.get(i).getPlural());
                     if (Units.get(i).getSingular() != null && !Units.get(i).getSingular().isEmpty()) {
                        out.write("," + Units.get(i).getSingular());
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
      return newUnit;
   }

   public static Unit getUnit(int i) {
      return Units.get(i);
   }

   /**
    * Returns the unit in the list given by the parameter "s".  If it does not
    * exist null is returned.
    *
    * @param s The unit to search for, or create
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

}
