package ctrmap.stdlib.math;

/**
 * TODO: Exterminate this class.
 */
public class InterpolationTimer {
	private double duration;
	
	private double start = 0f;
	private double time = start;

	public InterpolationTimer(double duration){
		this.duration = duration;
	}
	
	public float getInterpolationWeight(){
		if (duration == 0f){
			return 1f;
		}
		return Math.max(0f, Math.min(1f, (float)((time - start) / (duration))));
	}
	
	public void setToEndTime(){
		time = start + duration;
	}
	
	public void setTime(double time){
		this.time = time;
	}
	
	public double getTime() {
		return time;
	}
	
	public void advanceTime(double time){
		this.time += time;
	}
	
	public void setDuration(double duration){
		this.duration = duration;
	}
	
	public void forceStartTime(double st){
		start = st;
	}
	
	public boolean isTimerEnd(){
		return time > start + duration;
	}
}
