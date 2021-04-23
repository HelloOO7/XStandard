
package ctrmap.stdlib.gui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
	
	public static void setSystemNativeLookAndFeel(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			Logger.getLogger(ComponentUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
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

	public static float getFloatFromDocument(JFormattedTextField docOwner) {
		return getFloatFromDocument(docOwner, 0.0F);
	}

	public static float getFloatFromDocument(JFormattedTextField docOwner, float defaultValue) {
		try {
			String val = docOwner.getDocument().getText(0, docOwner.getDocument().getLength()).replace(',', '.');
			if (val.length() > 0 && !val.equals("-")) {
				return Float.valueOf(val);
			} else {
				return defaultValue;
			}
		} catch (BadLocationException | NumberFormatException ex) {
			return defaultValue;
		}
	}

}
