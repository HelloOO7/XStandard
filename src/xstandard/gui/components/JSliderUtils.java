package xstandard.gui.components;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public class JSliderUtils {

	/*
	* https://stackoverflow.com/questions/23697682/jslider-snapping-to-every-90th-number
	*/
	public static void snapToMouseClick(final JSlider slider) {
		MouseListener[] ml = slider.getMouseListeners();

		for (MouseListener l : ml) {
			slider.removeMouseListener(l);
		}

		MouseMotionListener[] mml = slider.getMouseMotionListeners();

		for (MouseMotionListener l : mml) {
			slider.removeMouseMotionListener(l);
		}

		final BasicSliderUI ui = (BasicSliderUI) slider.getUI();

		BasicSliderUI.TrackListener tl = ui.new TrackListener() {
			//  Position slider at mouse
			@Override
			public void mouseClicked(MouseEvent e) {
				Point p = e.getPoint();
				int value = ui.valueForXPosition(p.x);
				slider.setValue(value);
			}

			// Prevent scrolling while mouse button is held down
			@Override
			public boolean shouldScroll(int dir) {
				return false;
			}
		};

		slider.addMouseListener(tl);
		slider.addMouseMotionListener(tl);
	}

}
