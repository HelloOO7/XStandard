package xstandard.gui.components.tabbedpane;

import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JButton;

public class TabCloseButton extends JButton {

	TabbedPaneTab tab;

	public TabCloseButton() {
		setContentAreaFilled(false);
		setBorderPainted(false);
		setBorder(null);
		setMargin(new Insets(0, 0, 0, 0));
		setOpaque(false);

		addActionListener((e) -> {
			if (tab != null) {
				tab.remove();
			}
		});
	}

	@Override
	public String getText() {
		return getIcon() == null ? "X" : null;
	}

	@Override
	public Icon getIcon() {
		if (tab != null) {
			return tab.pane.getCloseIcon();
		}
		return null;
	}

	@Override
	public Icon getRolloverIcon() {
		if (tab != null) {
			return tab.pane.getCloseIconRollover();
		}
		return null;
	}

	@Override
	public Icon getPressedIcon() {
		if (tab != null) {
			return tab.pane.getCloseIconPressed();
		}
		return null;
	}

	@Override
	public boolean isRolloverEnabled() {
		return getRolloverIcon() != null;
	}

	@Override
	public boolean isVisible() {
		if (tab != null) {
			return tab.pane.isCloseable();
		}
		return false;
	}
}
