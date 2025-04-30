package recipejar.filetypes;

import java.io.IOException;

public class GenericHtmlFile extends recipejar.lib.AbstractXHTMLBasedFile {

   public GenericHtmlFile(String name) throws IOException {
      super(name);
      addToken("body");
      initialize();
   }
   /**
   * All constructors feed into this for common stuff
   *
   * @throws IOException
   */
   private void initialize() throws IOException {
     if (exists()) { //Initialize data structures from file
         this.load();
     } else {
         System.out.println("Generic File does not exist");
     }
   }

   @Override
   protected String buildBody(){
      return super.getDataElement("body").toString();
   }

   @Override
   protected String getMacroText(String macro){ return "";}

   @Override
   protected void prepforSave(){}

   @Override
   protected void cleanUpAfterSave(){}

}
