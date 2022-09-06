package xstandard.math;

import xstandard.math.vec.Vec3f;

/**
 * Math utilities and equations.
 */
public class MathEx {

	public static final float PI = (float) Math.PI;
	public static final float PI_NEG = -PI;
	public static final float HALF_PI = PI / 2f;
	public static final float ONE_THIRD_PI = PI / 3f;
	public static final float QUARTER_PI = PI / 4f;
	public static final float HALF_PI_NEG = -HALF_PI;
	public static final float TWO_PI = 2 * PI;
	public static final float RADIANS_TO_DEGREES = 57.2957795f;
	public static final float DEGREES_TO_RADIANS = 0.01745329f;

	public static float toDegreesf(float rad) {
		return rad * RADIANS_TO_DEGREES;
	}

	public static float toRadiansf(float deg) {
		return deg * DEGREES_TO_RADIANS;
	}

	public static float makeAnglePositiveDeg(float angle) {
		angle %= 360f;
		if (angle < 0) {
			angle += 360f;
		}
		return angle;
	}

	public static float angleDiffNormalizedRad(float angle1, float angle2) {
		float diff = Math.abs(angle2 - angle1);
		if (diff > PI) {
			return TWO_PI - diff;
		}
		return diff;
	}

	/**
	 * Returns an angle in a triangle calculated using the law of cosines.
	 *
	 * @param adj1 The first side of the triangle that is adjacent to the angle.
	 * @param adj2 The second side of the triangle that is adjacent to the angle.
	 * @param opp The side of the triangle opposite to the angle.
	 * @return The angle, in radians.
	 */
	public static float getAngleByCosineLaw(float adj1, float adj2, float opp) {
		return (float) Math.acos(cosineLaw(adj1, adj2, opp));
	}

	/**
	 * Returns the cosine of an angle in a triangle calculated using the law of cosines.
	 *
	 * @param adj1 The first side of the triangle that is adjacent to the angle.
	 * @param adj2 The second side of the triangle that is adjacent to the angle.
	 * @param opp The side of the triangle opposite to the angle.
	 * @return The cosine of the angle, in radians.
	 */
	public static float cosineLaw(float adj1, float adj2, float opp) {
		return (adj1 * adj1 + adj2 * adj2 - opp * opp) / (2 * adj1 * adj2);
	}

	/**
	 * Calculates an angle of a triangle using the law of sines, knowing the side opposite to it and an arbitrary other side/angle pair.
	 *
	 * @param side1 A side of the triangle that is not opposite to the angle to be calculated.
	 * @param angle1 The angle opposite to side1.
	 * @param side2 Side of the triangle that is opposite to the calculated angle.
	 * @return The angle in radians.
	 */
	public static float getAngleBySineLaw(float side1, float angle1, float side2) {
		return (float) Math.asin(sineLaw(side1, angle1, side2));
	}

	/**
	 * Calculates the sine of an angle of a triangle using the law of sines, knowing the side opposite to it and an arbitrary other side/angle pair.
	 *
	 * @param side1 A side of the triangle that is not opposite to the angle to be calculated.
	 * @param angle1 The angle opposite to side1.
	 * @param side2 Side of the triangle that is opposite to the calculated angle.
	 * @return The sine of the angle, in radians.
	 */
	public static float sineLaw(float side1, float angle1, float side2) {
		return (float) (side2 / (side1 / Math.sin(angle1)));
	}

	/*
	From: https://stackoverflow.com/questions/31225062/rotating-a-vector-by-angle-and-axis-in-java
	 */
	public static Vec3f noGlRotatef(Vec3f vec, Vec3f axis, double theta) {
		float x;
		float y;
		float z;
		float u;
		float v;
		float w;
		x = vec.x;
		y = vec.y;
		z = vec.z;
		u = axis.x;
		v = axis.y;
		w = axis.z;
		float xPrime = (float) (u * (u * x + v * y + w * z) * (1.0 - Math.cos(theta)) + x * Math.cos(theta) + (-w * y + v * z) * Math.sin(theta));
		float yPrime = (float) (v * (u * x + v * y + w * z) * (1.0 - Math.cos(theta)) + y * Math.cos(theta) + (w * x - u * z) * Math.sin(theta));
		float zPrime = (float) (w * (u * x + v * y + w * z) * (1.0 - Math.cos(theta)) + z * Math.cos(theta) + (-v * x + u * y) * Math.sin(theta));
		return new Vec3f(xPrime, yPrime, zPrime);
	}

	/**
	 * Compares two floating point values with 0.1 tolerance.
	 *
	 * @param f0
	 * @param f1
	 * @return True if the floats are roughly equal.
	 */
	public static boolean impreciseFloatEquals(float f0, float f1) {
		return impreciseFloatEquals(f0, f1, 0.01F); //default precision
	}

	/**
	 * Compares two floating values with a given threshold.
	 *
	 * @param f0
	 * @param f1
	 * @param precision Maximum deviation of the values from each other.
	 * @return True if the difference of the input values is negligible, given 'precision'.
	 */
	public static boolean impreciseFloatEquals(float f0, float f1, float precision) {
		return Math.abs(f0 - f1) < precision;
	}

	public static int min(int... integers) {
		int min = Integer.MAX_VALUE;
		for (int i : integers) {
			if (i < min) {
				min = i;
			}
		}
		return min;
	}
	
	public static int max(int... integers) {
		int max = Integer.MIN_VALUE;
		for (int i : integers) {
			if (i > max) {
				max = i;
			}
		}
		return max;
	}
	
	public static float min(float... floats) {
		float max = Float.MAX_VALUE;
		for (float i : floats) {
			if (i < max) {
				max = i;
			}
		}
		return max;
	}
	
	public static float max(float... floats) {
		float max = -Float.MAX_VALUE;
		for (float i : floats) {
			if (i > max) {
				max = i;
			}
		}
		return max;
	}

	/**
	 * Calculates the arithmetic average of an arbitrary number of integers.
	 *
	 * @param sources Integers to calculate the average of.
	 * @return Average of 'sources'.
	 */
	public static int average(int... sources) {
		int sum = 0;
		for (int i = 0; i < sources.length; i++) {
			sum += sources[i];
		}
		return sum / sources.length;
	}

	public static float clamp(float min, float max, float value) {
		if (value > max) {
			return max;
		}
		if (value < min) {
			return min;
		}
		return value;
	}

	/**
	 * Clamps a continuous repeating integer flow so that all values exceeding the maximum are set to the minimum, and vice versa.
	 *
	 * @param value The value to clamp.
	 * @param min Minimum value.
	 * @param max Maximum value.
	 * @param isMaxInclusive True if 'value' can be equal to 'max'.
	 * @return Clamped value.
	 */
	public static int clampIntegerFlow(int value, int min, int max, boolean isMaxInclusive) {
		int actualMaximum = max - (isMaxInclusive ? 0 : 1);
		if (value > actualMaximum) {
			return min;
		}
		if (value < min) {
			return actualMaximum;
		}
		return value;
	}

	public static int padInteger(int v, int padTo) {
		v += (padTo - (v % padTo)) % padTo;
		return v;
	}

	public static int padIntegerDownPow2(int v, int bitCount) {
		return v & ~((1 << bitCount) - 1);
	}
}
