package ctrmap.stdlib.util;

public interface ProgressMonitor {
	public void setProgressPercentage(int value);
	public void setProgressTitle(String value);
	public void setProgressSubTitle(String value);
	
	public static class DummyProgressMonitor implements ProgressMonitor{

		@Override
		public void setProgressPercentage(int value) {
		}

		@Override
		public void setProgressTitle(String value) {
		}

		@Override
		public void setProgressSubTitle(String value) {
		}
		
	}
}
