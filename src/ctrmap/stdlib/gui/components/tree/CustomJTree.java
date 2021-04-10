/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ctrmap.stdlib.gui.components.tree;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 */
public class CustomJTree extends JTree {
	private CustomJTreeRootNode root;
	private DefaultTreeModel model;
	protected TreeIconResourceProvider iconProvider = new TreeIconResourceProvider();
	
	public CustomJTree(){
		super();
		setCellRenderer(new CustomJTreeNode.CellRenderer());
		setRootVisible(false);
		root = new CustomJTreeRootNode(iconProvider);
		model = new DefaultTreeModel(root);
		setModel(model);
		setShowsRootHandles(true);
	}
	
	public void removeAllChildren(){
		root.removeAllChildren();
		model.reload(root);
	}
	
	public CustomJTreeRootNode getRootNode(){
		return root;
	}
	
	public TreeIconResourceProvider getIconProvider(){
		return iconProvider;
	}
	
	public void reload(){
		model.reload();
	}
	
	public void addListener(CustomJTreeSelectionListener listener){
		addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				listener.onNodeSelected((CustomJTreeNode)getLastSelectedPathComponent());
			}
		});
	}
}
