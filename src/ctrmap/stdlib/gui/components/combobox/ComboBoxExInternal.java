// 
// Decompiled by Procyon v0.5.36
// 
package ctrmap.stdlib.gui.components.combobox;

import javax.swing.plaf.basic.BasicComboBoxEditor;
import ctrmap.stdlib.util.ArraysEx;
import javax.swing.text.BadLocationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import ctrmap.stdlib.gui.components.listeners.DocumentAdapterEx;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class ComboBoxExInternal<T> extends JComboBox<T> {

	private DefaultComboBoxModel<T> model = new ComboBoxModelEx<>();
	private List<T> itemLib = new ArrayList<>();

	private List<ComboBoxListener> listeners = new ArrayList<>();
	private ComboBoxEditorEx editorEx = new ComboBoxEditorEx();

	private ACMode mode = ACMode.CONTAINS;
	private boolean allowListeners = true;
	private boolean allowAC = false;
	private boolean acAlphaSort = true;
	private boolean allowProgramTextChanges = false;
	private boolean acPopUpOnType = true;

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
					lastGoodSelectedItem = item;
					listeners.iterator();

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
					}
					setSelectedItem(candidates.get(0));
				} else if (item != lastGoodSelectedItem) {
					setSelectedItem(lastGoodSelectedItem);
				}
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
								popup.hide();
								popup.show();
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

	private String getUniqueStrInstance(String str) {
		if (str == null){
			return str;
		}
		return new String(str);
	}

	public void loadValues(T... values) {
		loadValues(ArraysEx.asList(values));
	}

	public void loadValues(List<T> values) {
		itemLib.clear();
		if (!values.isEmpty()) {
			boolean isStr = values.get(0) instanceof String;
			if (!isStr) {
				itemLib.addAll(values);
			} else {
				for (T str : values) {
					itemLib.add((T) getUniqueStrInstance((String) str));
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
		makeValuesByQuery(getSelectedItem(), false);
	}

	@Override
	public void insertItemAt(T item, int index) {
		if (item instanceof String) {
			item = (T) getUniqueStrInstance((String) item);
		}
		itemLib.add(index, item);
		makeValuesByQuery(getSelectedItem(), false);
	}

	@Override
	public void removeItemAt(int index) {
		if (index >= 0 && index < itemLib.size()) {
			candidates.remove(itemLib.get(index));
			itemLib.remove(index);
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
		setEditable(val);
	}

	public void setACAlphabeticSort(boolean val) {
		acAlphaSort = val;
	}

	public boolean getACAlphabeticSort() {
		return acAlphaSort;
	}

	public void setACPopUpOnType(boolean val) {
		acPopUpOnType = val;
	}

	@Override
	public int getSelectedIndex() {
		if (candidates != null) {
			Object item = getSelectedItem();

			for (int i = 0; i < candidates.size(); ++i) {
				if (candidates.get(i) == item) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public void setSelectedIndex(int index) {
		if (index < 0 || index >= candidates.size()) {
			setSelectedItem(null);
		}
		setSelectedItem(candidates.get(index));
	}

	public int getItemCountEx() {
		return itemLib.size();
	}

	public void setSelectedIndexEx(int index) {
		if (index == -1) {
			setSelectedItem(null);
		} else {
			setSelectedItem(itemLib.get(index));
		}
	}

	@Override
	public void setSelectedItem(Object item) {
		//Fuck you, Oracle. I want to use == not equals. You don't like that, take this shitty code.
		selectedItemReminder = null;
		isEditable = true;
		super.setSelectedItem(item);
		isEditable = false;
	}

	public int getSelectedIndexEx() {
		if (itemLib != null) {
			Object item = getSelectedItem();
			for (int i = 0; i < itemLib.size(); ++i) {
				if (itemLib.get(i) == item) {
					return i;
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
		if (q != null){
			query = String.valueOf(q);
		}
		candidates = new ArrayList<>();
		if (!allowAC || forceFull || query == null || query.isEmpty()) {
			candidates = itemLib;
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
				candidates = itemLib;
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
				if (acAlphaSort) {
					candidates.sort((o1, o2) -> {
						String str1 = String.valueOf(o1);
						String str2 = String.valueOf(o2);
						return str1.compareToIgnoreCase(str2);
					});
				}
			}
		}
		allowListeners = false;
		model.removeAllElements();
		for (T t : candidates) {
			model.addElement(t);
		}
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

	private class ComboBoxModelEx<T> extends DefaultComboBoxModel<T> {

		private boolean disallowFireContentsChanged = false;

		@Override
		public void addElement(T item) {
			super.addElement(item);
			if (getSize() == 1) {
				super.setSelectedItem(item);
			}
		}

		@Override
		public void setSelectedItem(Object item) {
			if (allowListeners) {
				//I think I'm slowly starting to get why Futaba Sakura's codename is Oracle, 'cause she's just as excruciatingly annoying as the company

				disallowFireContentsChanged = true;
				super.setSelectedItem(null);
				disallowFireContentsChanged = false;
				super.setSelectedItem(item);
			}
		}

		@Override
		protected void fireContentsChanged(Object source, int index0, int index1) {
			if (!disallowFireContentsChanged) {
				super.fireContentsChanged(source, index0, index1);
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

	public interface ComboBoxListener {

		public void itemSelected(Object selectedItem);
	}
}
