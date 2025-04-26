/*
 * UnitConverterDialog.java
 *
 * Created on Oct 23, 2009, 9:37:36 AM
 */
package recipejar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;

import recipejar.recipe.Unit;

/**
 *
 * @author james
 */
public class UnitConverterDialog extends javax.swing.JDialog {

	private HashMap<String, DefaultComboBoxModel> toComboBoxModels;

   /**
    * Returns the model for a given from unit.
    */
	private DefaultComboBoxModel getComboBoxModelFor(String u) {
		if (toComboBoxModels.containsKey(u)) {
			return toComboBoxModels.get(u);
		} else {
			if (Unit.getUnit(u).hasConversionUnits()) {
            Object[] keys = Unit.getUnit(u).getConversionUnitKeySet().toArray();
            String[] modelLabels = new String[keys.length+1];
            modelLabels[0] = "-- Convert To --";
            for(int i=1; i<modelLabels.length; i++){
               modelLabels[i] = keys[i-1].toString();
            }
				DefaultComboBoxModel m = new DefaultComboBoxModel(modelLabels);
				toComboBoxModels.put(u, m);
				return m;
			} else {
            String[] labels = {"-- No Conversions --"};
				DefaultComboBoxModel m = new DefaultComboBoxModel(labels);
				toComboBoxModels.put(u, m);
				return m;
			}
		}
	}

	private ActionListener fromListnr = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			toUnts.setModel(getComboBoxModelFor(fromUnts.getSelectedItem().toString()));
		}
	};
	private ActionListener toListnr = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			if (!toUnts.getSelectedItem().toString().equals("Convert To...")) {
				quantity.setText(Unit.convert(quantity.getText(), Unit.getUnit(fromUnts.getSelectedItem().toString()),
						Unit.getUnit(toUnts.getSelectedItem().toString())));
				//quantity.selectAll();
            fromUnts.setSelectedItem(toUnts.getSelectedItem());
			}
		}
	};

	/**
	 * Creates new form UnitConverterDialog
	 * 
	 * @param parent
	 * @param modal
	 */
	public UnitConverterDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
      if(ProgramVariables.LAF.toString().equals(recipejar.lib.LAFType.METAL.toString())) this.setUndecorated(true);
      this.getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.PLAIN_DIALOG);
		initComponents();

		toComboBoxModels = new HashMap<String, DefaultComboBoxModel>();

		this.fromUnts.addActionListener(fromListnr);
		this.toUnts.addActionListener(toListnr);
		quantity.addActionListener(toListnr);
	}


	@SuppressWarnings("unchecked")
	private void initComponents() {

		quantity = new javax.swing.JTextField();
		fromUnts = new javax.swing.JComboBox();
		toUnts = new javax.swing.JComboBox();

		setTitle("Unit Converter");

		quantity.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				quantityKeyPressed(evt);
			}
		});


		fromUnts.setModel(new javax.swing.DefaultComboBoxModel(Unit.getConvertableUnitLabels()));
      fromUnts.setSelectedIndex(0);
		fromUnts.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				convertableUntsKeyPressed(evt);
			}
		});

		toUnts.setModel(new javax.swing.DefaultComboBoxModel(
				Unit.getUnit(fromUnts.getSelectedItem().toString()).getConversionUnitKeySet().toArray()));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addComponent(quantity, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(fromUnts, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addComponent(toUnts, 0, 300, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(quantity, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(fromUnts, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(toUnts,
								javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.PREFERRED_SIZE)));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void convertableUntsKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_convertableUntsKeyPressed
		shortcutHandler(evt);
	}// GEN-LAST:event_convertableUntsKeyPressed

	private void quantityKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_quantityKeyPressed
		shortcutHandler(evt);
	}

	private void shortcutHandler(java.awt.event.KeyEvent evt) {
		// if (recipejar.Util.isOS("mac")) {
		// if (evt.isMetaDown()) {
		// if (evt.getKeyCode() == KeyEvent.VK_W) {
		// this.setVisible(false);
		// }
		// }
		// }
	}// GEN-LAST:event_quantityKeyPressed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JComboBox fromUnts;
	private javax.swing.JTextField quantity;
	private javax.swing.JComboBox toUnts;
	// End of variables declaration//GEN-END:variables
}
