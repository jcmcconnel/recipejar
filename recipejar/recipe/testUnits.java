//James McConnel 5/6/2020
//Plan:
//
package recipejar.recipe;

import file.*;
import java.util.Random;
class testUnits {
   public static void main(String args[]){
      Unit.readUnitsFromFile("units.txt");
      if(args[0].equals("add")){
         Unit.addUnit(args[1], args[2]);
      }
      if(args[0].equals("convert")){
         System.out.println("Converting...");
         System.out.println(args[1]+" "+Unit.getUnit(args[2]).getPlural()+" equals "+Unit.convert(args[1], Unit.getUnit(args[2]), Unit.getUnit(args[3]))+" "+args[3]);
      
      }
   }
}

