/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recipejar;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import javax.swing.JOptionPane;
import recipejar.StringProcessor;
import recipejar.Kernel;

/**
 * Beta
 * @author James McConnel
 */
public enum ProgramVariables {

   //Program state
   CURRENT_MODE("state/mode"),
   LAF("state/lookandfeel"),
   TAB("state/tab"),
   //Search options
   SEARCH_NOTES("state/search_options/notes"),
   SEARCH_TITLES("state/search_options/titles"),
   SEARCH_LABELS("state/search_options/labels"),
   SEARCH_INGREDIENTS("state/search_options/ingredients"),
   SEARCH_PROCEDURES("state/search_options/procedures"),
   //User Info items
   USER_CUSTOM("user/custom"),
   USER_EMAIL("user/email"),
   USER_NAME("user/name"),
   USER_PHONE("user/phone"),
   
   /*External Resources*/
   //Data Files
   FILE_INDEX("files/index"),
   FILE_MACRO("files/macros"),
   FILE_UNIT("files/units"),
   FILE_WELCOME("files/welcome"),
   FILE_CURRENT("files/current"),
   FILE_LAST("files/last"),
   FILE_ERROR("files/error_log"),
   //Directories
   DIR_DB("dirs/database"),
   /**
    * The location where the program stores its files.
    */
   DIR_PROGRAM("dirs/working"),
   /**
    * The directory where program resources are stored 
    */
   DIR_RESOURCE("dirs/resources"),
   //Templates
   TEMPLATE_RECIPE("templates/recipe"),
   TEMPLATE_INDEX("templates/index"),
   //Style sheets
   CSS_INDEX("css/index"),
   CSS_RECIPE("css/recipe"),
   //Other
   HELP_URL("files/help_url");

   public static void unpackToRegistry() {
       for(int i=0; i<ProgramVariables.values().length; i++){
           ProgramVariables.values()[i].set(ProgramVariables.getDefault(ProgramVariables.values()[i]));
       }
   }
   private String key;
   private static String defaultPrefs;
   public static final String CONFIG_SRC = "config.ini";
   private final static String PROGRAM_NODE = "recipejar";

   private ProgramVariables(String s) {
      key = s;
   }

   /**
    * 
    * Uses the config.ini file packed in the jar file to provide default values
    */
   private static String getDefault(ProgramVariables k) {
      try {
         if (defaultPrefs == null) {
            try {
               InputStream in = ClassLoader.getSystemResourceAsStream(CONFIG_SRC);
               defaultPrefs = new String();
               int c = in.read();
               while (c != -1) {
                  defaultPrefs = defaultPrefs + (char) c;
                  c = in.read();
               }
            } catch (IOException e) {
               JOptionPane.showMessageDialog(null, "Error: config.ini, "+e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            } catch (NullPointerException e) {
               JOptionPane.showMessageDialog(null, "Error: config.ini, "+e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
         }

         int begin = defaultPrefs.indexOf(k.name());
         if (begin == -1) {
            throw new java.io.EOFException(k.name()+" :No such default preference defined.");
         }
         begin = begin  + k.name().length() + 1;
         String val = defaultPrefs.substring(begin, defaultPrefs.indexOf("\n", begin)).trim();
         return val;
      } catch (EOFException e) {
         JOptionPane.showMessageDialog(null, "Error: config.ini, "+e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
         //System.exit(1);
         return null;
      }
   }
   
   public static String buildAbsoluteFileNameFrom(String s) {
      return DIR_DB.toString() + StringProcessor.removeBadChars(s.trim()) + ".html";
   }

    /**
     *
     * @return
     */
    public static boolean programNodeExists(){
        try {
            return Preferences.userRoot().nodeExists(PROGRAM_NODE);
        } catch (BackingStoreException ex) {
            return false;
        }
   }

   private String correctPathSep(String s){
       //The call to Matcher forces the system to treat the escape char as a literal.
      String t = s.replaceAll("/", Matcher.quoteReplacement(File.separator));
      t = t.replaceAll(Matcher.quoteReplacement("\\"), Matcher.quoteReplacement(File.separator));
      if(Kernel.isOS("mac")){
          t = t.replaceAll(":", File.separator); 
      }
      return t;
   }
   
   @Override
   public String toString() {
      //see config.ini in the default package

      switch (this) {
         case LAF:
            return Preferences.userRoot().node(PROGRAM_NODE).get(key, recipejar.lib.LAFType.SYSTEM.toString());
         case CURRENT_MODE:
            return Preferences.userRoot().node(PROGRAM_NODE).get(key, recipejar.lib.UIMode.INITIAL_STATE.name());
         case TAB:
            return Preferences.userRoot().node(PROGRAM_NODE).get(key, getDefault(this));

         //Search options
         case SEARCH_TITLES:
         case SEARCH_LABELS:
         case SEARCH_NOTES:
         case SEARCH_INGREDIENTS:
         case SEARCH_PROCEDURES:
            return Preferences.userRoot().node(PROGRAM_NODE).get(key, getDefault(this));

         //User Options
         case USER_CUSTOM:
         case USER_EMAIL:
         case USER_NAME:
         case USER_PHONE:
            return Preferences.userRoot().node(PROGRAM_NODE).get(key, "");


         //Files and directories
         case FILE_INDEX:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, DIR_DB.toString() + getDefault(this)));
         case FILE_MACRO:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, DIR_RESOURCE.toString() + getDefault(this)));
         case FILE_UNIT:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, DIR_RESOURCE.toString() + getDefault(this)));
         case FILE_WELCOME:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, DIR_RESOURCE.toString() + getDefault(this)));
         case FILE_ERROR:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, DIR_PROGRAM.toString() + getDefault(this)));
         
         //Directories
             //TODO fix the recursive function calls.
         case DIR_DB:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, DIR_PROGRAM.toString() + getDefault(this) + File.separator));
         case DIR_PROGRAM:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, getDefaultWorkingPath()));
         case DIR_RESOURCE:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, DIR_PROGRAM.toString() + getDefault(this) + File.separator));
         //Templates
         case TEMPLATE_RECIPE:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, DIR_RESOURCE.toString() + getDefault(this)));
         case TEMPLATE_INDEX:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, DIR_RESOURCE.toString() + getDefault(this)));
         //Style sheets
         case CSS_INDEX:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, DIR_DB.toString() + getDefault(this)));
         case CSS_RECIPE:
            return correctPathSep(Preferences.userRoot().node(PROGRAM_NODE).get(key, DIR_DB.toString() + getDefault(this)));

         //Other
         case HELP_URL:
            return Preferences.userRoot().node(PROGRAM_NODE).get(key, getDefault(this));
         default:
            return Preferences.userRoot().node(PROGRAM_NODE).get(key, null);
      }
   }

   public void set(String s) {
      Preferences.userRoot().node(PROGRAM_NODE).put(key, s);
   }

   public void set(recipejar.lib.LAFType l) {
      if (this.equals(LAF)) {
         set(l.toString());
      }
   }

   public void set(boolean b) {
      Preferences.userRoot().node(PROGRAM_NODE).putBoolean(key, b);
   }

   /**
    * @return The path to the program files
    */
   public static String getDefaultWorkingPath() {
      if (Kernel.isOS("mac os x")) {
         return (System.getProperty("user.home") + "/Library/Application Support/RecipeJar/");
      } else if (Kernel.isOS("windows")) {
         return (System.getProperty("user.home") + "\\.RecipeJar\\");
      } else {    // if (Kernel.isOS("linux")) {
         return (System.getProperty("user.home") + "/.RecipeJar/");
      }
   }
}
