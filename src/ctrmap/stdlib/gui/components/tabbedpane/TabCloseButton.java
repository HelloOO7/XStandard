package ctrmap.stdlib.gui.components.tabbedpane;

import javax.swing.Icon;
import javax.swing.JButton;

public class TabCloseButton extends JButton {

	TabbedPaneTab tab;

	public TabCloseButton(TabbedPaneTab tab) {
		this.tab = tab;
		
		addActionListener((e) -> {
			tab.remove();
		});
	}

	public TabCloseButton() {

	}

	@Override
	public Icon getIcon() {
		if (tab != null) {
			return tab.pane.getCloseIcon();
		}
		return null;
	}
	
	@Override
	public boolean isVisible(){
		return tab.pane.isCloseable();
	}
}
