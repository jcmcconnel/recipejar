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
import recipejar.filetypes.RecipeFile;
import recipejar.StringProcessor;
import recipejar.ProgramVariables;
import recipejar.Kernel;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import java.io.IOException;


/**
 * The purpose of this class is to serve as a recipe model for the editor panel.
 *
 * @author Owner
 */
public class Recipe {

    ///////////////////////////////////
    //////Non-static members///////////
    ///////////////////////////////////
    //Private
    private RecipeFile diskFile;
   
    private String originalTitle;
    private Document titleModel;

    private String originalNotes;
    private Document notesModel;

    private ArrayList<Ingredient> ingredients;
    private IngredientTableModel tmodel;

    private String originalProcedure;
    private Document procedureModel;

    private ArrayList<String> labels;

    ////////Public///////////
    //Constructors
    public Recipe(RecipeFile f) throws BadLocationException {
       diskFile = f;
       originalTitle = f.getTitle();
       titleModel = new PlainDocument();
       titleModel.insertString(0, originalTitle, null);
       originalNotes = recipejar.StringProcessor.convertToASCIILinebreaks(f.getNotes());
       notesModel = new PlainDocument();
       notesModel.insertString(0, originalNotes, null);
       ingredients = f.getIngredients();
       tmodel = new IngredientTableModel(this);
       originalProcedure = recipejar.StringProcessor.convertToASCIILinebreaks(f.getProcedure());
       procedureModel = new PlainDocument();
       procedureModel.insertString(0, originalProcedure, null);
       labels = f.getLabels();
    }

    public String getLabelsAsText() {
       String l = "";
       for (int i = 0; i < this.getLabels().size(); i++) {
           l = l + this.getLabels().get(i);
           if (i < this.getLabels().size() - 1) {
               l = l + ", ";
           }
       }
       return l;
    }
    /**
     * Save the model to the backing file.
     **/
    public void writeToDisk() throws BadLocationException, IOException{
      if (diskFile.exists()) {
         System.out.println("attempted to save existing");
         //return false;
      }
      if (hasTitleChanged()) { //Saving a new recipe.
         if (StringProcessor.isBadTitle(titleModel.getText(0, titleModel.getLength()))) {
            //JOptionPane.showMessageDialog(Kernel.topLevelFrame, "Please enter a valid title for your recipe.");
            //return false;
         } else {
            diskFile = new RecipeFile(ProgramVariables.buildAbsoluteFileNameFrom(titleModel.getText(0, titleModel.getLength())));
            diskFile.setTitle(titleModel.getText(0, titleModel.getLength()).trim());
         }
      }
      diskFile.setLabels(this.getLabelsAsText());
      diskFile.setNotes(StringProcessor.convertToXMLLineBreaks(StringProcessor.fixInformalAnchors(notesModel.getText(0, notesModel.getLength()))));

      diskFile.setProcedure(StringProcessor.convertToXMLLineBreaks(StringProcessor.fixInformalAnchors(procedureModel.getText(0, procedureModel.getLength()))));

      //updateMetaData();

      diskFile.save();
      //recipeChanged = false;
      //return true;
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
    public Document getTitleModel() {
       return titleModel;
    }
    public boolean hasTitleChanged() {
       try {
          return !originalTitle.equals(titleModel.getText(0, titleModel.getLength()));
       }
       catch (BadLocationException ble) {
          return true;
       }
    }

    /**
     *
     * @return
     */
    public Document getNotesModel() {
       return notesModel;
    }

    /**
     *
     * @param newNotes
     */
    //public void setNotes(String newNotes) {
    //   originalNotes = recipejar.StringProcessor.convertToXMLLineBreaks(newNotes);
    //}

    /**
     *
     * @return
     */
    public Document getProcedureModel() {
       return procedureModel;
    }

    /**
     *
     * @param newProcedure
     */
    //public void setProcedure(String newProcedure) {
    //   originalProcedure = recipejar.StringProcessor.convertToXMLLineBreaks(newProcedure);
    //}

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
       out = originalTitle + "\n";
       out = out + originalNotes +"\n";
       Iterator<Ingredient> i = ingredients.iterator();
       while(i.hasNext()){
          out = out + i.next().toString()+"\n";
       }
       out = out + originalProcedure + "\n";
       return out;
    }

    public String toXHTMLString(){
       StringWriter out = new StringWriter();
       out.write("<h1>"+originalTitle+"</h1>\n");
       out.write("<div id=\"notes\">" + originalNotes +"</div>\n");
       Iterator<Ingredient> i = ingredients.iterator();
       out.write("<div id=\"ingredients\"><ul>\n");
       while(i.hasNext()){
           out.write(i.next().toXHTMLString()+"\n");
       }
       out.write("</ul></div>\n");
       out.write("<div id=\"procedure\">" + originalProcedure + "</div>\n");
       return out.toString();
    }
}
