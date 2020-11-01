/*
 * AlphaTab.java
 *
 * Created on September 19, 2008, 6:57 AM
 */
package recipejar;

import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import recipejar.filetypes.IndexFile;

/**
 *
 * @author James McConnel
 */
public class AlphaTab extends JTabbedPane {

    private StyleSheet css;
    private IndexFile index;

    /**
     *
     */
    public AlphaTab(IndexFile s) {
        index = s;
        initComponents();
        setPreferredSize(new java.awt.Dimension(500, 500));
    }

    public void setStyle(FileReader in) {
        css = new StyleSheet();
        try {
            css.loadRules(in, null);
        } catch (IOException ex) {
            Logger.getLogger(AlphaTab.class.getName()).log(Level.SEVERE, null, ex);
        }
        setupDocument(this.getSelectedTextPane());
    }

    private void setupDocument(JTextPane p) {
        if (css != null) {
            p.setContentType("text/html");
            HTMLDocument doc = (HTMLDocument) p.getEditorKit().createDefaultDocument();
            doc.getStyleSheet().addStyleSheet(css);
            p.setDocument(doc);
        }
        try{
         p.setText(index.buildSection(this.getSelectedIndex()));
        } catch(java.lang.NullPointerException e) {
            if(p == null) System.out.print("pane");   
            if(index == null) System.out.print("index");   
        }
    }

    /**
     *
     * @param l
     */
    @Override
    public void addKeyListener(KeyListener l) {
        super.addKeyListener(l);
        jTextPane1.addKeyListener(l);
        jTextPane2.addKeyListener(l);
        jTextPane3.addKeyListener(l);
        jTextPane4.addKeyListener(l);
        jTextPane5.addKeyListener(l);
        jTextPane6.addKeyListener(l);
        jTextPane7.addKeyListener(l);
        jTextPane8.addKeyListener(l);
        jTextPane9.addKeyListener(l);
        jTextPane10.addKeyListener(l);
        jTextPane11.addKeyListener(l);
        jTextPane12.addKeyListener(l);
        jTextPane13.addKeyListener(l);
        jTextPane14.addKeyListener(l);
        jTextPane15.addKeyListener(l);
        jTextPane16.addKeyListener(l);
        jTextPane17.addKeyListener(l);
        jTextPane18.addKeyListener(l);
        jTextPane19.addKeyListener(l);
        jTextPane20.addKeyListener(l);
        jTextPane21.addKeyListener(l);
        jTextPane22.addKeyListener(l);
        jTextPane23.addKeyListener(l);
        jTextPane24.addKeyListener(l);
        jTextPane25.addKeyListener(l);
        jTextPane26.addKeyListener(l);
        jTextPane27.addKeyListener(l);
    }

    public void addHyperlinkListener(HyperlinkListener Listener) {
        jTextPane1.addHyperlinkListener(Listener);
        jTextPane2.addHyperlinkListener(Listener);
        jTextPane3.addHyperlinkListener(Listener);
        jTextPane4.addHyperlinkListener(Listener);
        jTextPane5.addHyperlinkListener(Listener);
        jTextPane6.addHyperlinkListener(Listener);
        jTextPane7.addHyperlinkListener(Listener);
        jTextPane8.addHyperlinkListener(Listener);
        jTextPane9.addHyperlinkListener(Listener);
        jTextPane10.addHyperlinkListener(Listener);
        jTextPane11.addHyperlinkListener(Listener);
        jTextPane12.addHyperlinkListener(Listener);
        jTextPane13.addHyperlinkListener(Listener);
        jTextPane14.addHyperlinkListener(Listener);
        jTextPane15.addHyperlinkListener(Listener);
        jTextPane16.addHyperlinkListener(Listener);
        jTextPane17.addHyperlinkListener(Listener);
        jTextPane18.addHyperlinkListener(Listener);
        jTextPane19.addHyperlinkListener(Listener);
        jTextPane20.addHyperlinkListener(Listener);
        jTextPane21.addHyperlinkListener(Listener);
        jTextPane22.addHyperlinkListener(Listener);
        jTextPane23.addHyperlinkListener(Listener);
        jTextPane24.addHyperlinkListener(Listener);
        jTextPane25.addHyperlinkListener(Listener);
        jTextPane26.addHyperlinkListener(Listener);
        jTextPane27.addHyperlinkListener(Listener);
    }

    /**
     * Sets the tab of this alphabetical tab pane to the appropriate tab given
     * the character input c. If c is not a letter it defaults to the last tab.
     *
     * @param c
     */
    public void setSelectedTab(char c) {
        if (Character.isLetter(c)) {
            this.setSelectedIndex((Character.toUpperCase(c) - 'A'));
        } else {
            this.setSelectedIndex(this.getTabCount() - 1);
        }
        setupDocument(this.getSelectedTextPane());
    }

    public final javax.swing.JTextPane getSelectedTextPane() {
        switch (this.getSelectedIndex() + 1) {
            case 1:
                return jTextPane1;
            case 2:
                return jTextPane2;
            case 3:
                return jTextPane3;
            case 4:
                return jTextPane4;
            case 5:
                return jTextPane5;
            case 6:
                return jTextPane6;
            case 7:
                return jTextPane7;
            case 8:
                return jTextPane8;
            case 9:
                return jTextPane9;
            case 10:
                return jTextPane10;
            case 11:
                return jTextPane11;
            case 12:
                return jTextPane12;
            case 13:
                return jTextPane13;
            case 14:
                return jTextPane14;
            case 15:
                return jTextPane15;
            case 16:
                return jTextPane16;
            case 17:
                return jTextPane17;
            case 18:
                return jTextPane18;
            case 19:
                return jTextPane19;
            case 20:
                return jTextPane20;
            case 21:
                return jTextPane21;
            case 22:
                return jTextPane22;
            case 23:
                return jTextPane23;
            case 24:
                return jTextPane24;
            case 25:
                return jTextPane25;
            case 26:
                return jTextPane26;
            case 27:
                return jTextPane27;
            default:
                return null;
        }
    }

    public void scrollToReference(String r) {
        String ref;
        if (r.contains("#")) {
            ref = r.split("#")[1];
        } else {
            ref = r;
        }
        this.setSelectedTab(ref.trim().charAt(0));
        JTextPane p = this.getSelectedTextPane();
        HTMLDocument d = (HTMLDocument) p.getDocument();
        Element e;
        e = d.getElement(ref);
        if (e != null) {
            p.setCaretPosition(e.getStartOffset());
            try {
                Rectangle2D r1 = p.modelToView2D(e.getStartOffset());
                Rectangle whatIWant = new Rectangle(Double.valueOf(r1.getX()).intValue(), Double.valueOf(r1.getY()).intValue(), Double.valueOf(r1.getWidth()).intValue(), p.getVisibleRect().height);
                p.scrollRectToVisible(whatIWant);
            } catch (BadLocationException ex) {
            }
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane3 = new javax.swing.JTextPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextPane4 = new javax.swing.JTextPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextPane5 = new javax.swing.JTextPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextPane6 = new javax.swing.JTextPane();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTextPane7 = new javax.swing.JTextPane();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTextPane8 = new javax.swing.JTextPane();
        jScrollPane9 = new javax.swing.JScrollPane();
        jTextPane9 = new javax.swing.JTextPane();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTextPane10 = new javax.swing.JTextPane();
        jScrollPane11 = new javax.swing.JScrollPane();
        jTextPane11 = new javax.swing.JTextPane();
        jScrollPane12 = new javax.swing.JScrollPane();
        jTextPane12 = new javax.swing.JTextPane();
        jScrollPane13 = new javax.swing.JScrollPane();
        jTextPane13 = new javax.swing.JTextPane();
        jScrollPane14 = new javax.swing.JScrollPane();
        jTextPane14 = new javax.swing.JTextPane();
        jScrollPane15 = new javax.swing.JScrollPane();
        jTextPane15 = new javax.swing.JTextPane();
        jScrollPane16 = new javax.swing.JScrollPane();
        jTextPane16 = new javax.swing.JTextPane();
        jScrollPane17 = new javax.swing.JScrollPane();
        jTextPane17 = new javax.swing.JTextPane();
        jScrollPane18 = new javax.swing.JScrollPane();
        jTextPane18 = new javax.swing.JTextPane();
        jScrollPane19 = new javax.swing.JScrollPane();
        jTextPane19 = new javax.swing.JTextPane();
        jScrollPane20 = new javax.swing.JScrollPane();
        jTextPane20 = new javax.swing.JTextPane();
        jScrollPane21 = new javax.swing.JScrollPane();
        jTextPane21 = new javax.swing.JTextPane();
        jScrollPane22 = new javax.swing.JScrollPane();
        jTextPane22 = new javax.swing.JTextPane();
        jScrollPane23 = new javax.swing.JScrollPane();
        jTextPane23 = new javax.swing.JTextPane();
        jScrollPane24 = new javax.swing.JScrollPane();
        jTextPane24 = new javax.swing.JTextPane();
        jScrollPane25 = new javax.swing.JScrollPane();
        jTextPane25 = new javax.swing.JTextPane();
        jScrollPane26 = new javax.swing.JScrollPane();
        jTextPane26 = new javax.swing.JTextPane();
        jScrollPane27 = new javax.swing.JScrollPane();
        jTextPane27 = new javax.swing.JTextPane();

        setTabPlacement(javax.swing.JTabbedPane.LEFT);
        setMaximumSize(new java.awt.Dimension(300, 600));
        setMinimumSize(new java.awt.Dimension(100, 300));
        setPreferredSize(new java.awt.Dimension(250, 500));
        addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                onTabSelection(evt);
            }
        });

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane1.setContentType("text/html"); // NOI18N
        jTextPane1.setEditable(false);
        jTextPane1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane1.setViewportView(jTextPane1);

        addTab("A", jScrollPane1);

        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane2.setContentType("text/html"); // NOI18N
        jTextPane2.setEditable(false);
        jTextPane2.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                fireOpenEvent(evt);
            }
        });
        jTextPane2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane2.setViewportView(jTextPane2);

        addTab("B", jScrollPane2);

        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane3.setContentType("text/html"); // NOI18N
        jTextPane3.setEditable(false);
        jTextPane3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane3.setViewportView(jTextPane3);

        addTab("C", jScrollPane3);

        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane4.setContentType("text/html"); // NOI18N
        jTextPane4.setEditable(false);
        jTextPane4.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane4.setViewportView(jTextPane4);

        addTab("D", jScrollPane4);

        jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane5.setContentType("text/html"); // NOI18N
        jTextPane5.setEditable(false);
        jTextPane5.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane5.setViewportView(jTextPane5);

        addTab("E", jScrollPane5);

        jScrollPane6.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane6.setContentType("text/html"); // NOI18N
        jTextPane6.setEditable(false);
        jTextPane6.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane6.setViewportView(jTextPane6);

        addTab("F", jScrollPane6);

        jScrollPane7.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane7.setContentType("text/html"); // NOI18N
        jTextPane7.setEditable(false);
        jTextPane7.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane7.setViewportView(jTextPane7);

        addTab("G", jScrollPane7);

        jScrollPane8.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane8.setContentType("text/html"); // NOI18N
        jTextPane8.setEditable(false);
        jTextPane8.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane8.setViewportView(jTextPane8);

        addTab("H", jScrollPane8);

        jScrollPane9.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane9.setContentType("text/html"); // NOI18N
        jTextPane9.setEditable(false);
        jTextPane9.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane9.setViewportView(jTextPane9);

        addTab("I", jScrollPane9);

        jScrollPane10.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane10.setContentType("text/html"); // NOI18N
        jTextPane10.setEditable(false);
        jTextPane10.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane10.setViewportView(jTextPane10);

        addTab("J", jScrollPane10);

        jScrollPane11.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane11.setContentType("text/html"); // NOI18N
        jTextPane11.setEditable(false);
        jTextPane11.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane11.setViewportView(jTextPane11);

        addTab("K", jScrollPane11);

        jScrollPane12.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane12.setContentType("text/html"); // NOI18N
        jTextPane12.setEditable(false);
        jTextPane12.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane12.setViewportView(jTextPane12);

        addTab("L", jScrollPane12);

        jScrollPane13.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane13.setContentType("text/html"); // NOI18N
        jTextPane13.setEditable(false);
        jTextPane13.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane13.setViewportView(jTextPane13);

        addTab("M", jScrollPane13);

        jScrollPane14.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane14.setContentType("text/html"); // NOI18N
        jTextPane14.setEditable(false);
        jTextPane14.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane14.setViewportView(jTextPane14);

        addTab("N", jScrollPane14);

        jScrollPane15.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane15.setContentType("text/html"); // NOI18N
        jTextPane15.setEditable(false);
        jTextPane15.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane15.setViewportView(jTextPane15);

        addTab("O", jScrollPane15);

        jScrollPane16.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane16.setContentType("text/html"); // NOI18N
        jTextPane16.setEditable(false);
        jTextPane16.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane16.setViewportView(jTextPane16);

        addTab("P", jScrollPane16);

        jScrollPane17.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane17.setContentType("text/html"); // NOI18N
        jTextPane17.setEditable(false);
        jTextPane17.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane17.setViewportView(jTextPane17);

        addTab("Q", jScrollPane17);

        jScrollPane18.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane18.setContentType("text/html"); // NOI18N
        jTextPane18.setEditable(false);
        jTextPane18.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane18.setViewportView(jTextPane18);

        addTab("R", jScrollPane18);

        jScrollPane19.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane19.setContentType("text/html"); // NOI18N
        jTextPane19.setEditable(false);
        jTextPane19.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane19.setViewportView(jTextPane19);

        addTab("S", jScrollPane19);

        jScrollPane20.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane20.setContentType("text/html"); // NOI18N
        jTextPane20.setEditable(false);
        jTextPane20.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane20.setViewportView(jTextPane20);

        addTab("T", jScrollPane20);

        jScrollPane21.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane21.setContentType("text/html"); // NOI18N
        jTextPane21.setEditable(false);
        jTextPane21.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane21.setViewportView(jTextPane21);

        addTab("U", jScrollPane21);

        jScrollPane22.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane22.setContentType("text/html"); // NOI18N
        jTextPane22.setEditable(false);
        jTextPane22.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane22.setViewportView(jTextPane22);

        addTab("V", jScrollPane22);

        jScrollPane23.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane23.setContentType("text/html"); // NOI18N
        jTextPane23.setEditable(false);
        jTextPane23.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane23.setViewportView(jTextPane23);

        addTab("W", jScrollPane23);

        jScrollPane24.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane24.setContentType("text/html"); // NOI18N
        jTextPane24.setEditable(false);
        jTextPane24.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane24.setViewportView(jTextPane24);

        addTab("X", jScrollPane24);

        jScrollPane25.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane25.setContentType("text/html"); // NOI18N
        jTextPane25.setEditable(false);
        jTextPane25.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane25.setViewportView(jTextPane25);

        addTab("Y", jScrollPane25);

        jScrollPane26.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane26.setContentType("text/html"); // NOI18N
        jTextPane26.setEditable(false);
        jTextPane26.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane26.setViewportView(jTextPane26);

        addTab("Z", jScrollPane26);

        jScrollPane27.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTextPane27.setContentType("text/html"); // NOI18N
        jTextPane27.setEditable(false);
        jTextPane27.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                gotFocus(evt);
            }
        });
        jScrollPane27.setViewportView(jTextPane27);

        addTab("123", jScrollPane27);
    }// </editor-fold>//GEN-END:initComponents

   private void gotFocus(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_gotFocus
       JTextPane p = (JTextPane) evt.getSource();
       setupDocument(p);
       p.setCaretPosition(0);
   }//GEN-LAST:event_gotFocus

   private void fireOpenEvent(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_fireOpenEvent

   }//GEN-LAST:event_fireOpenEvent

   private void onTabSelection(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_onTabSelection
       JTextPane p = this.getSelectedTextPane();
       setupDocument(p);
       p.setCaretPosition(0);
   }//GEN-LAST:event_onTabSelection
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane23;
    private javax.swing.JScrollPane jScrollPane24;
    private javax.swing.JScrollPane jScrollPane25;
    private javax.swing.JScrollPane jScrollPane26;
    private javax.swing.JScrollPane jScrollPane27;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane10;
    private javax.swing.JTextPane jTextPane11;
    private javax.swing.JTextPane jTextPane12;
    private javax.swing.JTextPane jTextPane13;
    private javax.swing.JTextPane jTextPane14;
    private javax.swing.JTextPane jTextPane15;
    private javax.swing.JTextPane jTextPane16;
    private javax.swing.JTextPane jTextPane17;
    private javax.swing.JTextPane jTextPane18;
    private javax.swing.JTextPane jTextPane19;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JTextPane jTextPane20;
    private javax.swing.JTextPane jTextPane21;
    private javax.swing.JTextPane jTextPane22;
    private javax.swing.JTextPane jTextPane23;
    private javax.swing.JTextPane jTextPane24;
    private javax.swing.JTextPane jTextPane25;
    private javax.swing.JTextPane jTextPane26;
    private javax.swing.JTextPane jTextPane27;
    private javax.swing.JTextPane jTextPane3;
    private javax.swing.JTextPane jTextPane4;
    private javax.swing.JTextPane jTextPane5;
    private javax.swing.JTextPane jTextPane6;
    private javax.swing.JTextPane jTextPane7;
    private javax.swing.JTextPane jTextPane8;
    private javax.swing.JTextPane jTextPane9;
    // End of variables declaration//GEN-END:variables
}
