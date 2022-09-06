package xstandard.gui.components.tabbedpane;

import java.awt.Component;
import java.awt.Font;
import javax.swing.Icon;

public class TabbedPaneTab {

	JTabbedPaneEx pane;
	private int index = -1;

	private TabHeader header;
	private Font headerFont;

	private Component componentAfterRemoval = null;

	TabbedPaneTab(JTabbedPaneEx pane, int index) {
		this.pane = pane;
		this.index = index;
		header = new TabHeader(this, pane);
		headerFont = header.getLabel().getFontInternal();
	}

	void finalizeBeforeRemove() {
		componentAfterRemoval = getComponent();
	}

	TabHeader getHeader() {
		return header;
	}
	
	public Font getHeaderFont(){
		return headerFont;
	}
	
	public void setHeaderFont(Font f){
		this.headerFont = f;
		header.revalidate();
		header.repaint();
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex(){
		return index;
	}

	public String getTitle() {
		return pane.getTitleAt(index);
	}

	public Icon getIcon() {
		return pane.getIconAt(index);
	}

	public Component getComponent() {
		if (componentAfterRemoval != null){
			return componentAfterRemoval;
		}
		return pane.getComponentAt(index);
	}

	public void remove() {
		pane.removeTabAt(index);
	}
}
