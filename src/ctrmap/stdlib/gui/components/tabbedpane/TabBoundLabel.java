package ctrmap.stdlib.gui.components.tabbedpane;

import javax.swing.Icon;
import javax.swing.JLabel;

public class TabBoundLabel extends JLabel {

	TabbedPaneTab tab;

	public TabBoundLabel(TabbedPaneTab tab) {
		this.tab = tab;
	}

	TabBoundLabel() {

	}

	@Override
	public String getText() {
		if (tab != null) {
			return tab.getTitle();
		}
		return super.getText();
	}
	
	@Override
	public Icon getIcon(){
		if (tab != null) {
			return tab.getIcon();
		}
		return super.getIcon();
	}
}
