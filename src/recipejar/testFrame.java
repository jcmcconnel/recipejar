package recipejar;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;

import recipejar.filetypes.IndexFile;

public class testFrame extends JFrame {

    public rjTextPane readerPane;
    public EditorPanel  ePanel;
    public JMenuBar menuBar;
    
    /**
     * Frame initializer.
     */
    public testFrame(String name){
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        

        AlphaTab tabbedPane = new AlphaTab(IndexFile.getIndexFile());
        
        readerPane = new rjTextPane();
        ePanel = new EditorPanel();
        tabbedPane.addHyperlinkListener(readerPane);
        tabbedPane.addHyperlinkListener(ePanel);
                                  
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, readerPane);
        splitPane.setOneTouchExpandable(true);
        this.getContentPane().add(splitPane, BorderLayout.CENTER);

        kernel.programActions.put("toggle-edit-mode", new AbstractAction("Edit"){
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
        kernel.programActions.put("exit-program", new AbstractAction("Exit"){
           public void actionPerformed(ActionEvent e){
              System.exit(0);
           }
        });
        this.addWindowListener(new WindowAdapter(){
           public void windowClosed(WindowEvent e){
              kernel.programActions.get("exit-program").actionPerformed(new ActionEvent(e.getWindow(), ActionEvent.ACTION_PERFORMED, "Exit"));
           }
        });

        this.setJMenuBar(kernel.getJMenuBar());
        pack();
    }

    public static void main(String[] argv){
        String unitsFile = "/home/james/projects/recipejar/Test/settings/units.txt";
        String indexFile = "/home/james/projects/recipejar/Test/settings/units.txt";
        if(argv.length > 1){
        } else {}

        recipejar.recipe.Unit.readUnitsFromFile(unitsFile);
        IndexFile.setIndexFileLocation("/home/james/projects/recipejar/Test/Recipes/");

        testFrame f = new testFrame("test frame");
        f.setVisible(true);
    }
}
