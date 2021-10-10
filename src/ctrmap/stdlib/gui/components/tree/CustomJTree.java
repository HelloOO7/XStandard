package ctrmap.stdlib.gui.components.tree;

import ctrmap.stdlib.res.ResourceAccess;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;

public class CustomJTree extends JTree {

	protected final CustomJTreeRootNode root;
	protected DefaultTreeModel model;
	protected TreeIconResourceProvider iconProvider = new TreeIconResourceProvider();

	private int imageSize = -1;

	public CustomJTree() {
		super();
		setCellRenderer(new CustomJTreeMultiCellRenderer());
		setEditable(true);
		setCellEditor(new CustomJTreeEditor(this));
		setRootVisible(false);
		root = new CustomJTreeRootNode(iconProvider);
		model = new DefaultTreeModel(root);
		setModel(model);
		setShowsRootHandles(true);
	}

	public void setImageSize(int size) {
		this.imageSize = size;
	}

	public int getImageSize() {
		return imageSize;
	}

	protected void registerIconResourceImpl(int resID, String path) {
		if (iconProvider != null) {
			iconProvider.registerResourceIcon(resID, ResourceAccess.getByteArray(path), getImageSize());
		}
	}

	@Override
	public DefaultTreeModel getModel() {
		return model;
	}

	public void setModel() {
		throw new UnsupportedOperationException();
	}

	public void removeAllChildren() {
		root.removeAllChildren();
		model.reload(root);
	}

	public CustomJTreeRootNode getRootNode() {
		return root;
	}

	public TreeIconResourceProvider getIconProvider() {
		return iconProvider;
	}

	public void reload() {
		model.reload();
	}

	public void addListener(CustomJTreeSelectionListener listener) {
		addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				listener.onNodeSelected((CustomJTreeNode) getLastSelectedPathComponent());
			}
		});
	}
}
