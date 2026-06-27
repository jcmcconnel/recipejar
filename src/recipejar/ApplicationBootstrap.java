package recipejar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import recipejar.persistence.FileSystemRecipeRepository;
import recipejar.persistence.RecipeRepository;

public class ApplicationBootstrap {

    public static MainFrame bootstrap(String[] argv) {
        Kernel.configDir = new File("%HOME/.RecipeJar");
        if (argv.length > 1) {
            if (argv[0].contains("-d")) {
                Kernel.configDir = new File(argv[1]);
                if (!Kernel.configDir.exists()) {
                    Kernel.configDir.mkdir();
                    try {
                        System.out.println("Unpack");
                        BufferedReader r = new BufferedReader(new InputStreamReader(
                                ClassLoader.getSystemClassLoader().getResourceAsStream("unpackingList.txt")));
                        while (r.ready()) {
                            String line = r.readLine();
                            System.out.println(line);
                            String[] lineComps = line.split(" ");
                            if (lineComps.length == 2) {
                                MainFrame.extractFile(
                                        new File(Kernel.configDir.getAbsolutePath() + "/" + lineComps[1]),
                                        line.split(" ")[0]);
                            } else {
                                new File(Kernel.configDir.getAbsolutePath() + "/" + line).mkdir();
                            }
                        }
                        r.close();
                    } catch (IOException e) {
                        System.out.println("Unpacking failed");
                    }
                }
            }
        }
        System.setProperty("java.util.prefs.userRoot", Kernel.configDir.getAbsolutePath());
        ProgramVariables.DIR_PROGRAM.set(Kernel.configDir.getAbsolutePath() + "/");
        try {
            UIManager.setLookAndFeel(ProgramVariables.LAF.toString());
        } catch (UnsupportedLookAndFeelException e) {
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }

        try {
            recipejar.recipe.Unit.readUnitsFromFile(ProgramVariables.FILE_UNIT.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        RecipeRepository repo = new FileSystemRecipeRepository(ProgramVariables.DIR_DB.toString());

        MainFrame frame = new MainFrame("RecipeJar");
        frame.setRecipeRepository(repo);
        return frame;
    }
}