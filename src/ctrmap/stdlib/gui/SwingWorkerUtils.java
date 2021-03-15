package ctrmap.stdlib.gui;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

public class SwingWorkerUtils {

	public static void runOnNewThread(Runnable job){
		new Thread(job).start();
	}
	
	public static SwingWorker executeJob(Runnable job) {
		return executeJob(job, false);
	}

	public static SwingWorker executeJob(Runnable job, boolean join) {
		SwingWorker worker = new SwingWorker() {
			@Override
			protected Object doInBackground() throws Exception {
				job.run();
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
				} catch (InterruptedException | ExecutionException ex) {
					DialogUtils.showExceptionTraceDialog(ex);
				}
			}
		};
		worker.execute();
		if (join) {
			try {
				worker.get();
			} catch (InterruptedException | ExecutionException ex) {
				Logger.getLogger(SwingWorkerUtils.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return worker;
	}

	public static SwingWorker prepareJob(Runnable job) {
		return prepareJob(job, null);
	}

	public static SwingWorker prepareJob(Runnable job, Runnable done) {
		SwingWorker worker = new SwingWorker() {
			@Override
			protected Object doInBackground() throws Exception {
				job.run();
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
				} catch (InterruptedException | ExecutionException ex) {
					DialogUtils.showExceptionTraceDialog(ex);
					ex.printStackTrace();
				}
				if (done != null){
					done.run();
				}
			}
		};
		return worker;
	}
}
