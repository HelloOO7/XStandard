package xstandard.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class ThreadingUtils {

	public static void runOnNewThread(Runnable job) {
		new Thread(job).start();
	}

	public static void shutdownWaitService(ExecutorService service) {
		try {
			service.shutdown();
			service.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException ex) {
			Logger.getLogger(ThreadingUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void runOnEDT(Runnable r) {
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			SwingUtilities.invokeLater(r);
		}
	}
}
