/*
 * RecipeFile.java
 *
 * Created on October 18, 2007, 2:55 PM
 *
 * @Author James McConnel
 *
 * License: Artistic, http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package recipejar.recipe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
//import recipejar.data.Prefs;
//import recipejar.data.RecipeTemplate;

/**
 * The purpose of this class is to provide fundamental capabilities for an HTML
 * based recipe.
 *
 * @author Owner
 */
public class RecipeFile extends AbstractXHTMLBasedFile {

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
            //DO NOT do this if this file is the template.
            if (!Prefs.TEMPLATE_RECIPE.toString().equals(this.getPath())
                    || RecipeTemplate.getSource() != null) {
                //Make recipefile conform to template if it doesn't already.
                if (!this.getStyle().equals(RecipeTemplate.getSource().getStyle())) {
                    this.setStyle(RecipeTemplate.getSource().getStyle());
                }
                revertToDefault("header");
                revertToDefault("notes-header");
                revertToDefault("notes-footer");
                revertToDefault("ingredients-header");
                revertToDefault("ingredients-footer");
                revertToDefault("procedure-header");
                revertToDefault("procedure-footer");
                revertToDefault("program-footer");
                revertToDefault("browser-footer");
                revertToDefault("export-footer");

                for (int i = 0; i < RecipeTemplate.getSource().getMetaItems().size(); i++) {
                    if (RecipeTemplate.getSource().getMetaItems().get(i).hasAttribute("name")
                            && getMetaData(RecipeTemplate.getSource().getMetaItems().get(i).getAttribute("name")) != null) {
                        getMetaItems().add(RecipeTemplate.getSource().getMetaItems().get(i).clone());
                    }
                }
                //ingredients = new ArrayList<Ingredient>();
                //setIngredientsFromHTML(getDataElement("ingredients").getContent());
            }
        } else { //File does not exist, so load default data from template
            setDataElement("title", new recipejar.data.Element("title"));
            //DO NOT do this if this file is the template.
            if (!Prefs.TEMPLATE_RECIPE.toString().equals(this.getPath())
                    || RecipeTemplate.getSource() != null) {
                setDataElement("style", RecipeTemplate.getSource().getDataElement("style").clone());

                setDataElement("notes", RecipeTemplate.getSource().getDataElement("notes").clone());
                setDataElement("ingredients", RecipeTemplate.getSource().getDataElement("ingredients").clone());
                ingredients = new ArrayList<Ingredient>();
                setIngredientsFromHTML(getDataElement("ingredients").getContent());
                setDataElement("procedure", RecipeTemplate.getSource().getDataElement("procedure").clone());
                revertToDefault("header");

                revertToDefault("notes-header");
                revertToDefault("notes-footer");

                revertToDefault("ingredients-header");
                revertToDefault("ingredients-footer");

                revertToDefault("procedure-header");
                revertToDefault("procedure-footer");

                revertToDefault("program-footer");
                revertToDefault("browser-footer");
                revertToDefault("export-footer");
            } else {
                throw new FileNotFoundException("Template does not exist.");
            }
        }
        setActiveFooter("program-footer");
    }

    /**
     * Makes sure that the element exists in this RecipeFile, and if it does
     * sets the content to the default given in the template.
     *
     * @param token the Element
     */
    private void revertToDefault(String token) {
        if (RecipeTemplate.getSource().dataElementExists(token)) {
            if (!this.dataElementExists(token)) {
                setDataElement(token, RecipeTemplate.getSource().getDataElement(token).clone());
            } else {
                this.getDataElement(token).setContent(RecipeTemplate.getSource().getDataElement(token).getContent());
            }
        }
    }
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
        if (this.dataElementExists("header")) {
            out.write("    " + processMacros(this.getDataElement("header").toString()) + "\n");
        }

        if (this.dataElementExists("notes-header")) {
            out.write("    " + processMacros(this.getDataElement("notes-header").toString()) + "\n");
        }
        out.write("    <div id=\"" + Section.NOTES.toString() + "\">");
        out.write(this.getNotes());
        out.write("    </div>\n");
        if (this.dataElementExists("notes-footer")) {
            out.write("    " + processMacros(this.getDataElement("notes-footer").toString()) + "\n");
        }

        if (this.dataElementExists("ingredients-header")) {
            out.write("    " + processMacros(this.getDataElement("ingredients-header").toString()) + "\n");
        }
        out.write("    <div id=\"" + Section.INGREDIENTS.toString() + "\">");
        out.write(this.getIngredientsAsHTML());
        out.write("\n    </div>\n");
        if (this.dataElementExists("ingredients-footer")) {
            out.write("    " + processMacros(this.getDataElement("ingredients-footer").toString()) + "\n");
        }

        if (this.dataElementExists("procedure-header")) {
            out.write("    " + processMacros(this.getDataElement("procedure-header").toString()) + "\n");
        }
        out.write("    <div id=\"" + Section.PROCEDURE.toString() + "\">");
        out.write(this.getProcedure());
        out.write("    </div>\n");
        if (this.dataElementExists("procedure-footer")) {
            out.write("    " + processMacros(this.getDataElement("procedure-footer").toString()) + "\n");
        }

        if (this.dataElementExists(activeFooter)) {
            out.write("    " + processMacros(this.getDataElement(activeFooter).toString()) + "\n");
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
                String[] labels = savedMeta.split(",");
                for (int i = 0; i < labels.length; i++) {
                    if (i > 0) {
                        textStroke = textStroke + ", ";
                    }
                    if (activeFooter.equals("export-footer")) {
                        textStroke = textStroke + labels[i];
                    } else {
                        textStroke = textStroke + "<a href=\"" + "index.html#"
                                + recipejar.Util.underscoreSpaces(labels[i].trim()) + "\">"
                                + labels[i].trim() + "</a>";
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
        String l = "";
        for (int i = 0; i < this.getLabels().length; i++) {
            l = l + this.getLabels()[i];
            if (i < this.getLabels().length - 1) {
                l = l + ", ";
            }
        }
        temp.setLabels(l);
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
        String[] labels = getLabels();
        if (labels != null) {
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].equals(s)) {
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
    public String getNotes() {
        if (!dataElementExists("notes")) {
            setNotes("");
        }
        return recipejar.Util.removeCarriageReturns(getDataElement("notes").getContent());
    }

    /**
     *
     * @param newNotes
     */
    public void setNotes(String newNotes) {
        if (!dataElementExists("notes")) {
            HashMap<String, String> m = new HashMap<String, String>();
            m.put("id", "notes");
            recipejar.data.Element e = setDataElement("div", m, newNotes);
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
        return recipejar.Util.removeCarriageReturns(getDataElement("procedure").getContent());
    }

    /**
     *
     * @param newProcedure
     */
    public void setProcedure(String newProcedure) {
        if (!dataElementExists("procedure")) {
            HashMap<String, String> m = new HashMap<String, String>();
            m.put("id", "procedure");
            recipejar.data.Element e = setDataElement("div", m, newProcedure);
        } else {
            getDataElement("procedure").setContent(newProcedure);
        }
    }

    /**
     * Returns all the labels this recipe has, as a string array.
     *
     * @return array of all the labels
     */
    public String[] getLabels() {
        String allLabels = getMetaData("labels");
        if (allLabels != null && !allLabels.isEmpty()) {
            String[] labels = allLabels.split(",");
            String[] trimmed = new String[labels.length];
            for (int i = 0; i < labels.length; i++) {
                trimmed[i] = labels[i].trim();
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
    //public void swapIngredients(int a, int b) {
    //    if ((a >= 0 && a < ingredients.size()) && (b >= 0 && b < ingredients.size())) {
    //        Ingredient ia = ingredients.get(a);
    //        Ingredient ib = ingredients.get(b);

    //        ingredients.set(b, ia);
    //        ingredients.set(a, ib);
    //    }
    //}

    ///**
    // *
    // * @param i
    // * @return
    // */
    //public Ingredient getIngredient(int i) {
    //    return ingredients.get(i);
    //}

    ///**
    // * Replaces Ingredient at index i, with Ingredient I.
    // *
    // * @param i
    // * @param I
    // */
    //public void setIngredient(int i, Ingredient I) {
    //    ingredients.set(i, I);
    //}

    ///**
    // * Appends ingredient to the end of the list.
    // *
    // * @param I
    // */
    //public void addIngredient(Ingredient I) {
    //    ingredients.add(I);
    //}

    //protected Ingredient removeIngredient(int i) {
    //    return ingredients.remove(i);
    //}

    //public int getIngredientListSize() {
    //    return ingredients.size();
    //}

    ///**
    // * Translates the ingredient array into an HTML list.
    // *
    // * @return
    // */
    //private String getIngredientsAsHTML() {
    //    //Remove empty ingredients first.
    //    for (int i = 0; i < ingredients.size() - 1; i++) {
    //        if (ingredients.get(i).getName().toString().isEmpty()) {
    //            ingredients.remove(i);
    //            i--;
    //        }
    //    }
    //    StringWriter s = new StringWriter();
    //    s.write("\n      <ul>\n");
    //    for (int i = 0; i < ingredients.size() - 1; i++) {
    //        s.write(ingredients.get(i).toString());
    //    }
    //    s.write("      </ul>");
    //    return s.toString();
    //}

    ///**
    // * Loads the contents of the ingredients div into the ingredients array.
    // *
    // * @param s the div contents
    // */
    //private void setIngredientsFromHTML(String s) {
    //    if (ingredients == null) {
    //        ingredients = new ArrayList<Ingredient>();
    //    }
    //    int next = s.indexOf("<li>");
    //    while (next != -1) {
    //        next = next + 4;//# of chars in "<li>"
    //        ingredients.add(Ingredient.parse(s.substring(next, s.indexOf("</li>", next))));
    //        next = s.indexOf("<li>", next);
    //    }
    //}

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
        for (int i = 0; i < this.getLabels().length; i++) {
            l = l + this.getLabels()[i];
            if (i < this.getLabels().length - 1) {
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
