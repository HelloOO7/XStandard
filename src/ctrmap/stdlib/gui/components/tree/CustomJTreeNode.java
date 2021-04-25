/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap.stdlib.gui.components.tree;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 */
public abstract class CustomJTreeNode extends DefaultMutableTreeNode {

	private JLabel label = new JLabel();
	protected TreeIconResourceProvider iconProvider;

	public CustomJTreeNode() {

	}

	public abstract int getIconResourceID();

	public abstract String getNodeName();
	
	public void onNodeSelected(){
		
	}

	@Override
	public void setParent(MutableTreeNode parent) {
		if (parent instanceof CustomJTreeNode) {
			iconProvider = ((CustomJTreeNode) parent).iconProvider;
			if (iconProvider != null) {
				loadRenderingComponents();
			}
		}
		super.setParent(parent);
		for (int ch = 0; ch < getChildCount(); ch++){
			TreeNode n = getChildAt(ch);
			if (n instanceof CustomJTreeNode){
				((CustomJTreeNode) n).setParent(this);
			}
		}
	}

	private void loadRenderingComponents() {
		label.setText(getNodeName());
		label.setIcon(iconProvider.getImageIcon(getIconResourceID()));
	}

	public static class CellRenderer extends DefaultTreeCellRenderer {

		public CellRenderer() {
			super();
		}

		@Override
		public Component getTreeCellRendererComponent(
				JTree tree,
				Object value,
				boolean sel,
				boolean expanded,
				boolean leaf, int row,
				boolean hasFocus) {
			this.hasFocus = hasFocus;
			this.selected = sel;
			if (value instanceof CustomJTreeNode) {
				CustomJTreeNode b = (CustomJTreeNode) value;
				setText(b.label.getText());
				setForeground(sel ? getTextSelectionColor() : getTextNonSelectionColor());
				setIcon(b.label.getIcon());
				setComponentOrientation(tree.getComponentOrientation());
			}
			return this;
		}
	}
}
