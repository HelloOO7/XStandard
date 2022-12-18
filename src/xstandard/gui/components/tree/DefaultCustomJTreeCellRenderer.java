package xstandard.gui.components.tree;

import java.awt.Color;
import java.awt.Component;
import java.util.EventObject;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

public class DefaultCustomJTreeCellRenderer extends DefaultTreeCellRenderer implements CustomJTreeCellRenderer {

	private Icon customIcon = null;

	@Override
	public void setIcon(Icon icn) {
		customIcon = icn;
	}

	@Override
	public Icon getIcon() {
		return customIcon;
	}

	private void setLabelIcon(Icon icn) {
		super.setIcon(icn);
	}
	
	@Override
	public void free() {
		getUI().uninstallUI(this);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
		boolean sel,
		boolean expanded,
		boolean leaf, int row,
		boolean hasFocus) {
		
		this.hasFocus = hasFocus;

		Color fg = null;

		if (sel) {
			fg = getTextSelectionColor();
		} else {
			fg = getTextNonSelectionColor();
		}

		setForeground(fg);

		Icon icon = getIcon();
		if (icon == null) {
			if (leaf) {
				icon = getLeafIcon();
			} else if (expanded) {
				icon = getOpenIcon();
			} else {
				icon = getClosedIcon();
			}
		}

		if (!tree.isEnabled()) {
			setEnabled(false);
			LookAndFeel laf = UIManager.getLookAndFeel();
			Icon disabledIcon = laf.getDisabledIcon(tree, icon);
			if (disabledIcon != null) {
				icon = disabledIcon;
			}
			setDisabledIcon(icon);
		} else {
			setEnabled(true);
			setLabelIcon(icon);
		}
		setComponentOrientation(tree.getComponentOrientation());

		selected = sel;

		return this;
	}

	@Override
	public boolean isEditable(EventObject e) {
		return false;
	}
}
