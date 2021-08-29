package ctrmap.stdlib.gui;

import ctrmap.stdlib.util.ProgressMonitor;
import java.awt.Dimension;

public class LoadingDialog extends javax.swing.JDialog implements ProgressMonitor{

	public LoadingDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		setLocationRelativeTo(parent);
	}
	
	public LoadingDialog(java.awt.Frame parent, boolean modal, String title) {
		this(parent, modal);
		setProgressTitle(title);
	}
	
	public void showDialog(){
		setVisible(true);
	}
	
	public void close(){
		setVisible(false);
	}
	
	@Override
	public void setProgressPercentage(int value) {
		bar.setValue(value);
	}

	@Override
	public void setProgressTitle(String value) {
		desc.setText(value);
		Dimension ps = getPreferredSize();
		Dimension descPS = desc.getPreferredSize();
		setSize(new Dimension(Math.max(ps.width, descPS.width + 20), ps.height));
	}

	@Override
	public void setProgressSubTitle(String value) {
		bar.setString(value);
	}
	
	public void setProgressBarIsFake(boolean value){
		bar.setIndeterminate(value);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel = new javax.swing.JPanel();
        desc = new javax.swing.JLabel();
        bar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        panel.setBorder(new javax.swing.border.LineBorder(java.awt.SystemColor.windowBorder, 2, true));

        desc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        desc.setText("Sample text");

        bar.setForeground(new java.awt.Color(51, 204, 0));
        bar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        bar.setString("");
        bar.setStringPainted(true);

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bar, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                    .addComponent(desc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(desc)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar bar;
    private javax.swing.JLabel desc;
    private javax.swing.JPanel panel;
    // End of variables declaration//GEN-END:variables
}
