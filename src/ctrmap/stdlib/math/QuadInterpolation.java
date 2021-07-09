package ctrmap.stdlib.math;

import ctrmap.stdlib.math.vec.Vec3f;

/**
 * Calculator for interpolation around a quadratic curve.
 */
public class QuadInterpolation {

	/**
	 * Calculates the interpolated product of vectors of a curve.
	 *
	 * @param p1 The start point of the curve.
	 * @param cp The control point.
	 * @param p2 The end point of the curve.
	 * @param t Interpolation weight.
	 * @return Quadratic interpolation of P1 through CP to P2.
	 */
	public static Vec3f calc(Vec3f p1, Vec3f cp, Vec3f p2, float t) {
		return QuadInterpolation.calc(p1, cp, p2, t, new Vec3f());
	}

	/**
	 * Calculates the interpolated product of vectors of a curve into a
	 * pre-allocated vector object.
	 *
	 * @param p1 The start point of the curve.
	 * @param cp The control point.
	 * @param p2 The end point of the curve.
	 * @param t Interpolation weight.
	 * @param dest Vector to store the result into.
	 * @return Quadratic interpolation of P1 through CP to P2.
	 */
	public static Vec3f calc(Vec3f p1, Vec3f cp, Vec3f p2, float t, Vec3f dest) {
		dest.x = calc(p1.x, cp.x, p2.x, t);
		dest.y = calc(p1.y, cp.y, p2.y, t);
		dest.z = calc(p1.z, cp.z, p2.z, t);
		return dest;
	}

	/**
	 * Interpolates two values with a control value around a quadratic curve.
	 * The result is calculated as:
	 * 
	 * R = B(2,0,t) * P1 + B(2,1,t) * CP + B(2,2,t) * P2
	 * 
	 * Where B is the Bernstein polynomial function and t is the interpolation weight.
	 * 
	 * @param p1 The starting value of the curve.
	 * @param cp The control value.
	 * @param p2 The ending value of the curve.
	 * @param t Interpolation weight.
	 * @return Quadratic interpolation of P1 through CP to P2 at t.
	 */
	public static float calc(float p1, float cp, float p2, float t) {
		return bernstein20(t) * p1 + bernstein21(t) * cp + bernstein22(t) * p2;
	}

	public static float bernstein20(float t) {
		return (1 - t) * (1 - t);
	}

	public static float bernstein21(float t) {
		return 2 * t * (1 - t);
	}

	public static float bernstein22(float t) {
		return t * t;
	}
}
