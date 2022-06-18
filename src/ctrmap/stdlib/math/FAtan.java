package ctrmap.stdlib.math;

/**
 * Blazingly fast LUT atan2 implementation by Icecore.
 *
 * https://jvm-gaming.org/t/extremely-fast-atan2/55147/24
 */
public class FAtan {

	static final private int LUT_RESOLUTION = 4000;
	static final private int LUT_SIZE = LUT_RESOLUTION + 1;

	static final private float ATAN2[] = new float[LUT_SIZE];
	static final private float ATAN2_PM[] = new float[LUT_SIZE];
	static final private float ATAN2_MP[] = new float[LUT_SIZE];
	static final private float ATAN2_MM[] = new float[LUT_SIZE];

	static final private float ATAN2_R[] = new float[LUT_SIZE];
	static final private float ATAN2_RPM[] = new float[LUT_SIZE];
	static final private float ATAN2_RMP[] = new float[LUT_SIZE];
	static final private float ATAN2_RMM[] = new float[LUT_SIZE];

	static {
		double invLUTRes = 1.0 / LUT_RESOLUTION;
		
		for (int i = 0; i <= LUT_RESOLUTION; i++) {
			double d = i * invLUTRes;
			double x = 1;
			double y = x * d;
			float v = (float) Math.atan2(y, x);
			ATAN2[i] = v;
			ATAN2_PM[i] = MathEx.PI - v;
			ATAN2_MP[i] = -v;
			ATAN2_MM[i] = MathEx.PI_NEG + v;

			ATAN2_R[i] = MathEx.HALF_PI - v;
			ATAN2_RPM[i] = MathEx.HALF_PI + v;
			ATAN2_RMP[i] = MathEx.HALF_PI_NEG + v;
			ATAN2_RMM[i] = MathEx.HALF_PI_NEG - v;
		}
	}

	public static final float atan2(float y, float x) {
		if (y < 0) {
			if (x < 0) {
				//(y < x) because == (-y > -x)
				if (y < x) {
					return ATAN2_RMM[(int) (x / y * LUT_RESOLUTION)];
				} else {
					return ATAN2_MM[(int) (y / x * LUT_RESOLUTION)];
				}
			} else {
				y = -y;
				if (y > x) {
					return ATAN2_RMP[(int) (x / y * LUT_RESOLUTION)];
				} else {
					return ATAN2_MP[(int) (y / x * LUT_RESOLUTION)];
				}
			}
		} else {
			if (x < 0) {
				x = -x;
				if (y > x) {
					return ATAN2_RPM[(int) (x / y * LUT_RESOLUTION)];
				} else {
					return ATAN2_PM[(int) (y / x * LUT_RESOLUTION)];
				}
			} else {
				if (y > x) {
					return ATAN2_R[(int) (x / y * LUT_RESOLUTION)];
				} else {
					return ATAN2[(int) (y / x * LUT_RESOLUTION)];
				}
			}
		}
	}
}
