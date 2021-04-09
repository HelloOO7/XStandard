
package ctrmap.stdlib.gui.components.listeners;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class DocumentAdapterEx implements DocumentListener {

	@Override
	public void insertUpdate(DocumentEvent e) {
		textChangedUpdate(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		textChangedUpdate(e);
	}
	
	@Override
	public void changedUpdate(DocumentEvent e){
		
	}
	
	public abstract void textChangedUpdate(DocumentEvent e);
}
