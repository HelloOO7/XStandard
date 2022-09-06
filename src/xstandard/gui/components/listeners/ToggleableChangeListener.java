package xstandard.gui.components.listeners;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class ToggleableChangeListener extends  AbstractToggleableListener implements ChangeListener {

	@Override
	public void stateChanged(ChangeEvent e) {
		if (getAllowEvents()) {
			onApprovedStateChange(e);
		}
	}
	
	public abstract void onApprovedStateChange(ChangeEvent e);
}
