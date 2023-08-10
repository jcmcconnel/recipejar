/*
 * Recipe.java
 *
 * Created on October 18, 2007, 2:55 PM
 *
 * @Author James McConnel
 *
 * License: Artistic, http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package recipejar.recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.StringWriter;
import recipejar.recipe.IngredientTableModel;


/**
 * The purpose of this class is to provide fundamental capabilities for an HTML
 * based recipe.
 *
 * @author Owner
 */
public class Recipe {

    ///////////////////////////////////
    //////Non-static members///////////
    ///////////////////////////////////
    //Private
    private String title;
    private String notes;
    private ArrayList<Ingredient> ingredients;
    private IngredientTableModel tmodel;
    private String procedure;
    private ArrayList<String> labels;

    ////////Public///////////
    //Constructors
    public Recipe(String t, String n, ArrayList<Ingredient> i, String p, ArrayList<String> l){
       title = t;
       notes = n;
       ingredients = i;
       tmodel = new IngredientTableModel(this);
       procedure = p;
       labels = l;
    }


    /**
     * Returns true if this recipe has this label.
     *
     * @param s the label
     * @return
     */
    public boolean isLabeled(String s) {
        if (labels != null) {
            for (int i = 0; i < labels.size(); i++) {
                if (labels.get(i).equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /////Getters and Setters/////
    /**
     *
     * @return
     */
    public String getTitle() {
       return title;
    }

    /**
     *
     * @return
     */
    public String getNotes() {
       return recipejar.StringProcessor.convertToASCIILinebreaks(notes);
    }

    /**
     *
     * @param newNotes
     */
    public void setNotes(String newNotes) {
       notes = recipejar.StringProcessor.convertToXMLLineBreaks(newNotes);
    }

    /**
     *
     * @return
     */
    public String getProcedure() {
       return recipejar.StringProcessor.convertToASCIILinebreaks(procedure);
    }

    /**
     *
     * @param newProcedure
     */
    public void setProcedure(String newProcedure) {
       procedure = recipejar.StringProcessor.convertToXMLLineBreaks(newProcedure);
    }

    /**
     * Returns all the labels this recipe has, as a string array.
     *
     * @return array of all the labels
     */
    public ArrayList<String> getLabels() {
       return labels;
    }

    public void addLabel(String s) {
       labels.add(s);
    }

    /**
     * Puts the given string into the labels data element.
     *
     * @param text
     */
    public void setLabels(String text) {
    }

    ///////////Ingredient Methods////////////
    public void swapIngredients(int a, int b) {
        if ((a >= 0 && a < ingredients.size()) && (b >= 0 && b < ingredients.size())) {
            Ingredient ia = ingredients.get(a);
            Ingredient ib = ingredients.get(b);

            ingredients.set(b, ia);
            ingredients.set(a, ib);
        }
    }

    /**
     *
     * @param i
     * @return
     */
    public Ingredient getIngredient(int i) {
        return ingredients.get(i);
    }

    /**
     * Replaces Ingredient at index i, with Ingredient I.
     *
     * @param i
     * @param I
     */
    public void setIngredient(int i, Ingredient I) {
        ingredients.set(i, I);
    }

    /**
     * Appends ingredient to the end of the list.
     *
     * @param I
     */
    public void addIngredient(Ingredient I) {
        ingredients.add(I);
    }

    protected Ingredient removeIngredient(int i) {
        return ingredients.remove(i);
    }

    public int getNumberOfIngredients() {
        return ingredients.size();
    }

    public IngredientTableModel getTableModel(){
       return tmodel;
    }

    @Override
    public String toString(){
       String out = new String();
       out = title + "\n";
       out = out + notes +"\n";
       Iterator<Ingredient> i = ingredients.iterator();
       while(i.hasNext()){
          out = out + i.next().toString()+"\n";
       }
       out = out + procedure + "\n";
       return out;
    }

    public String toXHTMLString(){
       StringWriter out = new StringWriter();
       out.write("<h1>"+title+"</h1>\n");
       out.write("<div id=\"notes\">" + notes +"</div>\n");
       Iterator<Ingredient> i = ingredients.iterator();
       out.write("<div id=\"ingredients\"><ul>\n");
       while(i.hasNext()){
           out.write(i.next().toXHTMLString()+"\n");
       }
       out.write("</ul></div>\n");
       out.write("<div id=\"procedure\">" + procedure + "</div>\n");
       return out.toString();
    }
}
