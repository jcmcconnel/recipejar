package recipejar;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;

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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        

        AlphaTab tabbedPane = new AlphaTab(IndexFile.getIndexFile());
        
        readerPane = new CustomTextPane();
        ePanel = new EditorPanel();
        tabbedPane.addHyperlinkListener(readerPane);
        tabbedPane.addHyperlinkListener(ePanel);
                                  
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, readerPane);
        splitPane.setOneTouchExpandable(true);
        this.getContentPane().add(splitPane, BorderLayout.CENTER);

        /*** Action Definitions ***/
        JMenu fileMenu = new JMenu("File");
        // Edit Mode
        Kernel.programActions.put("toggle-edit-mode", new AbstractAction("Edit"){
           public void actionPerformed(ActionEvent e){
              if(splitPane.getRightComponent().equals(ePanel)){
                 splitPane.setRightComponent(readerPane);
                 this.putValue(AbstractAction.NAME, "Edit");
              } else {
                 splitPane.setRightComponent(ePanel);
                 this.putValue(AbstractAction.NAME, "Close");
              }
           }
        });
        fileMenu.add(Kernel.programActions.get("toggle-edit-mode"));

        // Exit 
        Kernel.programActions.put("exit-program", new AbstractAction("Exit"){
           public void actionPerformed(ActionEvent e){
              System.exit(0);
           }
        });
        fileMenu.add(Kernel.programActions.get("exit-program"));
        /*** End Menubar ***/

        // Close window button
        this.addWindowListener(new WindowAdapter(){
           public void windowClosed(WindowEvent e){
              Kernel.programActions.get("exit-program").actionPerformed(new ActionEvent(e.getWindow(), ActionEvent.ACTION_PERFORMED, "Exit"));
           }
        });

        JMenu[] menus = new JMenu[1];
        menus[0] = fileMenu;
        this.setJMenuBar(Kernel.getJMenuBar(menus));
        pack();
    }

    public static void main(String[] argv){
        File configDir = new File("%HOME/.RecipeJar");
        if(argv.length > 1){
            if(argv[0].contains("-d")){
                configDir = new File(argv[1]);
            }
        } 

        recipejar.recipe.Unit.readUnitsFromFile(configDir.getAbsolutePath()+"/settings/units.txt");
        IndexFile.setIndexFileLocation(configDir.getAbsolutePath()+"/Recipes/");

        MainFrame f = new MainFrame("test frame");
        f.setVisible(true);
    }
}
