
package ctrmap.stdlib.math;

import ctrmap.stdlib.math.vec.Vec3f;

public class MathEx {

	public static float getAngleByCosineLaw(float adj1, float adj2, float opp){
		return (float)Math.acos(cosineLaw(adj1, adj2, opp));
	}
	
	public static float cosineLaw(float adj1, float adj2, float opp){
		return (adj1 * adj1 + adj2 * adj2 - opp * opp) / (2 * adj1 * adj2);
	}
	
	public static float getAngleBySineLaw(float side1, float angle1, float side2){
		return (float)Math.asin(sineLaw(side1, angle1, side2));
	}
	
	public static float sineLaw(float side1, float angle1, float side2){
		return (float)(side2 / (side1 / Math.sin(angle1)));
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

	public static boolean impreciseFloatEquals(float f0, float f1) {
		return impreciseFloatEquals(f0, f1, 0.1F); //default precision
	}

	public static boolean impreciseFloatEquals(float f0, float f1, float precision) {
		return Math.abs(f0 - f1) < precision;
	}
	
	public static int average(int... sources){
		int sum = 0;
		for (int i = 0; i < sources.length; i++){
			sum += sources[i];
		}
		return sum / sources.length;
	}

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

}
