
package ctrmap.stdlib.gui.components.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
public abstract class ToggleableActionListener extends AbstractToggleableListener implements ActionListener {
	
	public abstract void actionPerformedImpl(ActionEvent e);

	@Override
	public void actionPerformed(ActionEvent e) {
		if (getAllowEvents()) {
			actionPerformedImpl(e);
		}
	}

}
