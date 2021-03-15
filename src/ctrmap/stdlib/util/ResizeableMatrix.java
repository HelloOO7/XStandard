package ctrmap.stdlib.util;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ResizeableMatrix<T> implements Iterable<T> {

	private int originX = 0;
	private int originY = 0;

	public ArrayList<ArrayList<T>> list = new ArrayList<>();
	private T defaultValue;

	public ResizeableMatrix(int width, int height, T defaultValue) {
		this.defaultValue = defaultValue;
		populateList(list, width, false);
		for (int i = 0; i < width; i++) {
			populateList(list.get(i), height, true);
		}
	}

	public void merge(ResizeableMatrix<T> mtx) {
		expandMatrixToPoint(mtx.originX, mtx.originY);
		expandMatrixToPoint(mtx.originX + mtx.getWidth(), mtx.originY + mtx.getHeight());
		for (int x = 0; x < mtx.getWidth(); x++) {
			for (int y = 0; y < mtx.getHeight(); y++) {
				int tx = x + mtx.originX;
				int ty = y + mtx.originY;
				T obj = get(tx, ty);
				T src = mtx.get(tx, ty);
				if (src != null) {
					if (obj != null && obj instanceof Collection) {
						Collection c = (Collection) obj;
						c.addAll((Collection)src);
					} else {
						set(tx, ty, mtx.get(tx, ty));
					}
				}
			}
		}
	}
	
	public List<T> getRow(int idx){
		List<T> r = new ArrayList<>();
		int relocIdx = idx - originY;
		if (relocIdx < 0 || relocIdx >= getHeight()){
			return null;
		}
		for (List<T> col : list){
			r.add(col.get(relocIdx));
		}
		return r;
	}
	
	public List<T> getRows(){
		List<T> r = new ArrayList<>();
		for (int i = 0; i < getHeight(); i++){
			r.addAll(getRow(i + originY));
		}
		return r;
	}
	
	public List<T> getColumn(int idx){
		List<T> r = new ArrayList<>();
		int relocIdx = idx - originX;
		if (relocIdx < 0 || relocIdx >= getWidth()){
			return null;
		}
		r.addAll(list.get(relocIdx));
		return r;
	}

	public void clear() {
		list.clear();
		originX = 0;
		originY = 0;
	}

	public int getWidth() {
		return list.size();
	}

	public int getHeight() {
		if (list.size() > 0) {
			return list.get(0).size();
		} else {
			return 0;
		}
	}

	public Point getOrigin() {
		return new Point(originX, originY);
	}

	public ResizeableMatrix getSubMatrix(int x, int y, int w, int h) {
		ResizeableMatrix r = new ResizeableMatrix(w, h, defaultValue);
		for (int px = x; px < x + w; px++) {
			for (int py = y; py < y + h; py++) {
				if (containsPoint(px, py)) {
					r.set(px - x, py - y, get(px, py));
				}
			}
		}
		return r;
	}

	private void populateList(ArrayList l, int newSize, boolean isValue) {
		populateList(l, newSize, -1, isValue);
	}

	private void populateList(ArrayList l, int newSize, int insertionIndex, boolean isValue) {
		T dv;
		if (defaultValue instanceof Collection) {
			dv = (T) ((ArrayList) defaultValue).clone();
		} else {
			dv = defaultValue;
		}
		for (int i = l.size(); i < newSize; i++) {
			if (insertionIndex == -1) {
				l.add((isValue) ? dv : new ArrayList<T>());
			} else {
				l.add(insertionIndex, (isValue) ? dv : new ArrayList<T>());
			}
		}
	}

	public void set(int x, int y, T value) {
		list.get(x - originX).set(y - originY, value);
	}

	public T get(int x, int y) {
		return list.get(x - originX).get(y - originY);
	}

	public T get(Point p) {
		return get(p.x, p.y);
	}

	public void resizeMatrix(int newWidth, int newHeight) {
		for (int i = list.size() - 1; i >= 0; i--) {
			if (i >= newWidth) {
				list.remove(i);
			}
		}
		for (int i = 0; i < list.size(); i++) {
			ArrayList<T> l = list.get(i);
			for (int j = l.size(); j >= 0; j--) {
				if (j >= newHeight) {
					l.remove(j);
				}
			}
		}
		//removal done, now ensure size if width or height was upped
		populateList(list, newWidth, false);
		for (int i = 0; i < newWidth; i++) {
			populateList(list.get(i), newHeight, true);
		}
	}

	public void addColumn() {
		addColumns(1);
	}

	public void addColumns(int count) {
		int origHeight = (list.size() > 0) ? list.get(0).size() : 0;
		for (int i = 0; i < count; i++) {
			list.add(new ArrayList<>());
			populateList(list.get(list.size() - 1), origHeight, true);
		}
	}

	public void addRow() {
		addRows(1);
	}

	public void addRows(int count) {
		int origHeight = (list.size() > 0) ? list.get(0).size() : 0;
		for (int i = 0; i < list.size(); i++) {
			populateList(list.get(i), origHeight + count, true);
		}
	}

	public void removeColumn() {
		removeColumns(1);
	}

	public void removeColumns(int count) {
		for (int i = 0; i < count; i++) {
			list.remove(list.size() - 1);
		}
	}

	public void removeRow() {
		removeRows(1);
	}

	public void removeRows(int count) {
		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < count; j++) {
				list.get(i).remove(list.get(i).size() - 1);
			}
		}
	}

	public boolean containsPoint(int x, int y) {
		x -= originX;
		y -= originY;
		if (x >= 0 && x < list.size()) {
			if (y >= 0 && y < list.get(x).size()) {
				return true;
			}
		}
		return false;
	}

	public void expandMatrixToPoint(int x, int y) {
		if (!containsPoint(x, y)) {
			int tx = x - originX;
			int ty = y - originY;
			int addIdxX = tx < 0 ? 0 : -1;
			int addIdxY = ty < 0 ? 0 : -1;
			int newWidth = tx < 0 ? getWidth() - tx : Math.max(getWidth(), tx + 1);
			int newHeight = ty < 0 ? getHeight() - ty : Math.max(getHeight(), ty + 1);
			if (tx < 0) {
				originX += tx;
			}
			if (ty < 0) {
				originY += ty;
			}
			populateList(list, newWidth, addIdxX, false);
			for (ArrayList col : list) {
				populateList(col, newHeight, addIdxY, true);
			}
		}
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int currentX = originX;
			private int currentY = originY;

			@Override
			public boolean hasNext() {
				return (currentX - originX < getWidth()) && (currentY - originY < getHeight());
			}

			@Override
			public T next() {
				T next = get(currentX, currentY);
				currentX++;
				if (currentX - originX == getWidth()) {
					currentX = originX;
					currentY++;
				}
				return next;
			}
		};
	}
}
