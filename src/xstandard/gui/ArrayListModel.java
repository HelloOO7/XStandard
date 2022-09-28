package xstandard.gui;

import xstandard.util.ListenableList;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

public class ArrayListModel<E> extends AbstractListModel<E> implements ComboBoxModel<E> {

	private ListenableList<E> list;
	private Object selectedItem;

	private final ListenableList.ElementChangeListener listener = new ListenableList.ElementChangeListener() {
		@Override
		public void onEntityChange(ListenableList.ElementChangeEvent evt) {
			switch (evt.type) {
				case MODIFY:
					fireContentsChanged(ArrayListModel.this, evt.index, evt.index);
					break;
				case ADD:
					fireIntervalAdded(ArrayListModel.this, evt.index, evt.index);
					break;
				case REMOVE:
					fireIntervalRemoved(ArrayListModel.this, evt.index, evt.index);
					break;
			}
		}
	};

	public void setList(ListenableList<E> list) {
		if (this.list != null) {
			fireIntervalRemoved(this, 0, this.list.size());
			this.list.removeListener(listener);
		}
		this.list = list;
		this.list.addListener(listener);
		fireIntervalAdded(this, 0, list.size());
	}

	@Override
	public int getSize() {
		return list == null ? 0 : list.size();
	}

	@Override
	public E getElementAt(int index) {
		return list.get(index);
	}

	@Override
	public void setSelectedItem(Object anItem) {
		this.selectedItem = anItem;
	}

	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	public int indexOf(E element) {
		return list.indexOf(element);
	}
}
