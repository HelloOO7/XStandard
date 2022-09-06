package xstandard.math;

/**
 * Class for calculating common physics equations for uniformly accelerated motion.
 */
public class UniformlyAcceleratedMotion {

	/**
	 * Calculates the distance after t seconds of uniformly accelerated motion at acceleration a.
	 * @param a Acceleration.
	 * @param t Time.
	 * @return Distance.
	 */
	public static double calcUniformlyAcceleratedDistance(double a, double t) {
		return calcUniformlyAcceleratedDistance(0, a, t);
	}

	/**
	 * Calculates the distance after t seconds of uniformly accelerated motion at acceleration a with initial velocity v0.
	 * @param v0 Initial velocity.
	 * @param a Acceleration.
	 * @param t Time.
	 * @return Distance.
	 */
	public static double calcUniformlyAcceleratedDistance(double v0, double a, double t) {
		//s = 1/2*a*t^2.... they teach this stuff in high schools
		return v0 * t + 0.5 * a * t * t;
	}

	/**
	 * Calculates the acceleration needed to cover s meters distance in T seconds.
	 * @param s Distance.
	 * @param T Time.
	 * @return Acceleration.
	 */
	public static double calcUniformAcceleration(double s, double T) {
		//derived from the formula above... but like... I don't have to say that, you did pay attention in physics class, did you not?
		return 2 * s / Math.sqrt(T);
	}

	/**
	 * Calculates the final velocity after T seconds of accelerating at acceleration a.
	 * @param a Acceleration.
	 * @param T Time.
	 * @return Final velocity.
	 */
	public static double calcUniformAccelerationFinalVelocity(double a, double T) {
		return a * T;
	}

	public static double exCalcUniformAccelerationAndDeccelerationForInOutSlopeAndTime(double S, double T, double slope) {
		//Derived this on paper, sorry
		if (slope == 0){
			return calcUniformAcceleration(S, T);
		}
		return S / (slope * (T - slope));
	}

	public static double exCalcDistanceInSlopedCurve(double T, double S, double slope, double t) {
		double acc = exCalcUniformAccelerationAndDeccelerationForInOutSlopeAndTime(S, T, slope);
		double constV = calcUniformAccelerationFinalVelocity(acc, slope);
		return exCalcDistanceInSlopedCurve(t, S, slope, t, constV, acc);
	}

	public static double exCalcDistanceInSlopedCurve(double T, double S, double slope, double a, double vf, double t) {
		double dist;
		if (t < slope) {
			dist = calcUniformlyAcceleratedDistance(a, t);
		} else if (t > T - slope) {
			dist = vf * (T - 1.5 * slope) + calcUniformlyAcceleratedDistance(vf, -a, t - (T - slope));
		} else {
			dist = vf * (t - 0.5 * slope);
		}
		return dist;
	}
}
