//James McConnel 5/6/2020
//Plan:
//
package recipejar.test;

import java.util.Random;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import recipejar.recipe.Unit;
import recipejar.recipe.Ingredient;

class testRecipe {
	public static recipejar.recipe.Recipe r;

	public static void main(String args[]) {
		try {
			Unit.readUnitsFromFile("units.txt");
			System.out.println("Units file read...");
			if (args.length == 0 || args[0].equals("help")) {
				System.out.println("Usage: java recipejar.test.testRecipe [Operation]");
				System.out.println("Operations:");
				System.out.println("      convert [Number] [Unit] [Unit]");
				System.out.println("      add-unit [Singular] [Plural]");
				return;
			}
			if (args[0].equals("add-unit")) {
				Unit.addUnit(args[1], args[2]);
			}
			if (args[0].equals("convert")) {
				System.out.println("Converting...");
				System.out.println(args[1] + " " + Unit.getUnit(args[2]).getPlural() + " equals "
						+ Unit.convert(args[1], Unit.getUnit(args[2]), Unit.getUnit(args[3])) + " " + args[3]);

			}
			ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
			ingredients.add(new Ingredient("2", Unit.getUnit("Slices"), "Bread"));
			ingredients.add(new Ingredient("1", Unit.getUnit("Tbsp"), "Peanut Butter"));
			r = new recipejar.recipe.Recipe("Peanut Butter Sandwiches", "", ingredients,
					"Spread peanut butter on one slice of bread.\n Top with second slice of bread.", new ArrayList());
			System.out.println("As string: ");
			System.out.print(r.toString());
			System.out.println();
			System.out.println("As XHTML: ");
			System.out.print(r.toXHTMLString());

		} catch(FileNotFoundException ex){
			System.out.println("File not found exception");
		}
	}
}
