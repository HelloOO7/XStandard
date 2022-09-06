package xstandard.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class ComponentList<T extends JComponent> extends JPanel implements MouseListener, Iterable<T> {

	private final Color SELECTION_COLOR = new Color(0x6C, 0xB5, 0xFF);

	private List<T> elements = new ArrayList<>();

	public ComponentList() {
		super();
		setBackground(Color.WHITE);
		setOpaque(true);
		BoxLayout lyt = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(lyt);
		addMouseListener(this);
	}

	public void addElement(T elem) {
		addElement(elements.size(), elem);
	}
	
	public void addElement(int idx, T elem) {
		elements.add(idx, elem);
		add(elem, idx);
		Dimension elemSize = elem.getPreferredSize();
		elemSize.width = 0;
		elem.setSize(elemSize);
		elem.setPreferredSize(elemSize);
		elem.setMinimumSize(new Dimension(0, elemSize.height));
		elem.setMaximumSize(new Dimension(Integer.MAX_VALUE, elemSize.height));
		revalidate();
		setSelectedElement(null);
	}

	public void removeElement(T elem) {
		remove(elem);
		elements.remove(elem);
		revalidate();
		repaint();
	}
	
	public void clear(){
		removeAll();
		elements.clear();
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		T selectedElement = null;
		for (T elem : elements) {
			Point pos = elem.getLocation();
			Dimension dim = elem.getSize();
			if (x >= pos.x && x < pos.x + dim.width && y >= pos.y && y <= pos.y + dim.height) {
				selectedElement = elem;
				break;
			}
		}
		setSelectedElement(selectedElement);
	}
	
	public void setSelectedElement(T selectedElement){
		for (T nonSelect : elements) {
			nonSelect.setOpaque(nonSelect == selectedElement);
		}
		if (selectedElement != null) {
			selectedElement.setBackground(SELECTION_COLOR);
		}
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public Iterator<T> iterator() {
		return elements.iterator();
	}
}
