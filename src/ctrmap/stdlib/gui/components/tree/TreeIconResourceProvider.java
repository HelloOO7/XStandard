package ctrmap.stdlib.gui.components.tree;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

public class TreeIconResourceProvider {
	private Map<Integer, ImageIcon> iconMap = new HashMap<>();
	
	public void registerResourceIcon(int resID, byte[] data, int size){
		ImageIcon icon = new ImageIcon(data);
		if (size != -1) {
			icon = new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
		}
		registerResourceIcon(resID, icon);
	}
	
	public void registerResourceIcon(int resID, ImageIcon icn){
		iconMap.put(resID, icn);
	}
	
	public ImageIcon getImageIcon(int resID){
		return iconMap.get(resID);
	}
}
