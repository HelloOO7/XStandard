package ctrmap.stdlib.gui;

import ctrmap.stdlib.CMStdLibPrefs;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JRadioButton;

public class ActionSelector extends javax.swing.JDialog {

	private Preferences lastOptPrefs = CMStdLibPrefs.node("CTRMapActionSelector");

	private Map<String, Object> acmdUserDataMap = new HashMap<>();
	private Map<String, Integer> acmdIndexMap = new HashMap<>();
	private boolean dialogStateCancelled = false;
	private int actionHash = 131;

	public ActionSelector(java.awt.Dialog parent, boolean modal, ASelAction... actions) {
		super(parent, modal);
		aSelInit(actions);
		setLocationRelativeTo(parent);
	}

	public ActionSelector(java.awt.Frame parent, boolean modal, String title, ASelAction... actions) {
		super(parent, modal);
		aSelInit(actions);
		setLocationRelativeTo(parent);
		if (title != null) {
			setTitle(title);
		}
	}

	public ActionSelector(java.awt.Frame parent, boolean modal, ASelAction... actions) {
		this(parent, modal, null, actions);
	}

	private void aSelInit(ASelAction... actions) {
		initComponents();

		getRootPane().setDefaultButton(btnConfirm);

		for (ASelAction act : actions) {
			actionHash = 31 * actionHash + act.actionName.hashCode();
		}

		int targetSelectionIndex = lastOptPrefs.getInt(String.valueOf(actionHash), 0);

		int idx = 0;
		for (ASelAction act : actions) {
			JRadioButton btn = new JRadioButton(act.actionName);
			btn.setActionCommand(act.actionName);
			acmdUserDataMap.put(act.actionName, act.actionObj);
			acmdIndexMap.put(act.actionName, idx);
			actionsGroup.add(btn);
			actionsPanel.add(btn);
			if (idx == targetSelectionIndex) {
				btn.setSelected(true);
			}
			idx++;
		}
		setSize(getPreferredSize());
	}

	public Object getSelectedUserObj() {
		if (!dialogStateCancelled) {
			return acmdUserDataMap.get(actionsGroup.getSelection().getActionCommand());
		}
		return null;
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        actionsGroup = new javax.swing.ButtonGroup();
        btnConfirm = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        actionsPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Choose an action");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        btnConfirm.setText("Confirm");
        btnConfirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfirmActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        actionsPanel.setLayout(new javax.swing.BoxLayout(actionsPanel, javax.swing.BoxLayout.PAGE_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 81, Short.MAX_VALUE)
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnConfirm))
                    .addComponent(actionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(actionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConfirm)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
		dialogStateCancelled = true;
		dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmActionPerformed
		lastOptPrefs.putInt(String.valueOf(actionHash), acmdIndexMap.get(actionsGroup.getSelection().getActionCommand()));
		dispose();
    }//GEN-LAST:event_btnConfirmActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
		btnCancelActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

	public static class ASelAction {

		private String actionName;
		private Object actionObj;

		public ASelAction(String actionName, Object actionObj) {
			this.actionName = actionName;
			this.actionObj = actionObj;
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup actionsGroup;
    private javax.swing.JPanel actionsPanel;
    protected javax.swing.JButton btnCancel;
    protected javax.swing.JButton btnConfirm;
    // End of variables declaration//GEN-END:variables
}
