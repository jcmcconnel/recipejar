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
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.text.Document;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import java.io.IOException;


/**
 * The purpose of this class is to serve as a recipe model for the editor panel.
 *
 * @author Owner
 */
public class Recipe implements TableModelListener, DocumentListener {

    ///////////////////////////////////
    //////Non-static members///////////
    ///////////////////////////////////
    //Private
    private RecipeFile diskFile;
   
    private String originalTitle;
    private Document titleModel;

    private String originalNotes;
    private Document notesModel;

    private ArrayList<Ingredient> originalIngredients;
    private ArrayList<Ingredient> ingredients;
    private IngredientTableModel tmodel;

    private String originalProcedure;
    private Document procedureModel;

    private String originalLabels;
    private Document labelsModel;
    private ArrayList<String> labels;

    private boolean tmodelChanged;

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
       originalIngredients = f.getIngredients();
       ingredients = new ArrayList(originalIngredients.size());
       for(Ingredient i : originalIngredients) ingredients.add(new Ingredient(i)); 
       tmodel = new IngredientTableModel(this);
       tmodel.addTableModelListener(this);
       originalProcedure = recipejar.StringProcessor.convertToASCIILinebreaks(f.getProcedure());
       procedureModel = new PlainDocument();
       procedureModel.insertString(0, originalProcedure, null);
       originalLabels = f.getMetaData("labels");
       labelsModel = new PlainDocument();
       labelsModel.insertString(0, originalLabels, null);

       tmodelChanged = false;

       titleModel.addDocumentListener(this);
       notesModel.addDocumentListener(this);
       procedureModel.addDocumentListener(this);
       labelsModel.addDocumentListener(this);
    }

   /**
   */
   @Override
   public void tableChanged(TableModelEvent e){
      System.out.println("Table Changed");
      tmodelChanged = true;
      Kernel.programActions.get("save").setEnabled(true);
   }

   @Override
   public void changedUpdate(DocumentEvent e) {
      System.out.println("Recipe Model Document Changed");
      Kernel.programActions.get("save").setEnabled(true);
   }

   @Override
   public void removeUpdate(DocumentEvent e) {
      System.out.println("Recipe Model Document Changed-removed");
      Kernel.programActions.get("save").setEnabled(true);
   }

   @Override
   public void insertUpdate(DocumentEvent e) {
      System.out.println("Recipe Model Document Changed-insert");
      Kernel.programActions.get("save").setEnabled(true);
   }

   /**
   * Save the model to the backing file.
   **/
   public boolean writeToDisk() throws BadLocationException, IOException{
      diskFile.setTitle(titleModel.getText(0, titleModel.getLength()).trim());
      diskFile.setHeader("<h1>"+titleModel.getText(0, titleModel.getLength()).trim()+"</h1>");
      diskFile.setNotes(StringProcessor.convertToXMLLineBreaks(StringProcessor.fixInformalAnchors(notesModel.getText(0, notesModel.getLength()))));
      diskFile.setIngredients(ingredients);
      diskFile.setProcedure(StringProcessor.convertToXMLLineBreaks(StringProcessor.fixInformalAnchors(procedureModel.getText(0, procedureModel.getLength()))));
      diskFile.setLabels(labelsModel.getText(0, labelsModel.getLength()));

      //updateMetaData();

      System.out.println("saving");
      diskFile.save();
      return true;
    }

    /**
     * Returns true if this recipe has this label.
     *
     * @param s the label
     * @return
     */
    public boolean isLabeled(String s) throws BadLocationException {
       String[] labels = labelsModel.getText(0, labelsModel.getLength()).split(",");
       for(int i=0; i < labels.length; i++){
          if(labels.equals(s)) return true;
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

   public boolean hasNotesChanged() {
       try {
         return !originalNotes.equals(notesModel.getText(0, notesModel.getLength()));
       }
       catch (BadLocationException ble) {
          return true;
       }
   }

   public boolean hasProcedureChanged() {
       try {
         return !originalProcedure.equals(procedureModel.getText(0, procedureModel.getLength()));
       }
       catch (BadLocationException ble) {
          return true;
       }
   }

   public boolean hasRecipeChanged() {
      try{
         return hasTitleChanged() || hasNotesChanged() || hasProcedureChanged() || tmodelChanged;
      }catch (NullPointerException npe) {
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
     * Changes the backing file to the one provided.
     * @param f The new RecipeFile
     */
    public void setDiskFile(RecipeFile f){
       diskFile = f;
    }

    /**
     *
     * @return
     */
    public Document getProcedureModel() {
       return procedureModel;
    }


    /**
     *
     * */
    public Document getLabelsModel(){
       return labelsModel;
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
     * Translates the ingredient array into an HTML list.
     *
     * @return
     */
    private String getIngredientsAsHTML() {
        //Remove empty ingredients first.
        for (int i = 0; i < ingredients.size() - 1; i++) {
            if (ingredients.get(i).getName().toString().isEmpty()) {
                ingredients.remove(i);
                i--;
            }
        }
        StringWriter s = new StringWriter();
        s.write("\n      <ul>\n");
        for (int i = 0; i < ingredients.size(); i++) {
            s.write(ingredients.get(i).toXHTMLString());
        }
        s.write("      </ul>");
        return s.toString();
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
   /**
    * Renames a recipe
    * 
    **/
   public boolean reTitle(String newTitle) {
      // Changing the name of this recipe.
      try {
         titleModel.remove(0, titleModel.getLength());
         titleModel.insertString(0, newTitle.trim(), null);
         recipejar.filetypes.IndexFile.getIndexFile().remove(diskFile);
         diskFile = new RecipeFile(ProgramVariables.DIR_DB.toString()+"/"+StringProcessor.removeBadChars(newTitle)+".html");
         writeToDisk();
         recipejar.filetypes.IndexFile.getIndexFile().add(diskFile);
         //TODO Remove old file
      }
      catch(BadLocationException e){}
      catch(IOException e){
         System.out.println("File Creation Failure in reTitle");
      }
      return true;
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
