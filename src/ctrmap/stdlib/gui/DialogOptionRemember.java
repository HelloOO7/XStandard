package ctrmap.stdlib.gui;

import ctrmap.stdlib.CMStdLibPrefs;
import java.util.Enumeration;
import java.util.Objects;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;

public class DialogOptionRemember {

	private static final Preferences PREFS_RBTN = CMStdLibPrefs.node("RadioButtonRemember");
	private static final Preferences PREFS_CHECKBOX = CMStdLibPrefs.node("CheckBoxRemember");
	private static final Preferences PREFS_SPINNER = CMStdLibPrefs.node("SpinnerRemember");

	public static void setRememberedSpinnerInt(JSpinner spinner, String key) {
		if (key != null) {
			if (PREFS_SPINNER.get(key, null) != null) {
				spinner.setValue(PREFS_SPINNER.getInt(key, 0));
			}
		}
	}
	
	public static void putRememberedSpinner(JSpinner spinner, String key) {
		if (key != null){
			PREFS_SPINNER.put(key, String.valueOf(spinner.getValue()));
		}
	}

	public static void setRememberedCheckbox(JCheckBox cb) {
		if (cb.getText() != null) {
			cb.setSelected(PREFS_CHECKBOX.getBoolean(cb.getText(), cb.isSelected()));
		}
	}

	public static void putRememberedCheckbox(JCheckBox cb) {
		if (cb.getText() != null) {
			PREFS_CHECKBOX.putBoolean(cb.getText(), cb.isSelected());
		}
	}

	public static void selectRememberedRBtnPos(ButtonGroup group) {
		int pos = PREFS_RBTN.getInt(String.valueOf(getButtonGroupHash(group)), 0);

		Enumeration<AbstractButton> e = group.getElements();

		int index = 0;

		while (e.hasMoreElements()) {
			AbstractButton btn = e.nextElement();

			if (index == pos) {
				group.setSelected(btn.getModel(), true);
				break;
			}

			index++;
		}
	}

	public static void putRememberedRBtnPos(ButtonGroup group) {
		Enumeration<AbstractButton> e = group.getElements();

		int index = 0;

		while (e.hasMoreElements()) {
			AbstractButton btn = e.nextElement();

			if (btn.getModel() == group.getSelection()) {
				PREFS_RBTN.putInt(String.valueOf(getButtonGroupHash(group)), index);
				break;
			}
			index++;
		}
	}

	private static int getButtonGroupHash(ButtonGroup group) {
		Enumeration<AbstractButton> e = group.getElements();

		int hash = 7;

		while (e.hasMoreElements()) {
			AbstractButton btn = e.nextElement();

			hash += 37 * hash + Objects.hashCode(btn.getText());
		}

		return hash;
	}
}
