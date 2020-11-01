package recipejar.filetypes;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import java.io.Reader;
import java.io.StringReader;

import recipejar.lib.AbstractXHTMLBasedFile;
import recipejar.lib.Anchor;
import recipejar.lib.Element;
import recipejar.filetypes.RecipeFile;

/**
 * It works like this: The program starts up, and scans the Recipes folder checking the meta tags
 * of each, to make sure it is a recipe and what labels are attached to it.  This data then gets
 * added to the data structure contained in the class. 
 * (This is the old way, scanning every time?  There has got to be a better way.)
 * (I remember now, I did it this way, because I was not sure how to parse the current index file, and be sure it was in fact current.)
 * @author Owner
 */
public class IndexFile extends AbstractXHTMLBasedFile {

    /**
     * The anchors list is mapped by Section, and then by category heading.  "DEFAULT" is the uncategorized list.
     */
   private EnumMap<Section, HashMap<String, ArrayList<Anchor>>> anchors;

   public IndexFile(String name){
      super(name);
      for (int i = 0; i < Section.values().length; i++) {
         addToken(Section.values()[i].toString());
      }
      addToken("header");
      addToken("footer");
      addToken("section-header");
      addToken("section-footer");

      addToken("letterA");
      addToken("letterB");
      addToken("letterC");
      addToken("letterD");
      addToken("letterE");
      addToken("letterF");
      addToken("letterG");
      addToken("letterH");
      addToken("letterI");
      addToken("letterJ");
      addToken("letterK");
      addToken("letterL");
      addToken("letterM");
      addToken("letterN");
      addToken("letterO");
      addToken("letterP");
      addToken("letterQ");
      addToken("letterS");
      addToken("letterT");
      addToken("letterU");
      addToken("letterV");
      addToken("letterW");
      addToken("letterX");
      addToken("letterY");
      addToken("letterZ");
      addToken("letter0");

      /*                     section          label      the list           */
      anchors = new EnumMap<Section, HashMap<String, ArrayList<Anchor>>>(Section.class);
      for (int i = 0; i < Section.values().length; i++) {
         anchors.put(Section.values()[i], new HashMap<String, ArrayList<Anchor>>());
      }
      for (int i = 0; i < Section.values().length; i++) {
         this.putList(Section.values()[i], "DEFAULT", new ArrayList<Anchor>());
      }
      try {
          load();
      }
      catch(IOException e) {
          System.out.print("Section parsing failed\n");
          System.out.print("\n"+e.toString()+"\n");
      }
   }

    @Override
    protected void load() throws IOException{
        try {
         super.load();
        } catch(IOException e){
          System.out.print("index loading failed");
          System.out.print(e.toString());
        }
        //Parse sections for anchors
        for(Section s : Section.values()){
            String content = getDataElements().get(s.toString()).getContent();
            StringReader r = new StringReader(content);
            parseForAnchors(s, "DEFAULT", r);
        }
    }

   /**
    * Searches the xhtml file for anchors listen in the index.
    * 
    * @param s The section being parsed (Only used to look up the map to store the anchors.
    * @param category The category being parsed.  Starts with "DEFAULT".  Allows recursion for multiple lists in a section.
    * @param in Reader for the input source for this object.
    * @throws java.io.IOException Thrown by any one of the many "reads".
    */
    private void parseForAnchors(Section s, String category, Reader in) throws IOException {
      int c = in.read();
      while (c != -1) {
         if (c == '<') {
            c = in.read();
            //If encountered thing is an instruction of some kind...
            if (c == '!' || c == '?') {
               loseCommentsEtc(in);
               c = in.read();
            } else {
               //this is an element.
               String tag = new String();
               while (c != '>') {
                  tag = tag + (char) c;
                  c = in.read();
               }
               Element e = Element.parse(tag);

               if (e != null) {
                  if (!e.isSelfClosing()) {
                      e.setContent(parseForContentAsText(e.getName(), in));
                     if (e.getName().toLowerCase().equals("a")) {
                        anchors.get(s).get(category).add(new Anchor(e.getAttribute("href"), e.getContent()));
                     } else if (e.getName().toLowerCase().equals("ul")) {
                        if(e.getAttribute("id") != null) {
                            parseForAnchors(s, e.getAttribute("id"), new StringReader(e.getContent()));
                        } else {
                            parseForAnchors(s, "DEFAULT", new StringReader(e.getContent()));
                        }
                     } else {
                        parseForAnchors(s, category, new StringReader(e.getContent()));
                     }
                  }
               }
            }
         } else {
            if (c != -1) {
               c = in.read();
            }
         }
      }
   }
   
   

   /************Public Members********************/
   /**
    * Searches the text of the given list for one with designation.
    * If found returns true.
    * Differs from containsLink in that it searches the anchor text,
    * not the link.
    *
    * @param list The list to search
    * @param designation The title of the Recipe.
    * @return true if found, false otherwise.
    */
   public static boolean containsTitle(ArrayList<Anchor> list, String designation) {
      for (int i = 0; i < list.size(); i++) {
         if (list.get(i).getText().equals(designation)) {
            return true;
         }
      }
      return false;
   }

   public boolean containsTitle(String designation){
      return containsTitle(getList(Section.parse(designation), designation), designation);
   }

   /**
    * Searches the given list for a link with the link of designation.
    * If it finds one, returns true.
    * Differs from containsTitle, in that it searches the link,
    * not the text.
    *
    * @param list The list to search
    * @param designation The link text.
    * @return true if found, false otherwise.
    */
   public static boolean containsLink(ArrayList<Anchor> list, String designation) {
      if (list == null) {
         return false;
      }
      for (int i = 0; i < list.size(); i++) {
         if (list.get(i).getLink().equals(designation)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Adds the newfile to the default list, in the appropriate section.
    * @param newfile
    * @throws java.io.IOException
    */
   public void add(RecipeFile newfile) throws IOException {
      Section sec;
      sec = Section.parse(newfile.getTitle());
      Anchor a = new Anchor(newfile);
      if (!getList(sec, "DEFAULT").contains(a)) {
         getList(sec, "DEFAULT").add(a);
      }
      this.save();
      this.updateCategoriesOf(newfile);
   }

   /**
    * Calls removeLink for the given file on all lists.
    * @param f file to remove
    */
   public void remove(RecipeFile f) {
      for (int i = 0; i < Section.values().length; i++) {
         Object[] keys = getSection(Section.values()[i]).keySet().toArray();
         for (int j = 0; j < keys.length; j++) {
            try {
               removeLink(f, keys[j].toString());
            } catch (IOException ex) {
               JOptionPane.showMessageDialog(null, "Fatal Error.  See \"errorLog.txt\" for more information.");
               System.exit(1);
            }
         }
      }
   }

   /**
    * Assumes that the categories in the recipefile have changed,
    * and looks in every list for a reference to this recipefile.
    * If it should be there but isn't it adds it.
    * If it isn't there but should be it removes it.
    * @param newfile
    */
   public void updateCategoriesOf(RecipeFile newfile) {
      ArrayList<String> categories = labels();
      for (int i = 0; i < categories.size(); i++) {
         ArrayList<Anchor> anchorList = this.getList(Section.parse(categories.get(i)), categories.get(i));
         //Remove from list if label removed.
         if (containsLink(anchorList, newfile.getName()) && !newfile.isLabeled(categories.get(i))) {
            try {
               this.removeLink(newfile, categories.get(i));
               //Add to list if label added.
            } catch (IOException ex) {
               //The category doesn't exist, or there isn't a link for this recipe in that category.
               //In other words, my job is done.
            }
            //Add to list if label added.
         } else if (!containsLink(anchorList, newfile.getName()) && newfile.isLabeled(categories.get(i))) {
            anchorList.add(new Anchor(newfile));
         }
      }
      String[] labels = newfile.getLabels();
      if (labels != null) {
         for (int i = 0; i < labels.length; i++) {
            if (!categories.contains(labels[i])) {
               ArrayList<Anchor> category = new ArrayList<Anchor>();
               category.add(new Anchor(newfile));
               this.putList(Section.parse(labels[i]), labels[i].trim(), category);
            }
         }
      }
   }

   /**
    * 
    * @param s The Section that contains the category.
    * @param key the name of the category ArrayList to get.
    * @return The vector of anchors mapped to the given key.
    */
   public ArrayList<Anchor> getList(Section s, String key) {
      return anchors.get(s).get(recipejar.StringProcessor.underscoreSpaces(key));
   }

   /**
    * Adds a list to the anchor HashMap.
    * @param s The section to put the new list in.
    * @param key The key to map the list too.
    * @param a The ArrayList to add.
    */
   public void putList(Section s, String key, ArrayList<Anchor> a) {
      anchors.get(s).put(recipejar.StringProcessor.underscoreSpaces(key), a);
   }

   /**
    * Returns a list of all labels currently in the index.
    * @return List of labels
    */
   public ArrayList<String> labels() {
      ArrayList<String> categories = new ArrayList<String>();
      for (int i = 0; i < Section.values().length; i++) {
         Object[] keys = getSection(Section.values()[i]).keySet().toArray();
         for (int j = 0; j < keys.length; j++) {
            if (!keys[j].equals("DEFAULT")) {
               categories.add(recipejar.StringProcessor.spaceUnderscores(keys[j].toString()));
            }
         }
      }
      return categories;
   }

    public String buildSection(int i){
        return buildSection(Section.parse(i));
    }
   /**
    * Returns an HTML DIV representation of the section
    * given by s, as text.
    * @param s The section to construct.
    * @return The HTML representation.
    */
   public String buildSection(Section s) {
      StringWriter out = new StringWriter();
      out.write("    <div id=\"" + s.toString() + "\">");
      if (s == Section.SECTION_OTHER) {
         out.write("<h2>" + "Other" + "</h2>");
      } else {
         out.write("<h2>" + s.getId() + "</h2>");
      }
      if (!anchors.get(s).get("DEFAULT").isEmpty()) {
         ArrayList<Anchor> list = anchors.get(s).get("DEFAULT");
         Collections.sort(list);
         out.write("\n      <ul>\n");
         for (int k = 0; k < list.size(); k++) {//each anchor
            out.write("        <li>" + list.get(k).toString() + "</li>\n");
         }
         out.write("      </ul>\n");
      }
      Object[] ckeys = anchors.get(s).keySet().toArray();
      Arrays.sort(ckeys);
      for (int j = 0; j < ckeys.length; j++) {//each category
         if (!ckeys[j].toString().equals("DEFAULT")) {
            out.write("\n      <ul id=\"" + ckeys[j] + "\"><h3>" + recipejar.StringProcessor.spaceUnderscores((String) ckeys[j]) + "</h3>\n");
            ArrayList<Anchor> list = anchors.get(s).get(ckeys[j].toString());
            Collections.sort(list);
            for (int k = 0; k < list.size(); k++) {//each anchor
               out.write("        <li>" + list.get(k).toString() + "</li>\n");
            }
            out.write("      </ul>\n");
         }
      }
      out.write("    </div>\n");
      return out.toString();
   }

   /**
    *
    * @param s
    * @return
    */
   public String getTextpaneFriendlySection(Section s){
      return doctype + "\n<html><head></head><body>" + buildSection(s) + "</body></html>";
   }

   /**
    * Checks the contents of the Recipes folder against the index
    * and updates the index
    *
    * @param parent parent of the progress dialog.  Can be null.
    * @throws java.io.FileNotFoundException
    * @throws IOException
    */
   private void mergeDOMWithDir(Component parent) throws FileNotFoundException, IOException {
      File recipeFolder = this.getParentFile();
      /*
      Get a list of files in the Recipes directory
       */
      if (recipeFolder.exists() && recipeFolder.isDirectory()) {
         File[] fileList = recipeFolder.listFiles();
         ProgressMonitor pm = new ProgressMonitor(parent, "Loading Index", "", 0, fileList.length - 1);
         /*
         For each file in the list
          */
         for (int i = 0; i < fileList.length; i++) {
            pm.setProgress(i);
            pm.setNote(fileList[i].getName());
            /*
            Ignore cindex, index and non html files
             */
            if (fileList[i].isFile() && fileList[i].getName().contains(".html")
                    && !fileList[i].getName().equals("cindex.html")
                    && !fileList[i].getName().equals("index.html")) {
               try {
                  RecipeFile r = new RecipeFile(fileList[i]);
                  ArrayList<Anchor> alphaSection = this.getList(Section.parse(r.getTitle()), "DEFAULT");
                  alphaSection.add(new Anchor(r));
                  updateCategoriesOf(r);
               } catch (IOException ex) {
                  JOptionPane.showMessageDialog(null, "A file appears to be corrupt. I was told: \n\""
                          + ex.getMessage() + "\"\nI suggest you open the file manually and correct the problem.  It is most likely a missing or incorrect html tag.", "Error Loading File", JOptionPane.ERROR_MESSAGE);
               }
            }
         }
         pm.close();
         this.save();
      }
   }

   ///**
    //* Looks for the HTML Element given by token in
    //* the template file, and if that exists, looks for it in
    //* this, and either clones the template element or
    //* copies the contents depending on whether or not it exists in
    //* this.
    //* @param token designator for the element
    //*/
    //private void resetElement(String token) {
    //if (IndexTemplate.getSource().dataElementExists(token)) {
    //
    //if (!this.dataElementExists(token)) {
    //setDataElement(token, IndexTemplate.getSource().getDataElement(token).clone());
    //} else {
    //this.getDataElement(token).setContent(IndexTemplate.getSource().getDataElement(token).getContent());
    //}
    //}
    //}

   @Override
   protected String buildBody() {
      StringWriter out = new StringWriter();
      out.write("\n  <body>\n");
      if (dataElementExists("header")) {
         out.write("    " + processMacros(getDataElement("header").toString()) + "\n");
      }
      for (int i = 0; i < Section.values().length; i++) {//each section
         if (dataElementExists("section-header")) {
            out.write("      " + processMacros(getDataElement("section-header").toString()) + "\n");
         }
         out.write(buildSection(Section.values()[i]));
         if (dataElementExists("section-footer")) {
            out.write("      " + processMacros(getDataElement("section-footer").toString()) + "\n");
         }
      }
      if (dataElementExists("footer")) {
         out.write("    " + processMacros(getDataElement("footer").toString()) + "\n");
      }
      out.write("  </body>\n");
      return out.toString();
   }

   private HashMap<String, ArrayList<Anchor>> getSection(Section s) {
      return anchors.get(s);
   }

   /**
    * Removes the link from the given list.  Calls the save function.
    *
    * @param oldFile The file to remove.
    * @param c The Category name
    * @return true if operation succeeds.
    * @throws java.io.IOException
    */
   private boolean removeLink(RecipeFile oldFile, String c) throws IOException {
      String category = recipejar.StringProcessor.underscoreSpaces(c);
      Section sec;
      if (category.equals("DEFAULT")) {
         sec = Section.parse(oldFile.getName());
      } else {
         sec = Section.parse(category);
      }
      ArrayList<Anchor> theList;
      if (getSection(sec).containsKey(category)) {
         theList = getList(sec, category);
      } else {
         throw new IOException("unable to access category: " + c);
      }
      for (int i = 0; i < theList.size(); i++) {
         if (theList.get(i).equals((Object)oldFile)) {
            theList.remove(i);
            //this is supposed to remove a category if it is empty.
            if (theList.isEmpty() && !category.equals("DEFAULT")) {
               getSection(sec).remove(category);
            }
            this.save();
            return true;
         }
      }
      return false;
   }

   @Override
   protected String getMacroText(String macro) {
      return macro;
   }

   public File lookup(String s) {
      ArrayList<Anchor> list = getList(Section.parse(s), "DEFAULT");
      for(int i=0; i<list.size(); i++){
         if(list.get(i).toString().contains(s)){
            return list.get(i).getSource();
         }
      }
      return null;
   }

   @Override
   protected void prepforSave() {
   }

   @Override
   protected void cleanUpAfterSave() {
   }
public enum Section {

   SECTION_A("A"), SECTION_B("B"), SECTION_C("C"), SECTION_D("D"), SECTION_E("E"), SECTION_F("F"),
   SECTION_G("G"), SECTION_H("H"), SECTION_I("I"), SECTION_J("J"), SECTION_K("K"), SECTION_L("L"),
   SECTION_M("M"), SECTION_N("N"), SECTION_O("O"), SECTION_P("P"), SECTION_Q("Q"), SECTION_R("R"),
   SECTION_S("S"), SECTION_T("T"), SECTION_U("U"), SECTION_V("V"), SECTION_W("W"), SECTION_X("X"),
   SECTION_Y("Y"), SECTION_Z("Z"), SECTION_OTHER("0");

   public static Section parse(int i) {
      return Section.values()[i];
   }

   /**
    *
    * @param s
    * @return The section where the given string would be filed alphabetically.  Section_123 for everything else.
    */
   public static Section parse(String s) {
      for (Section S : Section.values()) {
         if (s.toUpperCase().indexOf(S.id) == 0) {
            return S;
         }
      }
      return SECTION_OTHER;
   }

   private final String id;

   /**
    *
    * @param s
    */
   private Section(String s) {
      id = s;
   }

   public final String getId(){
      return id;
   }

   @Override
   public String toString() {
      return "letter" + id;
   }

}
}
