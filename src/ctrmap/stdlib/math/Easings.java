package ctrmap.stdlib.math;

/**
 * https://easings.net/
 */
public class Easings {

	public static float easeInSine(float w) {
		return (float) (1.0 - Math.cos((w * Math.PI) * 0.5));
	}

	public static float easeOutSine(float w) {
		return (float) Math.sin(w * Math.PI * 0.5);
	}
	
	public static float easeInOutSine(float w) {
		return (float)(-(Math.cos(w * Math.PI) - 1.0) * 0.5);
	}
	
	public static float easeInQuad(float w) {
		return w * w;
	}
	
	public static float easeOutQuad(float w) {
		return 1 - (1 - w) * (1 - w);
	}
	
	public static float easeInOutQuad(float w) {
		if (w < 0.5f) {
			return 2 * w * w;
		}
		else {
			float temp = -2 * w + 2;
			return 1f - temp * temp * 0.5f;
		}
	}
}
