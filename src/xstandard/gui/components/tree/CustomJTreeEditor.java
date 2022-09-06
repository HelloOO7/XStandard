package xstandard.gui.components.tree;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

public class CustomJTreeEditor extends AbstractCellEditor implements TreeCellEditor {

	private CustomJTree tree;

	public CustomJTreeEditor(CustomJTree tree) {
		this.tree = tree;
	}

	@Override
	public Object getCellEditorValue() {
		TreePath selPath = tree.getSelectionPath();
		if (selPath != null) {
			Object obj = tree.getSelectionPath().getLastPathComponent();
			if (obj != null) {
				if (obj instanceof CustomJTreeNode) {
					return ((CustomJTreeNode) obj).getNodeName();
				}
			}
		}
		return null;
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		if (value instanceof CustomJTreeNode) {
			//IDK : The editor never has the isSelected param set until the 2nd click.
			//Making it always true will fix the highlight being absent and shouldn't break anything since the non-editor
			//method will be used if the object is not being edited
			CustomJTreeNode n = (CustomJTreeNode) value;
			n.loadRenderingComponents();
			return n.getTreeCellComponent().getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, false);
		}
		return new JLabel(String.valueOf(value));
	}

	@Override
	public boolean isCellEditable(EventObject e) {
		if (!(e instanceof MouseEvent)) {
			return false;
		}

		MouseEvent mouseEvent = (MouseEvent) e;

		TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());

		if (path == null) {
			return false;
		}

		Object node = path.getLastPathComponent();
		if (node instanceof CustomJTreeNode) {
			CustomJTreeNode customNode = (CustomJTreeNode) node;
			return customNode.getTreeCellComponent().isEditable(mouseEvent);
		}
		return false;
	}
}
