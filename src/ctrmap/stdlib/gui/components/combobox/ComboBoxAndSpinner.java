/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap.stdlib.gui.components.combobox;

import ctrmap.stdlib.text.FormattingUtils;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ComboBoxAndSpinner extends javax.swing.JPanel {

	private boolean allowOutOfBoxValues = true;

	private boolean doChanges = true;
	private List<ChangeListener> listeners = new ArrayList<>();

	public ComboBoxAndSpinner() {
		initComponents();

		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateCBBySpinner();
			}
		});

		comboBox.addListener((Object selectedItem) -> {
			doChanges = false;
			int idx = comboBox.getSelectedIndexEx();
			if (idx != -1) {
				spinner.setValue(comboBox.getSelectedIndexEx());
				for (ChangeListener l : listeners) {
					l.stateChanged(new ChangeEvent(ComboBoxAndSpinner.this));
				}
			}
			doChanges = true;
		});
	}

	private void updateCBBySpinner() {
		if (doChanges) {
			doChanges = false;
			int val = ((Number) spinner.getValue()).intValue();
			if (val < 0 || val >= comboBox.getItemCountEx()) {
				if (allowOutOfBoxValues) {
					comboBox.setSelectedIndexEx(-1);
					comboBox.repaint();
				} else {
					spinner.setValue(comboBox.getSelectedIndexEx());
				}
			} else {
				comboBox.setSelectedIndexEx(val);
			}
			doChanges = true;
		}
	}

	public JComboBox getCB() {
		return comboBox;
	}

	public void setAllowOutOfBoxValues(boolean v) {
		allowOutOfBoxValues = v;
	}

	public boolean getAllowOutOfBoxValues() {
		return allowOutOfBoxValues;
	}

	public void setEditable(boolean v) {
		comboBox.setEditable(v);
	}

	public void setAutoComplete(boolean v) {
		comboBox.setACMode(ComboBoxExInternal.ACMode.CONTAINS);
		comboBox.setAllowAC(true);
	}

	public void loadComboBoxValues(Enum[] enumValues, boolean friendlize) {
		String[] str = new String[enumValues.length];
		for (int i = 0; i < enumValues.length; i++) {
			Enum v = enumValues[i];
			if (friendlize) {
				str[i] = FormattingUtils.getFriendlyEnum(v);
			} else {
				str[i] = v.toString();
			}
		}
		loadComboBoxValues(str);
	}

	public void loadComboBoxValues(String... values) {
		comboBox.loadValues(values);
	}

	public void makeComboBoxValuesInt(int max) {
		makeComboBoxValuesInt(max, null);
	}

	public void makeComboBoxValuesInt(int max, String prefix) {
		if (prefix != null) {
			prefix = prefix + " ";
		} else {
			prefix = "";
		}
		String[] str = new String[max];
		for (int i = 0; i < max; i++) {
			str[i] = prefix + String.valueOf(i);
		}
		loadComboBoxValues(str);
	}

	public void clear() {
		this.comboBox.removeAllItems();
	}

	public void addChangeListener(ChangeListener cl) {
		if (cl != null && !this.listeners.contains(cl)) {
			this.listeners.add(cl);
		}
	}

	public int getValueCB() {
		return comboBox.getSelectedIndexEx();
	}

	public int getValueSpinner() {
		return ((Number) spinner.getValue()).intValue();
	}

	public void setValue(int val) {
		this.spinner.setValue(val);
		updateCBBySpinner();
	}

	public void setMaximumRowCount(int val) {
		this.comboBox.setMaximumRowCount(val);
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        spinner = new javax.swing.JSpinner();
        comboBox = new ctrmap.stdlib.gui.components.combobox.ComboBoxExInternal<>();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(spinner, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spinner)
            .addComponent(comboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ctrmap.stdlib.gui.components.combobox.ComboBoxExInternal<String> comboBox;
    private javax.swing.JSpinner spinner;
    // End of variables declaration//GEN-END:variables
}
