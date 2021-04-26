package ctrmap.stdlib.gui.components.tabbedpane;

import java.awt.Component;
import javax.swing.Icon;

public class TabbedPaneTab {
	
	JTabbedPaneEx pane;
	private int index = -1;
	
	private TabHeader header;
	
	TabbedPaneTab(JTabbedPaneEx pane, int index){
		this.pane = pane;
		this.index = index;
		header = new TabHeader(this, pane);
	}
	
	TabHeader getHeader(){
		return header;
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	public String getTitle(){
		return pane.getTitleAt(index);
	}
	
	public Icon getIcon(){
		return pane.getIconAt(index);
	}
	
	public Component getComponent(){
		return pane.getComponentAt(index);
	}
	
	public void remove(){
		pane.removeTabAt(index);
	}
}
