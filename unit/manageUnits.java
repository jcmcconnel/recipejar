//James McConnel 5/6/2020
//Plan:
//
package unit;

import filetype.*;
import java.util.Random;
class manageUnits {
   public static void main(String args[]){
      Unit.readUnitsFromFile("units.txt");
      if(args[0].equals("add")){}
      if(args[0].equals("convert")){
         System.out.println("Converting...");
         System.out.println(Unit.convert(args[1], Unit.getUnit(args[2]), Unit.getUnit(args[3])));
      
      }
   }
}

