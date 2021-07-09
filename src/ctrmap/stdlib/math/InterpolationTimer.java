package ctrmap.stdlib.math;

/**
 * TODO: Exterminate this class.
 */
public class InterpolationTimer {
	private double duration;
	private float slope;
	private double start;
	
	private boolean frozen = false;
	private double frozenValue;
	
	private float acc;
	private float constV;
	
	public InterpolationTimer(double duration, float slope){
		this.duration = duration;
		this.slope = slope;
		start = System.currentTimeMillis();
		acc = (float)UniformlyAcceleratedMotion.exCalcUniformAccelerationAndDeccelerationForInOutSlopeAndTime(1, 1, slope);
		constV = (float)UniformlyAcceleratedMotion.calcUniformAccelerationFinalVelocity(acc, slope);
		if (slope == 0){
			constV = 1;
		}
	}
	
	public float getInterpolationWeight(){
		double timerValue = frozen ? frozenValue : System.currentTimeMillis();
		double weightBase = Math.min(1.0, (timerValue - start) / (double)duration);
		if (duration == 0){
			weightBase = 1f;
		}
		double actualWeight;
		actualWeight = UniformlyAcceleratedMotion.exCalcDistanceInSlopedCurve(1, 1, slope, acc, constV, weightBase);
		return (float)actualWeight;
	}
	
	public void forceTime(double time){
		freeze();
		frozenValue = time;
	}
	
	public void forceStartTime(double st){
		start = st;
	}
	
	public void freeze(){
		frozen = true;
		frozenValue = System.currentTimeMillis();
	}
	
	public boolean isFrozen(){
		return frozen;
	}
	
	public void unfreeze(){
		frozen = false;
		start = (frozenValue - start) + System.currentTimeMillis();
	}
	
	public boolean isTimerEnd(){
		return !frozen && (System.currentTimeMillis() > start + duration);
	}
}
