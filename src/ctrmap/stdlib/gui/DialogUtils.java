package ctrmap.stdlib.gui;

import java.awt.Component;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 */
public class DialogUtils {

	public static int getSelectedNumber(JFrame parent, String title, int initialValue, int minimum, int maximum){
		NumberInputDialog dlg = new NumberInputDialog(parent, true, title, initialValue, minimum, maximum);
		dlg.setLocationRelativeTo(parent);
		dlg.setVisible(true);
		boolean isResult = dlg.getConfirmed();
		if (isResult){
			return dlg.getResult();
		}
		return initialValue;
	}
	
	public static void showErrorMessage(String title, String message) {
		showErrorMessage(null, title, message);
	}
	
	public static void showErrorMessage(Component parent, String title, String message) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showInfoMessage(String title, String message) {
		showInfoMessage(null, title, message);
	}
	
	public static void showInfoMessage(Component parent, String title, String message) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void showWarningMessage(Component parent, String title, String message) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
	}
	
	public static boolean showYesNoDialog(String title, String message){
		return showYesNoDialog(null, title, message);
	}
	
	public static boolean showYesNoDialog(Component parent, String title, String message){
		return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	public static int showSaveConfirmationDialog(Component parent, String changeSubject) {
		return JOptionPane.showConfirmDialog(parent, changeSubject + " has been modified. Do you want to keep the changes?", "Save changes", JOptionPane.YES_NO_CANCEL_OPTION);
	}

	public static int showSaveConfirmationDialog(String changeSubject) {
		return showSaveConfirmationDialog(null, changeSubject);
	}

	public static void showExceptionTraceDialog(Exception ex) {
		ex.printStackTrace();
		StringWriter writer = new StringWriter();
		if (ex.getCause() != null) {
			ex.getCause().printStackTrace(new PrintWriter(writer));
		}
		try {
			writer.close();
			DialogUtils.showErrorMessage("Fatal error - contact a dev", writer.toString());
		} catch (IOException ex1) {
			Logger.getLogger(DialogUtils.class.getName()).log(Level.SEVERE, null, ex1);
		}
	}
}
