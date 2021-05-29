package ctrmap.stdlib.math;

import ctrmap.stdlib.math.vec.Vec3f;

public class QuadInterpolation {
	
	public static Vec3f calc(Vec3f p1, Vec3f cp, Vec3f p2, float t) {
		return QuadInterpolation.calc(p1, cp, p2, t, new Vec3f());
	}
	
	public static Vec3f calc(Vec3f p1, Vec3f cp, Vec3f p2, float t, Vec3f dest) {
		dest.x = calc(p1.x, cp.x, p2.x, t);
		dest.y = calc(p1.y, cp.y, p2.y, t);
		dest.z = calc(p1.z, cp.z, p2.z, t);
		return dest;
	}
	
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
