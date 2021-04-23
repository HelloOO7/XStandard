package ctrmap.stdlib.gui.components.tabbedpane;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JTabbedPane;

public class JTabbedPaneEx extends JTabbedPane {
	private List<TabbedPaneTab> tabs = new ArrayList<>();
	
	private Icon closeIcon = null;
	private boolean closeable = false;
	
	public JTabbedPaneEx(){
		
	}
	
	@Override
	public void insertTab(String title, Icon icon, Component component, String tip, int index) {
		TabbedPaneTab tab = new TabbedPaneTab(this, index);
		tabs.add(index, tab);
		updateTabIndices();
		super.insertTab(title, icon, component, tip, index);
		setTabComponentAt(index, tab.getHeader());
	}
	
	@Override
	public void removeTabAt(int index){
		super.removeTabAt(index);
		tabs.remove(index);
		updateTabIndices();
	}
	
	private void updateTabIndices(){
		for (int i = 0; i < tabs.size(); i++){
			tabs.get(i).setIndex(i);
		}
	}
	
	public Icon getCloseIcon(){
		return closeIcon;
	}
	
	public void setCloseIcon(Icon icn){
		closeIcon = icn;
		repaint();
	}
	
	public void setCloseable(boolean val){
		closeable = val;
		repaint();
	}
	
	public boolean isCloseable(){
		return closeable;
	}
}
