package ctrmap.stdlib.math;

import ctrmap.stdlib.math.vec.Vec3f;

/**
 * Calculator for Hermite spline interpolation.
 */
public class HermiteInterpolation {

	public static Vec3f calc(Vec3f p0, Vec3f m0, Vec3f p1, Vec3f m1, float t) {
		return HermiteInterpolation.calc(p0, m0, p1, m1, t, new Vec3f());
	}

	public static Vec3f calc(Vec3f p0, Vec3f m0, Vec3f p1, Vec3f m1, float t, Vec3f dest) {
		dest.x = calc(p0.x, m0.x, p1.x, m1.x, t);
		dest.y = calc(p0.y, m0.y, p1.y, m1.y, t);
		dest.z = calc(p0.z, m0.z, p1.z, m1.z, t);
		return dest;
	}

	/**
	 * Calculates the Hermite interpolation of two values with their respective
	 * tangents and an interpolation weight.
	 *
	 * @param p0 The first value.
	 * @param m0 Tangent of the first value.
	 * @param p1 The second value.
	 * @param m1 Tangent of the second value.
	 * @param t Interpolation weight.
	 * @return P0(M0) to P1(M1) interpolated using Hermite interpolation with weight t.
	 */
	public static float calc(float p0, float m0, float p1, float m1, float t) {
		float t2 = t * t;
		float t3 = t2 * t;
		return (2 * t3 - 3 * t2 + 1) * p0
				+ (t3 - 2 * t2 + t) * m0
				+ (-2 * t3 + 3 * t2) * p1
				+ (t3 - t2) * m1;
	}
}
