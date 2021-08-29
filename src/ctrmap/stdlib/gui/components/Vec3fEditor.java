/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap.stdlib.gui.components;

import ctrmap.stdlib.gui.components.listeners.DocumentAdapterEx;
import ctrmap.stdlib.math.vec.Vec3f;
import javax.swing.event.DocumentEvent;

public class Vec3fEditor extends javax.swing.JPanel {

	private Vec3f vec = new Vec3f();

	private boolean allowChanges = false;

	public Vec3fEditor() {
		initComponents();

		ComponentUtils.setNFValueClass(Float.class, x, y, z);

		x.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				if (allowChanges) {
					float val = ComponentUtils.getFloatFromDocument(x);
					if (Math.abs(vec.x - val) >= 0.001f) {
						vec.x = val;
					}
				}
			}
		});
		y.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				if (allowChanges) {
					float val = ComponentUtils.getFloatFromDocument(x);
					if (Math.abs(vec.y - val) >= 0.001f) {
						vec.y = val;
					}
				}
			}
		});
		z.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				if (allowChanges) {
					float val = ComponentUtils.getFloatFromDocument(x);
					if (Math.abs(vec.z - val) >= 0.001f) {
						vec.z = val;
					}
				}
			}
		});
	}

	public void loadVec(Vec3f vec) {
		if (vec == null) {
			this.vec = Vec3f.ZERO();
		} else {
			this.vec = vec;
		}
		refresh();
	}

	public void refresh() {
		allowChanges = false;
		x.setValue(vec.x);
		y.setValue(vec.y);
		z.setValue(vec.z);
		allowChanges = true;
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        xLabel = new javax.swing.JLabel();
        x = new javax.swing.JFormattedTextField();
        y = new javax.swing.JFormattedTextField();
        yLabel = new javax.swing.JLabel();
        z = new javax.swing.JFormattedTextField();
        zLabel = new javax.swing.JLabel();

        xLabel.setForeground(new java.awt.Color(255, 0, 0));
        xLabel.setText("X");

        x.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));

        y.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));

        yLabel.setForeground(new java.awt.Color(0, 153, 0));
        yLabel.setText("Y");

        z.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.000"))));

        zLabel.setForeground(new java.awt.Color(0, 0, 255));
        zLabel.setText("Z");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(xLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(x, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(yLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(y, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(zLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(z, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xLabel)
                    .addComponent(x, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yLabel)
                    .addComponent(y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zLabel)
                    .addComponent(z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField x;
    private javax.swing.JLabel xLabel;
    private javax.swing.JFormattedTextField y;
    private javax.swing.JLabel yLabel;
    private javax.swing.JFormattedTextField z;
    private javax.swing.JLabel zLabel;
    // End of variables declaration//GEN-END:variables
}