/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ctrmap.stdlib.gui.components.tree;

import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 *
 */
public class TreeIconResourceProvider {
	private Map<Integer, ImageIcon> iconMap = new HashMap<>();
	
	public void registerResourceIcon(int resID, byte[] data){
		registerResourceIcon(resID, new ImageIcon(data));
	}
	
	public void registerResourceIcon(int resID, ImageIcon icn){
		iconMap.put(resID, icn);
	}
	
	public ImageIcon getImageIcon(int resID){
		return iconMap.get(resID);
	}
}
