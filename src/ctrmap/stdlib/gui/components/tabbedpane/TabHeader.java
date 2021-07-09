package ctrmap.stdlib.gui.components.tabbedpane;

public class TabHeader extends javax.swing.JPanel {

	private TabbedPaneTab tab;
	JTabbedPaneEx pane;
			
	public TabHeader(TabbedPaneTab tab, JTabbedPaneEx pane) {
		initComponents();
		
		this.tab = tab;
		tabLabel.tab = tab;
		btnCloseTab.tab = tab;
	}
	
	TabBoundLabel getLabel(){
		return tabLabel;
	}
	
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabLabel = new ctrmap.stdlib.gui.components.tabbedpane.TabBoundLabel();
        btnCloseTab = new ctrmap.stdlib.gui.components.tabbedpane.TabCloseButton();

        setOpaque(false);

        tabLabel.setText("Title");

        btnCloseTab.setText("x");
        btnCloseTab.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tabLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCloseTab, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnCloseTab, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(tabLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ctrmap.stdlib.gui.components.tabbedpane.TabCloseButton btnCloseTab;
    private ctrmap.stdlib.gui.components.tabbedpane.TabBoundLabel tabLabel;
    // End of variables declaration//GEN-END:variables
}
