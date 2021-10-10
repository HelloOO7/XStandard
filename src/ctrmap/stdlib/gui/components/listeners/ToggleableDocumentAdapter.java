
package ctrmap.stdlib.gui.components.listeners;

import javax.swing.event.DocumentEvent;

public abstract class ToggleableDocumentAdapter extends AbstractToggleableListener implements DocumentAdapterEx {

	public abstract void textChangedUpdateImpl(DocumentEvent e);
	
	@Override
	public void textChangedUpdate(DocumentEvent e) {
		if (getAllowEvents()) {
			textChangedUpdateImpl(e);
		}
	}

}
