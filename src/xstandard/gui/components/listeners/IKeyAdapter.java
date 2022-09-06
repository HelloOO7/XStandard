package xstandard.gui.components.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public interface IKeyAdapter extends KeyListener {

	@Override
	public default void keyTyped(KeyEvent e) {
		
	}
	
	@Override
	public default void keyPressed(KeyEvent e) {
		
	}
	
	@Override
	public default void keyReleased(KeyEvent e) {
		
	}
}
