package xstandard.gui.components.listeners;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 */
public class DnDMouseListener extends MouseAdapter {

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getSource() instanceof JComponent) {
			JComponent c = (JComponent) e.getSource();
			TransferHandler hnd = c.getTransferHandler();
			if (hnd != null){
				hnd.exportAsDrag(c, e, TransferHandler.MOVE);
			}
		}
	}
}
