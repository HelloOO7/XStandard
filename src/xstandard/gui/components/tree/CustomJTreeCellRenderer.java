package xstandard.gui.components.tree;

import java.util.EventObject;
import javax.swing.Icon;
import javax.swing.tree.TreeCellRenderer;

public interface CustomJTreeCellRenderer extends TreeCellRenderer {
	public String getText();
	public Icon getIcon();
	public void setText(String text);
	public void setIcon(Icon icn);
	
	public void repaint();
	
	public boolean isEditable(EventObject e);
}
