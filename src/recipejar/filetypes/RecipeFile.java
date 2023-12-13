/*
 * RecipeFile.java
 *
 * Created on October 18, 2007, 2:55 PM
 *
 * @Author James McConnel
 *
 * License: Artistic, http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package recipejar.filetypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import recipejar.lib.AbstractXHTMLBasedFile;
import recipejar.lib.Element;
import recipejar.recipe.Ingredient;

/**
 * The purpose of this class is to provide fundamental capabilities for an HTML
 * based recipe.
 *
 * @author Owner
 */
public class RecipeFile extends AbstractXHTMLBasedFile {

    protected static RecipeFile recipeTemplate;
    public static RecipeFile setTemplate(RecipeFile r) {
       recipeTemplate = r;
       return recipeTemplate;
    }
    public static RecipeFile getTemplate() { return recipeTemplate; }
    ///////////////////////////////////
    //////Non-static members///////////
    ///////////////////////////////////
    //Private
    private ArrayList<Ingredient> ingredients;

    /**
     * All constructors feed into this for common stuff
     *
     * @throws IOException
     */
    private void initialize() throws IOException {
        //Adds RecipeFile specific tokens to the parser system.
        addToken("header");

        addToken("notes-header");
        addToken("notes");
        addToken("notes-footer");

        addToken("ingredients-header");
        addToken("ingredients");
        addToken("ingredients-footer");

        addToken("procedure-header");
        addToken("procedure");
        addToken("procedure-footer");

        addToken("program-footer");
        addToken("browser-footer");
        addToken("export-footer");

        if (exists()) { //Initialize data structures from file
            this.load();
            setIngredientsFromHTML(getDataElement("ingredients").getContent());
        } else {
            System.out.println("Recipe does not exist");
            System.out.println("Creating new fil: "+getAbsolutePath());
            this.createNewFile();
        }
        setActiveFooter("program-footer");
    }

    ///**
     //* Makes sure that the element exists in this RecipeFile, and if it does
     //* sets the content to the default given in the template.
     //*
     //* @param token the Element
     //*/
    //private void revertToDefault(String token) {
    //if (RecipeTemplate.getSource().dataElementExists(token)) {
    //if (!this.dataElementExists(token)) {
    //setDataElement(token, RecipeTemplate.getSource().getDataElement(token).clone());
    //} else {
    //this.getDataElement(token).setContent(RecipeTemplate.getSource().getDataElement(token).getContent());
    //}
    //}
    //}
    ////////Protected/////////
    private String activeFooter = "program-footer";

    /**
     *
     * @return
     */
    @Override
    protected String buildBody() {
        StringWriter out = new StringWriter();
        out.write("\n   <body>\n");
        if (recipeTemplate.dataElementExists("header")) {
            out.write("    " + processMacros(recipeTemplate.getDataElement("header").toString()) + "\n");
        }

        if (recipeTemplate.dataElementExists("notes-header")) {
            out.write("    " + processMacros(recipeTemplate.getDataElement("notes-header").toString()) + "\n");
        }
        out.write("    <div id=\"" + Section.NOTES.toString() + "\">");
        out.write(this.getNotes());
        out.write("    </div>\n");
        if (recipeTemplate.dataElementExists("notes-footer")) {
            out.write("    " + processMacros(recipeTemplate.getDataElement("notes-footer").toString()) + "\n");
        }

        if (recipeTemplate.dataElementExists("ingredients-header")) {
            out.write("    " + processMacros(recipeTemplate.getDataElement("ingredients-header").toString()) + "\n");
        }
        out.write("    <div id=\"" + Section.INGREDIENTS.toString() + "\">");
        out.write(this.getIngredientsAsHTML());
        out.write("\n    </div>\n");
        if (recipeTemplate.dataElementExists("ingredients-footer")) {
            out.write("    " + processMacros(recipeTemplate.getDataElement("ingredients-footer").toString()) + "\n");
        }

        if (recipeTemplate.dataElementExists("procedure-header")) {
            out.write("    " + processMacros(recipeTemplate.getDataElement("procedure-header").toString()) + "\n");
        }
        out.write("    <div id=\"" + Section.PROCEDURE.toString() + "\">");
        out.write(this.getProcedure());
        out.write("    </div>\n");
        if (recipeTemplate.dataElementExists("procedure-footer")) {
            out.write("    " + processMacros(recipeTemplate.getDataElement("procedure-footer").toString()) + "\n");
        }

        if (recipeTemplate.dataElementExists(activeFooter)) {
            out.write("    " + processMacros(recipeTemplate.getDataElement(activeFooter).toString()) + "\n");
        }
        out.write("  </body>\n");
        return out.toString();
    }

    /**
     * Returns the meta data value associated with the [LABEL]. Used by
     * processMacros in super.
     *
     * @param macro
     * @return The value associated with the string.
     */
    @Override
    protected String getMacroText(String macro) {
        if (macro.toUpperCase().equals("[LABELS]")) {
            String textStroke = new String();
            String savedMeta;
            if ((savedMeta = getMetaData("labels")) != null && !savedMeta.isEmpty()) {
                ArrayList<String> labels = new ArrayList<String>(Arrays.asList(savedMeta.split(",")));
                for (int i = 0; i < labels.size(); i++) {
                    if (i > 0) {
                        textStroke = textStroke + ", ";
                    }
                    if (activeFooter.equals("export-footer")) {
                        textStroke = textStroke + labels.get(i);
                    } else {
                        textStroke = textStroke + "<a href=\"" + "index.html#"
                                + recipejar.StringProcessor.underscoreSpaces(labels.get(i).trim()) + "\">"
                                + labels.get(i).trim() + "</a>";
                    }
                }
            } else {
                textStroke = textStroke + "Currently None.";
            }
            return textStroke;
        } else if (macro.toUpperCase().equals("[AUTHOR]")) {
            String savedMeta;
            if ((savedMeta = getMetaData("author")) != null && !savedMeta.isEmpty()) {
                return savedMeta;
            } else {
                return "Unknown";
            }
        } else if (macro.toUpperCase().equals("[USERPHONE]")) {
            String savedMeta;
            if ((savedMeta = getMetaData("userphone")) != null && !savedMeta.isEmpty()) {
                return savedMeta;
            } else {
                return "Unlisted";
            }
        } else if (macro.toUpperCase().equals("[USEREMAIL]")) {
            String savedMeta;
            if ((savedMeta = getMetaData("useremail")) != null && !savedMeta.isEmpty()) {
                return savedMeta;
            } else {
                return "Unlisted";
            }
        } else if (macro.toUpperCase().equals("[CUSTOM]")) {
            String savedMeta;
            if ((savedMeta = getMetaData("custom")) != null && !savedMeta.isEmpty()) {
                return savedMeta;
            } else {
                return "";
            }
        } else {
            return macro;
        }
    }

    ////////Public///////////
    //Constructors
    /**
     * Creates a new instance of RecipeFile
     *
     * @param name
     * @throws java.io.IOException
     */
    public RecipeFile(String name) throws IOException {
        super(name);
        initialize();
    }

    /**
     * Creates a new instance of RecipeFile given a normal file.
     *
     * @param f
     * @throws IOException
     */
    public RecipeFile(File f) throws IOException {
        super(f);
        initialize();
    }

    //General
    /**
     *
     * @param f
     * @throws IOException
     */
    public void export(File f) throws IOException {
        RecipeFile temp = new RecipeFile(f);
        temp.setActiveFooter("export-footer");
        temp.setTitle(this.getTitle());
        temp.setNotes(this.getNotes());
        temp.setProcedure(this.getProcedure());
        for (int i = 0; i < this.getIngredientListSize(); i++) {
            temp.setIngredient(i, this.getIngredient(i));
        }
        temp.setLabels(this.getLabelsAsText());
        temp.save();
        temp.setActiveFooter("program-footer");
    }

    /**
     * Returns true if this recipe has this label.
     *
     * @param s the label
     * @return
     */
    public boolean isLabeled(String s) {
        ArrayList<String> labels = getLabels();
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
   public String getHeader() {
      if (!dataElementExists("header")) {
            setHeader("");
      }
      return recipejar.StringProcessor.removeCarriageReturns(getDataElement("header").getContent());
   }

   /**
   *
   * @param newHeader
   */
   public void setHeader(String newHeader) {
      if (!dataElementExists("header")) {
         HashMap<String, String> m = new HashMap<String, String>();
         m.put("id", "header");
         Element e = setDataElement("div", m, newHeader);
      } else {
         getDataElement("header").setContent(newHeader);
      }
   }

   /**
   *
   * @return
   */
   public String getNotes() {
      if (!dataElementExists("notes")) {
            setNotes("");
      }
      return recipejar.StringProcessor.removeCarriageReturns(getDataElement("notes").getContent());
   }

   /**
   *
   * @param newNotes
   */
   public void setNotes(String newNotes) {
      if (!dataElementExists("notes")) {
         HashMap<String, String> m = new HashMap<String, String>();
         m.put("id", "notes");
         Element e = setDataElement("div", m, newNotes);
      } else {
         getDataElement("notes").setContent(newNotes);
      }
   }

   /**
   *
   * @return
   */
   public String getProcedure() {
      if (!dataElementExists("procedure")) {
         setProcedure("");
      }
      return recipejar.StringProcessor.removeCarriageReturns(getDataElement("procedure").getContent());
   }

   /**
   *
   * @param newProcedure
   */
   public void setProcedure(String newProcedure) {
      if (!dataElementExists("procedure")) {
         HashMap<String, String> m = new HashMap<String, String>();
         m.put("id", "procedure");
         Element e = setDataElement("div", m, newProcedure);
      } else {
         getDataElement("procedure").setContent(newProcedure);
      }
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
     * Returns all the labels this recipe has, as a string array.
     *
     * @return array of all the labels
     */
    public ArrayList<String> getLabels() {
        String allLabels = getMetaData("labels");
        if (allLabels != null && !allLabels.isEmpty()) {
            ArrayList<String> labels = new ArrayList<String>(Arrays.asList(allLabels.split(",")));
            ArrayList<String> trimmed = new ArrayList<String>(labels.size());
            for (int i = 0; i < labels.size(); i++) {
                trimmed.add(i, labels.get(i).trim());
            }
            return trimmed;
        }
        return null;
    }

    public void addLabel(String s) {
        this.setMetaData("labels", this.getMetaData("labels") + ", " + s);
    }

    /**
     * Puts the given string into the labels data element.
     *
     * @param text
     */
    public void setLabels(String text) {
        this.setMetaData("labels", text);
    }

    /////////////Ingredient Methods////////////

    /**
     *
     * @param i
     * @return
     */
    public Ingredient getIngredient(int i) {
        return ingredients.get(i);
    }
    public ArrayList<Ingredient> getIngredients() {
       return ingredients;
    }
    /**
     * Sets the ingredients array
     * @param l The given list
     */
    public void setIngredients(ArrayList<Ingredient> l){
       ingredients = l;
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

    public int getIngredientListSize() {
        return ingredients.size();
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
     * Loads the contents of the ingredients div into the ingredients array.
     *
     * @param s the div contents
     */
    private void setIngredientsFromHTML(String s) {
        if (ingredients == null) {
            ingredients = new ArrayList<Ingredient>();
        }
        int next = s.indexOf("<li>");
        while (next != -1) {
            next = next + 4;//# of chars in "<li>"
            ingredients.add(Ingredient.parse(s.substring(next, s.indexOf("</li>", next))));
            next = s.indexOf("<li>", next);
        }
    }

    private void setActiveFooter(String string) {
        activeFooter = string;
    }

    @Override
    protected void prepforSave() {
        setActiveFooter("browser-footer");
    }

    @Override
    protected void cleanUpAfterSave() {
        setActiveFooter("program-footer");
    }

    public String getLabelsAsString() {
        String l = "";
        for (int i = 0; i < this.getLabels().size(); i++) {
            l = l + this.getLabels().get(i);
            if (i < this.getLabels().size() - 1) {
                l = l + ", ";
            }
        }
        return l;
    }
    public enum Section {

        NOTES("notes"), INGREDIENTS("ingredients"), PROCEDURE("procedure");
        private final String id;

        Section(String s) {
            id = s;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
