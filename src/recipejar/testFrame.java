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
        
        recipejar.recipe.Unit.readUnitsFromFile("/home/james/projects/shiny-fortnight/Test/settings/units.txt");
        IndexFile.setIndexFileLocation("/home/james/projects/shiny-fortnight/Test/Recipes/");

        AlphaTab tabbedPane = new AlphaTab(IndexFile.getIndexFile());
        
        readerPane = new rjTextPane();
        ePanel = new EditorPanel();
        tabbedPane.addHyperlinkListener(readerPane);
        tabbedPane.addHyperlinkListener(ePanel);
                                  
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, readerPane);
        splitPane.setOneTouchExpandable(true);
        this.getContentPane().add(splitPane, BorderLayout.CENTER);

        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        AbstractAction action = new AbstractAction("Edit"){
           public void actionPerformed(ActionEvent e){
              if(splitPane.getRightComponent().equals(ePanel)){
                 splitPane.setRightComponent(readerPane);
              } else {
                 splitPane.setRightComponent(ePanel);
              }
           }
        };
        fileMenu.add(action);
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);
        pack();
    }

    public static void main(String[] argv){
        testFrame f = new testFrame("test frame");
        f.setVisible(true);
    }
}
