package ctrmap.stdlib.gui.components;

import ctrmap.stdlib.math.vec.RGBA;
import ctrmap.stdlib.math.vec.Vec3f;
import ctrmap.stdlib.util.ArraysEx;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NormalizedVec3fSliderEditor extends javax.swing.JPanel {

	private Vec3f vec = new Vec3f();

	private boolean allowChanges = false;

	private boolean allowsNegatives = false;
	
	private final JSlider[] SLIDERS = new JSlider[3];
	
	private List<VectorEditorListener> listeners = new ArrayList<>();

	public NormalizedVec3fSliderEditor() {
		initComponents();
		
		SLIDERS[0] = x;
		SLIDERS[1] = y;
		SLIDERS[2] = z;

		x.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (allowChanges) {
					float val = getFpFromSlider(x);
					if (Math.abs(vec.x - val) >= 0.001f) {
						vec.x = val;
						callListeners();
					}
				}
			}
		});

		y.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (allowChanges) {
					float val = getFpFromSlider(y);
					if (Math.abs(vec.y - val) >= 0.001f) {
						vec.y = val;
						callListeners();
					}
				}
			}
		});

		z.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (allowChanges) {
					float val = getFpFromSlider(z);
					if (Math.abs(vec.z - val) >= 0.001f) {
						vec.z = val;
						callListeners();
					}
				}
			}
		});
	}
	
	public void addListener(VectorEditorListener l) {
		ArraysEx.addIfNotNullOrContains(listeners, l);
	}

	public void loadVec(Vec3f vec) {
		if (vec == null) {
			this.vec = Vec3f.ZERO();
		} else {
			this.vec = vec;
		}
		refresh();
	}
	
	public Vec3f getVector() {
		return vec;
	}
	
	public RGBA getVecAsColor() {
		return new RGBA(vec.toVec4());
	}
	
	private void callListeners() {
		for (VectorEditorListener l : listeners) {
			l.onChanged();
		}
	}

	public void refresh() {
		allowChanges = false;
		setFpToSlider(vec.x, x);
		setFpToSlider(vec.y, y);
		setFpToSlider(vec.z, z);
		allowChanges = true;
	}
	
	public boolean getAllowsNegatives() {
		return allowsNegatives;
	}

	public void setAllowsNegatives(boolean value) {
		allowsNegatives = value;

		for (JSlider s : SLIDERS) {
			s.setMaximum(value ? 200 : 100);
		}
	}
	
	public void setRGBLabels(boolean value) {
		xLabel.setText(value ? "R" : "X");
		yLabel.setText(value ? "G" : "Y");
		zLabel.setText(value ? "B" : "Z");
	}

	public float getFpFromSlider(JSlider slider) {
		if (allowsNegatives) {
			return (slider.getValue() - 100f) / 200f;
		} else {
			return slider.getValue() / 100f;
		}
	}

	public void setFpToSlider(float value, JSlider slider) {
		if (allowsNegatives) {
			slider.setValue((int) (value * 200f + 100f));
		} else {
			slider.setValue((int) (value * 100f));
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        xLabel = new javax.swing.JLabel();
        yLabel = new javax.swing.JLabel();
        zLabel = new javax.swing.JLabel();
        x = new javax.swing.JSlider();
        y = new javax.swing.JSlider();
        z = new javax.swing.JSlider();

        xLabel.setForeground(new java.awt.Color(255, 0, 0));
        xLabel.setText("X");

        yLabel.setForeground(new java.awt.Color(0, 153, 0));
        yLabel.setText("Y");

        zLabel.setForeground(new java.awt.Color(0, 0, 255));
        zLabel.setText("Z");

        x.setMajorTickSpacing(100);
        x.setPaintTicks(true);
        x.setValue(0);

        y.setMajorTickSpacing(100);
        y.setPaintTicks(true);
        y.setValue(0);

        z.setMajorTickSpacing(100);
        z.setPaintTicks(true);
        z.setValue(0);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(xLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(yLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(z, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                    .addComponent(x, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(y, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(xLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(x, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(y, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(yLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider x;
    private javax.swing.JLabel xLabel;
    private javax.swing.JSlider y;
    private javax.swing.JLabel yLabel;
    private javax.swing.JSlider z;
    private javax.swing.JLabel zLabel;
    // End of variables declaration//GEN-END:variables
}
