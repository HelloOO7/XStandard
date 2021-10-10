// 
// Decompiled by Procyon v0.5.36
// 
package ctrmap.stdlib.gui.components.combobox;

import ctrmap.stdlib.gui.components.listeners.AbstractToggleableListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import ctrmap.stdlib.util.ArraysEx;
import javax.swing.text.BadLocationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import ctrmap.stdlib.gui.components.listeners.DocumentAdapterEx;
import ctrmap.stdlib.util.ListenableList;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class ComboBoxExInternal<T> extends JComboBox<T> {

	private ComboBoxModelEx<T> model = new ComboBoxModelEx<>();
	private List<T> itemLib = new ArrayList<>();

	private List<ComboBoxListener> listeners = new ArrayList<>();
	private ComboBoxEditorEx editorEx = new ComboBoxEditorEx();

	private ACMode mode = ACMode.CONTAINS;
	private boolean allowListeners = true;
	private boolean allowAC = false;
	private boolean acEnableComparator = true;
	private boolean allowProgramTextChanges = false;
	private boolean acPopUpOnType = true;
	private boolean useEqualsAnyway = false;

	private boolean allowPopupMenuListener = true;

	private Comparator<T> comparator = new Comparator<T>() {
		@Override
		public int compare(T o1, T o2) {
			String str1 = String.valueOf(o1);
			String str2 = String.valueOf(o2);
			return str1.compareToIgnoreCase(str2);
		}
	};

	public ComboBoxExInternal() {
		super();
		setEditor(editorEx);
		setModel(model);

		Document doc = ((JTextField) this.getEditor().getEditorComponent()).getDocument();
		final ComboBoxExInternal mInstance = this;

		addActionListener(e -> {
			if (allowListeners) {
				Object item = getSelectedItem();
				allowListeners = false;
				allowProgramTextChanges = true;
				editorEx.setItem(item);
				allowProgramTextChanges = false;
				allowListeners = true;

				if (candidates.size() == 1) {
					((ComboPopup) mInstance.getUI().getAccessibleChild(mInstance, 0)).hide();
				}
				if (itemLib.contains(item) || item == null) {
					if (item != null) {
						lastGoodSelectedItem = item;
					}

					for (ComboBoxListener l : listeners) {
						l.itemSelected(item);
					}
				} else if (!candidates.isEmpty()) {
					if (mode != ACMode.STARTS_WITH) {
						String q = String.valueOf(item);

						for (T t : candidates) {
							if (String.valueOf(t).startsWith(q)) {
								setSelectedItem(q);
								break;
							}
						}
					} else {
						setSelectedItem(candidates.get(0));
					}
				} else if (item != lastGoodSelectedItem) {
					setSelectedItem(lastGoodSelectedItem);
				}
			}
		});

		addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				if (allowPopupMenuListener && allowAC) {
					makeValuesByQuery(null, false);
				}
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		});

		doc.addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				if (!allowListeners || e.getLength() == 0 || !allowAC) {
					return;
				}
				SwingUtilities.invokeLater(() -> {
					try {
						String text = doc.getText(0, doc.getLength());
						makeValuesByQuery(text, false);
						ComboPopup popup = (ComboPopup) ((Object) mInstance.getUI().getAccessibleChild(mInstance, 0));
						if (popup.isVisible() || acPopUpOnType) {
							if (mInstance.getModel().getSize() == 0) {
								popup.hide();
							} else {
								allowPopupMenuListener = false;
								popup.hide();
								popup.show();
								allowPopupMenuListener = true;
							}
						}
					} catch (BadLocationException ex) {
						Logger.getLogger(ComboBoxExInternal.class.getName()).log(Level.SEVERE, null, ex);
					}
				});
			}
		});

		editorEx.getEditorComponent().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE && lastGoodSelectedItem != null) {
					setSelectedItem(lastGoodSelectedItem);
				}
			}
		});
	}

	public void addListener(ComboBoxListener l) {
		listeners.add(l);
	}

	private static String getUniqueStrInstance(String str) {
		if (str == null) {
			return str;
		}
		return new String(str);
	}

	public void loadValues(T... values) {
		loadValues(ArraysEx.asList(values));
	}

	public void loadValuesIndexed(T... values) {
		loadValuesIndexed(ArraysEx.asList(values));
	}

	public void loadValuesListenable(ListenableList<T> values) {
		loadValues(values);

		values.addListener((ListenableList.ElementChangeEvent evt) -> {
			int selidx = getSelectedIndexEx();
			switch (evt.type) {
				case MODIFY:
					itemLib.set(evt.index, (T) evt.element);
					break;
				case ADD:
					if (evt.element instanceof String) {
						itemLib.add(evt.index, (T) getUniqueStrInstance((String) evt.element));
					} else {
						itemLib.add(evt.index, (T) evt.element);
					}
					break;
				case REMOVE:
					itemLib.remove(evt.index);
					break;
			}

			makeValuesByQuery(null, true);

			if (selidx == evt.index) {
				setSelectedIndexEx(selidx);
			}
		});
	}

	public void loadValuesIndexed(List<T> values) {
		loadValues(values, true);
	}

	public void loadValues(List<T> values) {
		loadValues(values, false);
	}

	public void loadValues(List<T> values, boolean indexed) {
		itemLib.clear();
		if (!values.isEmpty()) {
			boolean isStr = values.get(0) instanceof String;
			if (!isStr) {
				itemLib.addAll(values);
			} else {
				if (indexed) {
					int index = 0;
					for (T str : values) {
						itemLib.add((T) (index + " - " + str));
						index++;
					}
				} else {
					for (T str : values) {
						itemLib.add((T) getUniqueStrInstance((String) str));
					}
				}
			}
		}
		makeValuesByQuery(null, true);
	}

	@Override
	public void addItem(T item) {
		if (item instanceof String) {
			item = (T) getUniqueStrInstance((String) item);
		}
		itemLib.add(item);
		makeValuesByQuery(getSelectedItem(), true);
	}

	@Override
	public void insertItemAt(T item, int index) {
		if (item instanceof String) {
			item = (T) getUniqueStrInstance((String) item);
		}
		itemLib.add(index, item);
		makeValuesByQuery(getSelectedItem(), true);
	}

	@Override
	public void removeItemAt(int index) {
		if (index >= 0 && index < itemLib.size()) {
			candidates.remove(itemLib.get(index));
			itemLib.remove(index);
			makeValuesByQuery(getSelectedItem(), true);
		}
	}

	public void setACMode(ACMode mode) {
		this.mode = mode;
		if (allowAC) {
			String lq = lastQuery;
			lastQuery = null;
			makeValuesByQuery(lq, true);
		}
	}

	public void setAllowAC(boolean val) {
		allowAC = val;
		if (!isEditable()) {
			setEditable(val);
		}
	}

	public void setACAlphabeticSort(boolean val) {
		acEnableComparator = val;
	}

	public void setACComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	public void setUseEqualsAnyway(boolean val) {
		useEqualsAnyway = val;
	}

	public boolean getUseEqualsAnyway() {
		return useEqualsAnyway;
	}

	public boolean getACAlphabeticSort() {
		return acEnableComparator;
	}

	public void setACPopUpOnType(boolean val) {
		acPopUpOnType = val;
	}

	@Override
	public int getSelectedIndex() {
		if (candidates != null) {
			Object item = getSelectedItem();

			for (int i = 0; i < candidates.size(); ++i) {
				if (useEqualsAnyway) {
					if (candidates.get(i).equals(item)) {
						return i;
					}
				} else {
					if (candidates.get(i) == item) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	@Override
	public void setSelectedIndex(int index) {
		if (index < 0 || index >= candidates.size()) {
			setSelectedItem(null);
		} else {
			setSelectedItem(candidates.get(index));
		}
	}

	public int getItemCountEx() {
		return itemLib.size();
	}

	public void setSelectedIndexEx(int index) {
		if (index == -1) {
			lastGoodSelectedItem = null;
			setSelectedItem(null);
		} else {
			setSelectedItem(itemLib.get(index));
		}
	}

	@Override
	public void setSelectedItem(Object item) {
		Object objectToSelect = item;

		getEditor().setItem(item);

		dataModel.setSelectedItem(objectToSelect);
		fireActionEvent();
		//Fuck you, Oracle. I want to use == not equals. You don't like that, take this shitty code.
		/*selectedItemReminder = null;
		boolean editable = isEditable;
		isEditable = true;
		try {
			super.setSelectedItem(item);
		} catch (Exception ex) {
			isEditable = editable;
			ex.printStackTrace();
			throw ex;
		}
		isEditable = editable;*/
	}

	@Override
	public Object getSelectedItem() {
		Object item = super.getSelectedItem();
		if (item == null && allowAC) {
			return lastGoodSelectedItem;
		}
		return item;
	}

	public int getSelectedIndexEx() {
		if (itemLib != null) {
			if (!useEqualsAnyway) {
				Object item = getSelectedItem();

				for (int i = 0; i < itemLib.size(); i++) {
					if (itemLib.get(i) == item) {
						return i;
					}
				}
			} else {
				Object item = getSelectedItem();
				if (item == null) {
					item = lastGoodSelectedItem;
				}

				for (int i = 0; i < itemLib.size(); i++) {
					if (itemLib.get(i).equals(item)) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	private Object lastGoodSelectedItem;
	private String lastQuery;
	private List<T> candidates = new ArrayList<>();

	private void makeValuesByQuery(Object q, boolean forceFull) {
		String query = null;
		if (q != null) {
			query = String.valueOf(q);
		}
		candidates.clear();
		if (!allowAC || forceFull || query == null || query.isEmpty()) {
			candidates.addAll(itemLib);
		} else {
			if (query.equals(this.lastQuery)) {
				return;
			}
			boolean itemsContainsQuery = false;
			for (T c : itemLib) {
				if (String.valueOf(c).equals(query)) {
					itemsContainsQuery = true;
					break;
				}
			}
			if (itemsContainsQuery) {
				candidates.addAll(itemLib);
			} else {
				if (mode == ACMode.INDEX_SKIP_MATCH) {
					candidates.addAll(itemLib);
				} else {
					for (T c : itemLib) {
						String s = String.valueOf(c);
						switch (this.mode) {
							case CONTAINS: {
								if (containsIgnoreCase(s, query)) {
									candidates.add(c);
								}
								break;
							}
							case STARTS_WITH: {
								if (startsWithIgnoreCase(s, query)) {
									candidates.add(c);
								}
								break;
							}
						}
					}
				}
				if (acEnableComparator) {
					candidates.sort(comparator);
				}
			}
		}
		allowListeners = false;
		Object last = model.getSelectedItem();
		model.removeAllElements();
		model.addElements(candidates);
		model.setSelectedItem(last);
		allowListeners = true;
	}

	private static boolean containsIgnoreCase(String str, String content) {
		return str.toLowerCase().contains(content.toLowerCase());
	}

	private static boolean startsWithIgnoreCase(String str, String sw) {
		return str.toLowerCase().startsWith(sw.toLowerCase());
	}

	public enum ACMode {
		CONTAINS,
		STARTS_WITH,
		INDEX_SKIP_MATCH;
	}

	public class ComboBoxModelEx<E> extends AbstractListModel<E> implements MutableComboBoxModel<E>, Serializable {

		protected List<E> objects;
		protected Object selectedObject;

		public ComboBoxModelEx() {
			objects = new ArrayList<E>();
		}

		private void setSelectedItem(Object anObject, boolean fireEvents) {
			if (selectedObject != anObject) {
				selectedObject = anObject;
				if (fireEvents) {
					fireContentsChanged(this, -1, -1);
				}
			}
		}

		@Override
		public void setSelectedItem(Object item) {
			if (allowListeners) {
				setSelectedItem(item, true);
			}
		}

		@Override
		public Object getSelectedItem() {
			return selectedObject;
		}

		@Override
		public int getSize() {
			return objects.size();
		}

		@Override
		public E getElementAt(int index) {
			if (index >= 0 && index < objects.size()) {
				return objects.get(index);
			} else {
				return null;
			}
		}

		// implements javax.swing.MutableComboBoxModel
		@Override
		public void addElement(E anObject) {
			objects.add(anObject);
			fireIntervalAdded(this, objects.size() - 1, objects.size() - 1);
			if (objects.size() == 1 && selectedObject == null && anObject != null) {
				setSelectedItem(anObject);
			}
		}

		public void addElements(Collection<E> collection) {
			if (!collection.isEmpty()) {
				boolean empty = objects.isEmpty();
				objects.addAll(collection);
				fireIntervalAdded(this, objects.size() - collection.size(), objects.size() - 1);
				if (empty && selectedObject == null) {
					setSelectedItem(objects.get(0));
				}
			}
		}

		@Override
		public void insertElementAt(E anObject, int index) {
			objects.add(index, anObject);
			fireIntervalAdded(this, index, index);
		}

		@Override
		public void removeElementAt(int index) {
			if (getElementAt(index) == selectedObject) {
				if (index == 0) {
					setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
				} else {
					setSelectedItem(getElementAt(index - 1));
				}
			}

			objects.remove(index);

			fireIntervalRemoved(this, index, index);
		}

		@Override
		public void removeElement(Object anObject) {
			int index = objects.indexOf(anObject);
			if (index != -1) {
				removeElementAt(index);
			}
		}

		public void removeAllElements() {
			if (objects.size() > 0) {
				int firstIndex = 0;
				int lastIndex = objects.size() - 1;
				objects.clear();
				selectedObject = null;
				fireIntervalRemoved(this, firstIndex, lastIndex);
			} else {
				selectedObject = null;
			}
		}
	}

	private class ComboBoxEditorEx extends BasicComboBoxEditor {

		public ComboBoxEditorEx() {
			super();
		}

		@Override
		public JTextField createEditorComponent() {
			JTextField tf = new JTextField();
			tf.setBorder(new EmptyBorder(0, 2, 0, 2));
			return tf;
		}

		@Override
		public void setItem(final Object item) {
			if (allowProgramTextChanges) {
				super.setItem(item);
			}
		}
	}

	public static interface ComboBoxListener {

		public void itemSelected(Object selectedItem);
	}

	public static abstract class ToggleableComboBoxListener extends AbstractToggleableListener implements ComboBoxListener {

		public abstract void itemSelectedImpl(Object item);

		@Override
		public void itemSelected(Object item) {
			if (getAllowEvents()) {
				itemSelectedImpl(item);
			}
		}
	}
}
