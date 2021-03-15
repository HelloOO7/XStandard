
package ctrmap.stdlib.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 */
public class DnDHelper {
	public static void addFileDropTarget(JComponent comp, FileDropListener l){
		comp.setDropTarget(new DropTarget() {
			@Override
			public synchronized void drop(DropTargetDropEvent evt) {
				if (evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					try {
						evt.acceptDrop(DnDConstants.ACTION_COPY);
						List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
						l.acceptDrop(droppedFiles);
					} catch (UnsupportedFlavorException | IOException ex) {
						DialogUtils.showExceptionTraceDialog(ex);
					}
				}
			}
		});
	}
	
	public static interface FileDropListener {
		public void acceptDrop(List<File> files);
	}
}
