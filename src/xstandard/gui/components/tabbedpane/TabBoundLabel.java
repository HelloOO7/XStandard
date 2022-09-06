package xstandard.gui.components.tabbedpane;

import java.awt.Font;
import javax.swing.Icon;
import javax.swing.JLabel;

public class TabBoundLabel extends JLabel {

	TabbedPaneTab tab;

	public TabBoundLabel(TabbedPaneTab tab) {
		this.tab = tab;
	}

	public TabBoundLabel() {

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
	
	Font getFontInternal(){
		return super.getFont();
	}
	
	@Override
	public Font getFont(){
		if (tab != null){
			return tab.getHeaderFont();
		}
		return super.getFont();
	}
}
