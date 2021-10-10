package ctrmap.stdlib.gui.components.tree;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

public class CustomJTreeMultiCellRenderer extends DefaultTreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (value instanceof CustomJTreeNode) {
			CustomJTreeNode b = (CustomJTreeNode) value;
			b.loadRenderingComponents();
			TreeCellRenderer rnd = b.getTreeCellComponent();
			return rnd.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
		return super.getTreeCellRendererComponent(tree, value, leaf, expanded, leaf, row, hasFocus);
	}

}
