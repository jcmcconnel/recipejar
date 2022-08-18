/*********************************
 * Main frame for the Recipe Jar *
 * James McConnel
 */
package recipejar;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import recipejar.filetypes.IndexFile;

public class MainFrame extends JFrame {

    public CustomTextPane readerPane;
    public EditorPanel  ePanel;
    public JMenuBar menuBar;
    
    /**
     * Frame initializer.
     */
    public MainFrame(String name){
        super(name);

        // Frame configuration
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        

        //Component initialization
        AlphaTab tabbedPane = new AlphaTab(IndexFile.getIndexFile());
        
        readerPane = new CustomTextPane();
        ePanel = new EditorPanel();
        tabbedPane.addHyperlinkListener(readerPane);
        tabbedPane.addHyperlinkListener(ePanel);
                                  
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, readerPane);
        splitPane.setOneTouchExpandable(true);
        this.getContentPane().add(splitPane, BorderLayout.CENTER);

        
        UnitConverterDialog converterDialog = new UnitConverterDialog(this, false);

        /*** Action Definitions ***/
        ArrayList<JMenu> menus = new ArrayList<JMenu>();

        JMenu fileMenu = new JMenu("File");
        // Edit Mode
        Kernel.programActions.put("toggle-edit-mode", new AbstractAction("Open"){
           public void actionPerformed(ActionEvent e){
              if(splitPane.getRightComponent().equals(ePanel)){
                 splitPane.setRightComponent(readerPane);
                 this.putValue(AbstractAction.NAME, "Open");
              } else {
                 splitPane.setRightComponent(ePanel);
                 this.putValue(AbstractAction.NAME, "Close");
              }
           }
        });
        fileMenu.add(Kernel.programActions.get("toggle-edit-mode"));
        menus.add(fileMenu);

        // Exit 
        Kernel.programActions.put("exit-program", new AbstractAction("Exit"){
           public void actionPerformed(ActionEvent e){
              System.exit(0);
           }
        });
        fileMenu.add(Kernel.programActions.get("exit-program"));

        JMenu toolsMenu = new JMenu("Tools");
        Kernel.programActions.put("toggle-converter-dialog", new AbstractAction("Unit Converter"){
                public void actionPerformed(ActionEvent e){
                    converterDialog.setVisible(!converterDialog.isVisible());
                }});
        toolsMenu.add(Kernel.programActions.get("toggle-converter-dialog"));
        menus.add(toolsMenu);

        JMenu helpMenu = new JMenu("Help");
        Kernel.programActions.put("about-dialog", new AbstractAction("About"){
           public void actionPerformed(ActionEvent e){
               JOptionPane.showMessageDialog(helpMenu, "Welcome to RecipeJar!");
           }});
        helpMenu.add(Kernel.programActions.get("about-dialog"));
        menus.add(helpMenu);
        /*** End Menubar ***/

        // Close window button
        this.addWindowListener(new WindowAdapter(){
           public void windowClosed(WindowEvent e){
              Kernel.programActions.get("exit-program").actionPerformed(new ActionEvent(e.getWindow(), ActionEvent.ACTION_PERFORMED, "Exit"));
           }
        });

        JMenu[] menuArray = new JMenu[menus.size()];
        menuArray = menus.toArray(menuArray);
        this.setJMenuBar(Kernel.getJMenuBar(menuArray));
        pack();
    }

    /********** Main ***********/
    public static void main(String[] argv){
        File configDir = new File("%HOME/.RecipeJar");
        if(argv.length > 1){
            if(argv[0].contains("-d")){
                configDir = new File(argv[1]);
            }
        } 

        try {
			recipejar.recipe.Unit.readUnitsFromFile(configDir.getAbsolutePath()+"/settings/units.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        IndexFile.setIndexFileLocation(configDir.getAbsolutePath()+"/Recipes/");

        MainFrame f = new MainFrame("test frame");
        f.setVisible(true);
    }
}
