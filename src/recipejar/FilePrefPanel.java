/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FilePrefPanel.java
 *
 * Created on Jul 17, 2011, 10:16:56 AM
 */
package recipejar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import recipejar.ProgramVariables;

/**
 *
 * @author james
 */
public class FilePrefPanel extends javax.swing.JPanel {

   private static JFileChooser fc;
   private ActionListener ac = new ActionListener() {

      public void actionPerformed(ActionEvent e) {
         if (e.getID() == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            ProgramVariables loc;
            if (f.getAbsolutePath().contains(ProgramVariables.DIR_PROGRAM.toString())) {
               int i = f.getAbsolutePath().indexOf(ProgramVariables.DIR_PROGRAM.toString());
               i = i + ProgramVariables.DIR_PROGRAM.toString().length();
               jTextField1.setText(f.getAbsolutePath().substring(i));
               boundPref.set(f.getAbsolutePath().substring(i));
               fc.removeActionListener(ac);
            } else {
               jTextField1.setText(f.getPath());
               boundPref.set(f.getPath());
               fc.removeActionListener(ac);
            }
         }
      }
   };
   private ProgramVariables boundPref;

   public ProgramVariables getBoundPref() {
      return boundPref;
   }

   public void setBoundPref(ProgramVariables boundPref) {
      this.boundPref = boundPref;
      jTextField1.setText(boundPref.toString());
   }

   /** Creates new form FilePrefPanel */
   public FilePrefPanel() {
      initComponents();
   }

   public static void setFileChooser(JFileChooser c) {
      fc = c;
   }

   private void checkFileIntegrity() {
      if (!(new File(ProgramVariables.DIR_PROGRAM.toString() + jTextField1.getText())).exists()
              && !(new File(jTextField1.getText())).exists()) {
         JOptionPane.showMessageDialog(this, "File: " + jTextField1.getText() + " does not exist!", "Whoops!", JOptionPane.ERROR_MESSAGE);
         jTextField1.setText(boundPref.toString());
      }
   }

   /** This method is called from within the constructor to
    * initialize the form.
    */
   @SuppressWarnings("unchecked")
    
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                onFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jTextField1, gridBagConstraints);

        jButton1.setText("Browse...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBrowse(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        add(jButton1, gridBagConstraints);
    }

    private void onBrowse(java.awt.event.ActionEvent evt) {
       System.out.println("Browsing");
       if (fc != null) {
          fc.setMultiSelectionEnabled(false);
          if(boundPref == ProgramVariables.DIR_PROGRAM){
             fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          } else {
             fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
          }
          System.out.println(boundPref.toString());
          File test = new File(boundPref.toString());
          if(test.exists()) System.out.println("Exists: "+test.toString());
          fc.setSelectedFile(test);
          fc.addActionListener(ac);
          fc.showOpenDialog(this.getRootPane());
       }
    }

    private void onFocusLost(java.awt.event.FocusEvent evt) {
       checkFileIntegrity();
    }
    private javax.swing.JButton jButton1;
    private javax.swing.JTextField jTextField1;
}
