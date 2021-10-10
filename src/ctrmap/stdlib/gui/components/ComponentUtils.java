
package ctrmap.stdlib.gui.components;

import ctrmap.stdlib.gui.components.combobox.ACComboBox;
import ctrmap.stdlib.math.vec.Vec3f;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.NumberFormatter;

/**
 *
 */
public class ComponentUtils {
	
	public static void maximize(Frame f){
		f.setExtendedState(Frame.MAXIMIZED_BOTH);
	}
	
	public static void setLookAndFeel(String className) {
		try {
			UIManager.setLookAndFeel(className);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			Logger.getLogger(ComponentUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static void setSystemNativeLookAndFeel(){
		setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}

	public static void setComponentsEnabled(boolean v, Component... components) {
		for (Component c : components) {
			c.setEnabled(v);
			
			if (c instanceof Container){
				setComponentsEnabled(v, ((Container) c).getComponents());
			}
		}
	}

	public static void setNFValueClass(Class<?> clazz, JFormattedTextField... fields) {
		for (int i = 0; i < fields.length; i++) {
			((NumberFormatter) fields[i].getFormatter()).setValueClass(clazz);
		}
	}
	
	public static void setSelectedIndexSafe(JComboBox box, int index) {
		box.setSelectedIndex(Math.min(index, box.getItemCount() - 1));
	}

	public static void clearComponents(Component... components) {
		for (Component c : components) {
			if (c instanceof JLabel) {
				((JLabel) c).setText(null);
			} else if (c instanceof JTextField) {
				((JTextField) c).setText(null);
			} else if (c instanceof JFormattedTextField) {
				((JFormattedTextField) c).setValue(null);
			} else if (c instanceof JComboBox) {
				//((JComboBox) c).removeAllItems();
				((JComboBox) c).setSelectedIndex(-1);
			} else if (c instanceof JCheckBox) {
				((JCheckBox) c).setSelected(false);
			} else if (c instanceof JRadioButton) {
				((JRadioButton) c).setSelected(false);
			} else if (c instanceof ACComboBox) {
				((ACComboBox) c).setSelectedIndex(-1);
			} else if (c instanceof JSpinner) {
				((JSpinner) c).setValue(0);
			}
		}
	}

	public static String getDocTextFromField(JTextField field) {
		try {
			Document d = field.getDocument();
			return d.getText(0, d.getLength());
		} catch (BadLocationException ex) {
			Logger.getLogger(ComponentUtils.class.getName()).log(Level.SEVERE, null, ex);
			return field.getText();
		}
	}
	
	public static void setTFsVector(Vec3f vec, JFormattedTextField x, JFormattedTextField y, JFormattedTextField z){
		x.setValue(vec.x);
		y.setValue(vec.y);
		z.setValue(vec.z);
	}
	
	public static void addChangeListener(ChangeListener listener, JSpinner... spinners){
		for (JSpinner cb : spinners){
			cb.addChangeListener(listener);
		}
	}
	
	public static void addActionListener(ActionListener listener, JComboBox... boxes){
		for (JComboBox cb : boxes){
			cb.addActionListener(listener);
		}
	}
	
	public static void addActionListener(ActionListener listener, JCheckBox... boxes){
		for (JCheckBox cb : boxes){
			cb.addActionListener(listener);
		}
	}
	
	public static void addDocumentListenerToTFs(DocumentListener listener, JTextField... fields){
		for (JTextField f : fields){
			f.getDocument().addDocumentListener(listener);
		}
	}

	public static float getFloatFromDocument(JFormattedTextField docOwner) {
		return getFloatFromDocument(docOwner, ((Number)docOwner.getValue()).floatValue());
	}

	public static float getFloatFromDocument(JFormattedTextField docOwner, float defaultValue) {
		try {
			String val = getDocTextFromField(docOwner).replace(',', '.');
			if (val.length() > 0 && !val.equals("-")) {
				return Float.valueOf(val);
			} else {
				return defaultValue;
			}
		} catch (NumberFormatException ex) {
			return defaultValue;
		}
	}
	
	public static int getIntFromDocument(JFormattedTextField docOwner) {
		return getIntFromDocument(docOwner, ((Number)docOwner.getValue()).intValue());
	}

	public static int getIntFromDocument(JFormattedTextField docOwner, int defaultValue) {
		try {
			String val = getDocTextFromField(docOwner);
			if (val.length() > 0 && !val.equals("-")) {
				return Integer.valueOf(val);
			} else {
				return defaultValue;
			}
		} catch (NumberFormatException ex) {
			return defaultValue;
		}
	}

}
