/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recipejar.data;

/**
 * A data source should contain: getters and setters for the source
 *
 * @author James
 */
public class RecipeTemplate {

    private static AbstractXHTMLBasedFile src = null;

    public static void setSource(AbstractXHTMLBasedFile s) {
        src = s;
    }

    /**
     *
     * Do not make up for deficiencies in other places by initializing the data
     * source here.
     *
     */
    public static AbstractXHTMLBasedFile getSource() {
        return src;
    }

    public void initializeSource() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void finalizeSource() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
