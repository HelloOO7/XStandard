package xstandard.gui.components.tabbedpane;

import xstandard.fs.FSFile;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;

public class JTabbedPaneEx extends JTabbedPane {
	private List<TabbedPaneTab> tabs = new ArrayList<>();
	
	private Icon closeIcon = null;
	private Icon closeIconRollover = null;
	private Icon closeIconPressed = null;
	
	private boolean closeable = false;
	
	private List<TabRemovedListener> tabRemovedListeners = new ArrayList<>();
		
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
	
	public void addTabRemovedListener(TabRemovedListener l){
		tabRemovedListeners.add(l);
	}
	
	public TabbedPaneTab getTab(int index){
		return tabs.get(index);
	}
	
	@Override
	public void removeTabAt(int index){
		TabbedPaneTab tab = tabs.get(index);
		for (TabRemovedListener l : tabRemovedListeners){
			if (!l.onTabIsRemoving(tab)){
				return;
			}
		}
		tab.finalizeBeforeRemove();
		tabs.remove(index);
		updateTabIndices();
		super.removeTabAt(index);
		for (TabRemovedListener l : tabRemovedListeners){
			l.onTabRemoved(tab);
		}
	}
	
	private void updateTabIndices(){
		for (int i = 0; i < tabs.size(); i++){
			tabs.get(i).setIndex(i);
		}
	}
	
	public Icon getCloseIcon(){
		return closeIcon;
	}
	
	public Icon getCloseIconRollover(){
		return closeIconRollover;
	}
	
	public Icon getCloseIconPressed(){
		return closeIconPressed;
	}
	
	public void setCloseIconsFromDirectory(FSFile dir){
		setCloseIcon(new ImageIcon(dir.getChild("idle.png").getBytes()));
		setCloseIconRollover(new ImageIcon(dir.getChild("rollover.png").getBytes()));
		setCloseIconPressed(new ImageIcon(dir.getChild("pressed.png").getBytes()));
	}
	
	public void setCloseIcon(Icon icn){
		closeIcon = icn;
		repaint();
	}
	
	public void setCloseIconRollover(Icon icn){
		closeIconRollover = icn;
		repaint();
	}
	
	public void setCloseIconPressed(Icon icn){
		closeIconPressed = icn;
		repaint();
	}
	
	public void setCloseable(boolean val){
		closeable = val;
		repaint();
	}
	
	public boolean isCloseable(){
		return closeable;
	}
	
	public interface TabRemovedListener {
		public boolean onTabIsRemoving(TabbedPaneTab tab);
		public void onTabRemoved(TabbedPaneTab tab);
	}
}
