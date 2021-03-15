package ctrmap.stdlib.math;

public class UniformlyAcceleratedMotion {

	public static double calcUniformlyAcceleratedDistance(double a, double t) {
		return calcUniformlyAcceleratedDistance(0, a, t);
	}

	public static double calcUniformlyAcceleratedDistance(double v0, double a, double t) {
		//s = 1/2*a*t^2.... they teach this stuff in high schools
		return v0 * t + 0.5 * a * t * t;
	}

	public static double calcUniformAcceleration(double s, double T) {
		//derived from the formula above... but like... I don't have to say that, you did pay attention in physics class, did you not?
		return 2 * s / Math.sqrt(T);
	}

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
