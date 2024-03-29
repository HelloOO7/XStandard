package xstandard.gui.components;

import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.listeners.DocumentAdapterEx;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

public class SliderAndTextField extends javax.swing.JPanel {

	private boolean textFieldIsBusy = false;
		
	/**
	 * Creates new form SliderAndTextField
	 */
	public SliderAndTextField() {
		initComponents();
		
		textField.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				textFieldIsBusy = true;
				float val = ComponentUtils.getIntFromDocument(textField);
				int valClamped = Math.min(slider.getMaximum(), Math.max(slider.getMinimum(), Math.round(val)));
				slider.setValue(valClamped);
				textFieldIsBusy = false;
			}
		});
		
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!textFieldIsBusy) {
					textField.setValue(slider.getValue());
				}
			}
		});
	}
	
	public void addChangeListener(ChangeListener cl) {
		slider.addChangeListener(cl);
	}
	
	public void setMinimum(int value) {
		slider.setMinimum(value);
	}
	
	public void setMaximum(int value) {
		slider.setMaximum(value);
	}
	
	public void setLabelTickSpacing(int value) {
		slider.setMajorTickSpacing(value);
	}
	
	public void setValue(int value) {
		slider.setValue(value);
		textField.setValue(value);
	}

	public int getValue() {
		return slider.getValue();
	}
	
	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textField = new javax.swing.JFormattedTextField();
        slider = new javax.swing.JSlider();

        textField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        textField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        textField.setPreferredSize(new java.awt.Dimension(40, 20));

        slider.setPaintLabels(true);
        slider.setPaintTicks(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(textField, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(slider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(slider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(textField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider slider;
    private javax.swing.JFormattedTextField textField;
    // End of variables declaration//GEN-END:variables
}
