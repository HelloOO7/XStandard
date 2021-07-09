package ctrmap.stdlib.util;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A resizeable 2D array.
 * @param <T> Element type of the array.
 */
public class ResizeableMatrix<T> implements Iterable<T> {

	private int originX = 0;
	private int originY = 0;

	public ArrayList<ArrayList<T>> list = new ArrayList<>();
	private T defaultValue;

	/**
	 * Creates a dynamic 2D array with an initial width, height and a default value reference.
	 * @param width The matrix's initial width.
	 * @param height The matrix's initial height.
	 * @param defaultValue The value to use for uninitialized fields.
	 */
	public ResizeableMatrix(int width, int height, T defaultValue) {
		this.defaultValue = defaultValue;
		populateList(list, width, false);
		for (int i = 0; i < width; i++) {
			populateList(list.get(i), height, true);
		}
	}

	/**
	 * Merges the contents of mtx with this matrix.
	 * If the element is of the Collection interface, its elements will be merged instead of replacing.
	 * @param mtx The matrix to merge with this one.
	 */
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
	
	/**
	 * Returns a list of all elements of a row.
	 * @param idx Index of the row.
	 * @return A list with the row elements. Changes to the list will not be reflected in the matrix.
	 */
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
	
	/**
	 * Returns the entire matrix row-wise.
	 * @return A list of lists containing all of the matrix's rows. Changes to the lists will not be reflected in the matrix.
	 */
	public List<T> getRows(){
		List<T> r = new ArrayList<>();
		for (int i = 0; i < getHeight(); i++){
			r.addAll(getRow(i + originY));
		}
		return r;
	}
	
	/**
	 * Returns a list of all elements of a column.
	 * @param idx Index of the column.
	 * @return A list with the column elements. Changes to the list will not be reflected in the matrix.
	 */
	public List<T> getColumn(int idx){
		List<T> r = new ArrayList<>();
		int relocIdx = idx - originX;
		if (relocIdx < 0 || relocIdx >= getWidth()){
			return null;
		}
		r.addAll(list.get(relocIdx));
		return r;
	}

	/**
	 * Removes all elements from the matrix.
	 */
	public void clear() {
		list.clear();
		originX = 0;
		originY = 0;
	}

	/**
	 * Gets the width of the matrix.
	 * @return The matrix's width.
	 */
	public int getWidth() {
		return list.size();
	}

	/**
	 * Gets the height of the matrix.
	 * @return The matrix's height.
	 */
	public int getHeight() {
		if (list.size() > 0) {
			return list.get(0).size();
		} else {
			return 0;
		}
	}

	/**
	 * Gets the origin point of the matrix.
	 * @return A Point object representing the horizontal/vertical offset of matrix data coordinates.
	 */
	public Point getOrigin() {
		return new Point(originX, originY);
	}

	/**
	 * Creates a "cut-out" sub-matrix of this matrix.
	 * @param x The horizontal offset of the sub-matrix in this matrix.
	 * @param y The vertical offset of the sub-matrix in this matrix.
	 * @param w The width of the sub-matrix.
	 * @param h The height of the sub-matrix.
	 * @return 
	 */
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

	/**
	 * Sets the matrix element at the given coordinates to the given value.
	 * @param x X coordinate of the element.
	 * @param y Y coordinate of the element.
	 * @param value 
	 */
	public void set(int x, int y, T value) {
		list.get(x - originX).set(y - originY, value);
	}

	/**
	 * Gets the element at the given coordinates.
	 * @param x X coordinate of the element.
	 * @param y Y coordinate of the element.
	 * @return Value of the matrix element at (x,y).
	 */
	public T get(int x, int y) {
		return list.get(x - originX).get(y - originY);
	}

	/**
	 * Gets the element at the given Point.
	 * @param p Point containing the (x,y) coordinates of the element.
	 * @return Value of the matrix element at (p.x, p.y).
	 */
	public T get(Point p) {
		return get(p.x, p.y);
	}

	/**
	 * Trims or expands the matrix to new dimensions.
	 * @param newWidth The new width of this matrix.
	 * @param newHeight The new height of this matrix.
	 */
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

	/**
	 * Adds a column to the matrix.
	 */
	public void addColumn() {
		addColumns(1);
	}

	/**
	 * Adds columns to the matrix.
	 * @param count Number of columns to add.
	 */
	public void addColumns(int count) {
		int origHeight = (list.size() > 0) ? list.get(0).size() : 0;
		for (int i = 0; i < count; i++) {
			list.add(new ArrayList<>());
			populateList(list.get(list.size() - 1), origHeight, true);
		}
	}

	/**
	 * Adds a row to the matrix.
	 */
	public void addRow() {
		addRows(1);
	}

	/**
	 * Adds rows to the matrix.
	 * @param count Number of columns to add.
	 */
	public void addRows(int count) {
		int origHeight = (list.size() > 0) ? list.get(0).size() : 0;
		for (int i = 0; i < list.size(); i++) {
			populateList(list.get(i), origHeight + count, true);
		}
	}

	/**
	 * Removes the last column of the matrix.
	 */
	public void removeColumn() {
		removeColumns(1);
	}

	/**
	 * Removes columns from the end of the matrix.
	 * @param count Number of columns to remove.
	 */
	public void removeColumns(int count) {
		for (int i = 0; i < count; i++) {
			list.remove(list.size() - 1);
		}
	}

	/**
	 * Removes a row from the end of the matrix.
	 */
	public void removeRow() {
		removeRows(1);
	}

	/**
	 * Removes rows from the end of the matrix.
	 * @param count Number of rows to remove.
	 */
	public void removeRows(int count) {
		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < count; j++) {
				list.get(i).remove(list.get(i).size() - 1);
			}
		}
	}

	/**
	 * Checks if a point is inside the matrix's boundaries.
	 * @param x X location of the point.
	 * @param y Y location of the point.
	 * @return True if the point is inside the matrix, false if it is outside.
	 */
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

	/**
	 * Ensures that the matrix contains a given point.
	 * @param x X coordinate of the point.
	 * @param y Y coordinate of the point.
	 */
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
