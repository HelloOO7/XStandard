package ctrmap.stdlib.gui.components.tree;

import java.awt.event.MouseEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public abstract class CustomJTreeNode extends DefaultMutableTreeNode {

	private CustomJTreeCellRenderer renderer;
	protected TreeIconResourceProvider iconProvider;

	public CustomJTreeNode() {
		this(new DefaultCustomJTreeCellRenderer());
	}

	public CustomJTreeNode(CustomJTreeCellRenderer rnd) {
		setTreeCellComponent(rnd);
	}

	protected final void setTreeCellComponent(CustomJTreeCellRenderer rnd) {
		renderer = rnd;
	}

	public CustomJTreeCellRenderer getTreeCellComponent() {
		return renderer;
	}

	public abstract int getIconResourceID();

	public abstract String getNodeName();
	
	@Override
	public String toString() {
		return getClass() + ":" + getNodeName();
	}

	public void onNodeSelected() {

	}
	
	public void onNodeDeselected() {

	}

	public void onNodePopupInvoke(MouseEvent e) {

	}

	public void updateCellUI() {
		loadRenderingComponents();
	}

	@Override
	public void setParent(MutableTreeNode parent) {
		if (parent instanceof CustomJTreeNode) {
			iconProvider = ((CustomJTreeNode) parent).iconProvider;
		}
		super.setParent(parent);
		for (int ch = 0; ch < getChildCount(); ch++) {
			TreeNode n = getChildAt(ch);
			if (n instanceof CustomJTreeNode) {
				((CustomJTreeNode) n).setParent(this);
			}
		}
	}

	void loadRenderingComponents() {
		renderer.setText(getNodeName());
		if (iconProvider == null) {
			throw new NullPointerException("Icon provider not set !! " + getNodeName());
		}
		renderer.setIcon(iconProvider.getImageIcon(getIconResourceID()));
	}
}
