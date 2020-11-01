package recipejar;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;

public class testFrame extends JFrame {

    public rjTextPane readerPane;
    public testFrame(String name){
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        recipejar.filetypes.IndexFile indexSource = new recipejar.filetypes.IndexFile("/home/james/projects/shiny-fortnight/Test/Recipes/index.html");
        AlphaTab tabbedPane = new AlphaTab(indexSource);
        readerPane = new rjTextPane();
        tabbedPane.addHyperlinkListener(readerPane);
                                  
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, readerPane);
        splitPane.setOneTouchExpandable(true);
        this.getContentPane().add(splitPane, BorderLayout.CENTER);
        pack();
    }
    public static void main(String[] argv){
        testFrame f = new testFrame("test frame");
        f.setVisible(true);
    }
}
