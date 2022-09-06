package xstandard.math;

import xstandard.math.vec.Vec3f;

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
	
	public static Vec3f calcDerivative(Vec3f p0, Vec3f m0, Vec3f p1, Vec3f m1, float t, Vec3f dest) {
		dest.x = HermiteInterpolation.calcDerivative(p0.x, m0.x, p1.x, m1.x, t);
		dest.y = HermiteInterpolation.calcDerivative(p0.y, m0.y, p1.y, m1.y, t);
		dest.z = HermiteInterpolation.calcDerivative(p0.z, m0.z, p1.z, m1.z, t);
		dest.normalize();
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
	
	/**
	 * Calculates the derivative (tangent) vector component of a hermite curve at a position between two values.
	 * 
	 * Code was decompiled from Pokémon X 1.0 at 0x61C90 of DllField.cro's .text segment.
	 * See calcDerivativeDisasm for unoptimized disassembly code.
	 * 
	 * @param p0 The first value.
	 * @param m0 Tangent of the first value.
	 * @param p1 The second value.
	 * @param m1 Tangent of the second value.
	 * @param t Interpolation weight.
	 * @return P0(M0) to P1(M1) derivative interpolated using Hermite interpolation with weight t.
	 */
	public static float calcDerivative(float p0, float m0, float p1, float m1, float t) {
		float diffP = p0 - p1;
						
		float result = (((m0 + m1 + 2f * diffP) * 3f) * t * t + (((-(m1 - m0 * -2f)) - 3f * diffP) * 2f) * t) + m0;
		
		return result;
	}
	
	/*
	Disassembly of Pokémon's rail derivative interpolation.
	*/
	private static float calcDerivativeDisasm(float p0, float m0, float p1, float m1, float t) {
		float s0 = t;
		float s1 = p0;
		float s2 = m0;
		float s3 = p1;
		float s4 = m1;
		
		float s7 = s2 + s4;
		s1 = s1 - s3;
		
		float s8 = -2f;
		float s6 = 2f;
		float s5 = 3f;
		
		s4 = -(s4 - s2 * s8);
		
		s7 += s1 * s6;
		s4 -= s1 * s5;
		s1 = s7 * s5;
		s3 = s4 * s6;
		s1 = s1 * s0;
		s1 = s1 * s0;
		s1 += s3 * s0;
		s0 = s1 + s2;
		
		return s0;
	}
}
