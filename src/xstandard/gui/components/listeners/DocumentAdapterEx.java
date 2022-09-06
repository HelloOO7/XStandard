
package xstandard.gui.components.listeners;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public interface DocumentAdapterEx extends DocumentListener {

	@Override
	public default void insertUpdate(DocumentEvent e) {
		textChangedUpdate(e);
	}

	@Override
	public default void removeUpdate(DocumentEvent e) {
		textChangedUpdate(e);
	}
	
	@Override
	public default void changedUpdate(DocumentEvent e){
		
	}
	
	public void textChangedUpdate(DocumentEvent e);
}
