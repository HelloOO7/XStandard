/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ctrmap.stdlib.gui.components.tree;

/**
 *
 */
public class CustomJTreeRootNode extends CustomJTreeNode {
	public CustomJTreeRootNode(TreeIconResourceProvider iconProvider){
		this.iconProvider = iconProvider;
	}

	@Override
	public int getIconResourceID() {
		return -1;
	}

	@Override
	public String getNodeName() {
		return "Root";
	}
}
