package ctrmap.stdlib.gui.components.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.util.EventObject;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.DefaultTreeCellRenderer;

public class CheckboxTreeCell extends javax.swing.JPanel implements CustomJTreeCellRenderer {

	private static final DefaultTreeCellRenderer DEFAULT_TREECELL_RENDERER_INSTANCE_FOR_COLORS = new DefaultTreeCellRenderer();
	
	/**
	 * Is the value currently selected.
	 */
	protected boolean selected;
	/**
	 * True if has focus.
	 */
	protected boolean hasFocus;
	/**
	 * True if draws focus border around icon as well.
	 */
	private boolean drawsFocusBorderAroundIcon;
	/**
	 * If true, a dashed line is drawn as the focus indicator.
	 */
	private boolean drawDashedFocusIndicator = true;

	// If drawDashedFocusIndicator is true, the following are used.
	/**
	 * Background color of the tree.
	 */
	private Color treeBGColor = Color.WHITE;
	/**
	 * Color to draw the focus indicator in, determined from the background. color.
	 */
	private Color focusBGColor = Color.BLACK;

	// Colors
	/**
	 * Color to use for the foreground for selected nodes.
	 */
	protected Color textSelectionColor = DEFAULT_TREECELL_RENDERER_INSTANCE_FOR_COLORS.getTextSelectionColor();

	/**
	 * Color to use for the foreground for non-selected nodes.
	 */
	protected Color textNonSelectionColor = DEFAULT_TREECELL_RENDERER_INSTANCE_FOR_COLORS.getTextNonSelectionColor();

	/**
	 * Color to use for the background when a node is selected.
	 */
	protected Color backgroundSelectionColor = DEFAULT_TREECELL_RENDERER_INSTANCE_FOR_COLORS.getBackgroundSelectionColor();

	/**
	 * Color to use for the background when the node isn't selected.
	 */
	protected Color backgroundNonSelectionColor = DEFAULT_TREECELL_RENDERER_INSTANCE_FOR_COLORS.getBackgroundNonSelectionColor();

	/**
	 * Color to use for the focus indicator when the node has focus.
	 */
	protected Color borderSelectionColor = DEFAULT_TREECELL_RENDERER_INSTANCE_FOR_COLORS.getBorderSelectionColor();

	private boolean isDropCell;
	private boolean fillBackground = true;

	public CheckboxTreeCell() {
		initComponents();
	}
	
	public void addActionListener(ActionListener listener) {
		checkbox.addActionListener(listener);
	}

	public void setChecked(boolean v) {
		checkbox.setSelected(v);
	}

	public boolean isChecked() {
		return checkbox.isSelected();
	}

	@Override
	public String getText() {
		return label.getText();
	}

	@Override
	public void setText(String text) {
		label.setText(text);
	}

	@Override
	public Icon getIcon() {
		return label.getIcon();
	}

	@Override
	public void setIcon(Icon icn) {
		label.setIcon(icn);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {
        this.hasFocus = hasFocus;

		Color fg = null;
        isDropCell = false;

        JTree.DropLocation dropLocation = tree.getDropLocation();
        if (dropLocation != null
                && dropLocation.getChildIndex() == -1
                && tree.getRowForPath(dropLocation.getPath()) == row) {

            Color col = null;
            if (col != null) {
                fg = col;
            } else {
                fg = textSelectionColor;
            }

            isDropCell = true;
        } else if (sel) {
            fg = textSelectionColor;
        } else {
            fg = textNonSelectionColor;
        }

        label.setForeground(fg);

        Icon icon = getIcon();

        if (!tree.isEnabled()) {
            setEnabled(false);
        } else {
            setEnabled(true);
            setIcon(icon);
        }
        setComponentOrientation(tree.getComponentOrientation());

        selected = sel;

        return this;
    }

	@Override
	public void paint(Graphics g) {
		Color bColor = null;

		if (isDropCell) {
			if (bColor == null) {
				bColor = backgroundSelectionColor;
			}
		} else if (selected) {
			bColor = backgroundSelectionColor;
		} else {
			bColor = backgroundNonSelectionColor;
			if (bColor == null) {
				bColor = getBackground();
			}
		}

		int imageOffset = -1;
		if (bColor != null && fillBackground) {
			imageOffset = getLabelStart();
			g.setColor(bColor);
			if (getComponentOrientation().isLeftToRight()) {
				g.fillRect(imageOffset, 0, getWidth() - imageOffset,
					getHeight());
			} else {
				g.fillRect(0, 0, getWidth() - imageOffset,
					getHeight());
			}
		}

		if (hasFocus) {
			if (drawsFocusBorderAroundIcon) {
				imageOffset = 0;
			} else if (imageOffset == -1) {
				imageOffset = getLabelStart();
			}
			if (getComponentOrientation().isLeftToRight()) {
				paintFocus(g, imageOffset, 0, getWidth() - imageOffset,
					getHeight(), bColor);
			} else {
				paintFocus(g, 0, 0, getWidth() - imageOffset, getHeight(), bColor);
			}
		}
		super.paint(g);
	}

	private int getLabelStart() {
		Icon currentI = label.getIcon();
		if (currentI != null && getText() != null) {
			return currentI.getIconWidth() + Math.max(0, 3) + checkbox.getWidth();
		}
		return 0;
	}

	private void paintFocus(Graphics g, int x, int y, int w, int h, Color notColor) {
		Color bsColor = borderSelectionColor;

		if (bsColor != null && (selected || !drawDashedFocusIndicator)) {
			g.setColor(bsColor);
			g.drawRect(x, y, w - 1, h - 1);
		}
		if (drawDashedFocusIndicator && notColor != null) {
			if (treeBGColor != notColor) {
				treeBGColor = notColor;
				focusBGColor = new Color(~notColor.getRGB());
			}
			g.setColor(focusBGColor);
			BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
		}
	}
	
	@Override
	public boolean isEditable(EventObject e) {
		return true;
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        checkbox = new javax.swing.JCheckBox();
        label = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setOpaque(false);

        checkbox.setOpaque(false);

        label.setText("<Label>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(checkbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(checkbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox checkbox;
    private javax.swing.JLabel label;
    // End of variables declaration//GEN-END:variables
}
