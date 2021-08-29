package ctrmap.stdlib.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * An ArrayList extension able to notify listeners of changes to the array.
 *
 * @param <T> Element type.
 */
public class ListenableList<T> extends ArrayList<T> {

	@ReflectionHashIgnore
	private List<ElementChangeListener> listeners = new ArrayList<>();
	
	public ListenableList(){
		
	}
	
	public ListenableList(Collection<? extends T> c){
		super(c);
	}

	/**
	 * Binds an ElementChangeListener to this array.
	 *
	 * @param l The listener.
	 */
	public void addListener(ElementChangeListener l) {
		ArraysEx.addIfNotNullOrContains(listeners, l);
	}

	/**
	 * Removes a previously bound listener from this array.
	 *
	 * @param l The listener to remove.
	 */
	public void removeListener(ElementChangeListener l) {
		listeners.remove(l);
	}

	@Override
	public boolean add(T o) {
		boolean rsl = super.add(o);
		fireAddEvent(o);
		return rsl;
	}

	@Override
	public void add(int index, T o) {
		super.add(index, o);
		fireAddEvent(o);
	}

	@Override
	public boolean addAll(Collection<? extends T> o) {
		boolean rsl = super.addAll(o);
		for (T t : o) {
			fireAddEvent(t);
		}
		return rsl;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> o) {
		boolean rsl = super.addAll(index, o);
		for (T t : o) {
			fireAddEvent(t);
		}
		return rsl;
	}

	@Override
	public T set(int idx, T o) {
		T r = super.set(idx, o);
		fireRemoveEvent(r, idx);
		fireAddEvent(o);
		return r;
	}
	
	public void setModify(int idx, T o) {
		super.set(idx, o);
		fireModifyEvent(o);
	}

	@Override
	public T remove(int index) {
		T e = super.remove(index);
		fireRemoveEvent(e, index);
		return e;
	}

	@Override
	public boolean remove(Object elem) {
		int idx = indexOf(elem);
		boolean rsl = super.remove(elem);
		if (rsl) {
			fireRemoveEvent(elem, idx);
		}
		return rsl;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object elem : c) {
			if (contains(elem)) {
				fireRemoveEvent(elem, indexOf(elem));
			}
		}
		boolean rsl = super.removeAll(c);
		return rsl;
	}

	@Override
	public void clear() {
		for (int i = size() - 1; i >= 0; i--) {
			fireRemoveEvent(get(i), i);
		}
		super.clear();
	}

	public T getOrDefault(int index, T defaultValue){
		if (index >= 0 && index < size()){
			return get(index);
		}
		return defaultValue;
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void replaceAll(UnaryOperator<T> c) {
		throw new UnsupportedOperationException("Not supported.");
	}

	protected void fireAddEvent(T entity) {
		fireChangeEvent(entity, ElementChangeType.ADD, indexOf(entity));
	}

	protected void fireRemoveEvent(Object entity, int index) {
		fireChangeEvent(entity, ElementChangeType.REMOVE, index);
	}

	/**
	 * Fire an event to this array's listener, indicating that the contents of an element have changed.
	 *
	 * @param entity An element in this array whose content has been changed externally.
	 */
	public void fireModifyEvent(Object entity) {
		fireChangeEvent(entity, ElementChangeType.MODIFY, indexOf(entity));
	}

	private void fireChangeEvent(Object entity, ElementChangeType type, int index) {
		fireChangeEvent(new ElementChangeEvent(entity, type, index));
	}

	private void fireChangeEvent(ElementChangeEvent evt) {
		for (ElementChangeListener l : new ArrayList<>(listeners)) {
			l.onEntityChange(evt);
		}
	}

	public static class ElementChangeEvent {

		public final Object element;
		public final int index;
		public final ElementChangeType type;

		public ElementChangeEvent(Object entity, ElementChangeType type, int idx) {
			this.element = entity;
			this.type = type;
			this.index = idx;
		}
	}

	public interface ElementChangeListener {

		public void onEntityChange(ElementChangeEvent evt);
	}

	public static enum ElementChangeType {
		ADD,
		REMOVE,
		MODIFY
	}
}
