package ctrmap.stdlib.gui.components.listeners;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class ToggleableChangeListener implements ChangeListener {

	public boolean enabled;

	public void setEnabled(boolean v) {
		enabled = v;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (enabled) {
			onApprovedStateChange(e);
		}
	}
	
	public abstract void onApprovedStateChange(ChangeEvent e);
}
