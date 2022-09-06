package xstandard.math;

/**
 * https://easings.net/ + Pokémon Omega Ruby / Alpha Sapphire disassembly
 */
public class Easings {

	public static float easeInOutCosine_GF(float w) {
		if (w <= 0f) {
			return 0f;
		}
		if (w >= 1f) {
			return 1f;
		}
		return 0.5f - (float) Math.cos(Math.PI * w) * 0.5f;
	}
	
	private static float easeInOutCosineBack_GFDisasm(float w) {
		float s0 = w;
		float s2 = 180f;
		float s1 = 20f;
		float s3 = MathEx.DEGREES_TO_RADIANS;
		s1 = s1 + s0 * s2;
		s0 = MathEx.toRadiansf(20f);
		float s18 = s1 * s3;
		float s19 = (float) Math.cos(s0) * 0.5f;
		s19 = s19 - (float) Math.cos(s18) * 0.5f;
		return s19;
	}
	
	public static float easeInOutCosineBack_GF(float w) {
		double rad20 = Math.toRadians(20.0);
		float cos20 = (float) Math.cos(rad20);
		float cosOffsWAngle = (float) Math.cos(rad20 + w * Math.PI);
		return cos20 * 0.5f - cosOffsWAngle * 0.5f;
	}

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
