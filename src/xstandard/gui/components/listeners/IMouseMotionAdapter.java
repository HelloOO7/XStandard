package xstandard.gui.components.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public interface IMouseMotionAdapter extends MouseMotionListener {

	@Override
	public default void mouseDragged(MouseEvent e) {
	}

	@Override
	public default void mouseMoved(MouseEvent e) {
	}
}
