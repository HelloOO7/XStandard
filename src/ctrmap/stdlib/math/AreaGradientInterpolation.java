package ctrmap.stdlib.math;

/*
Courtesy of Pokémon Omega Ruby / Alpha Sapphire disassembly.
*/
public class AreaGradientInterpolation {

	
	/*
	xxxxxxxxxxxxxxxxx
	xxxxxx....xxxxxxx
	xx...oooooo...xxx
	x..oOOOOOOOOo..xx
	x.ooOOOOOOOOoo..x
	x..oOOOOOOOOo..xx
	xx...oooooo...xxx
	xxxxxx....xxxxxxx
	xxxxxxxxxxxxxxxxx
	
	Linear falloff from a fixed rectangular core where the weight is 0.
	*/
	public static float calcWeightCircleFalloff(float x, float y, float areaX, float areaZ, float falloffStartW, float falloffStartH, float radius) {
		float halfW = falloffStartW * 0.5f;
		float halfH = falloffStartH * 0.5f;

		float sdistx = x - (areaX + halfW);
		float sdisty = y - (areaZ + halfH);

		float distx = Math.abs(sdistx);
		float disty = Math.abs(sdisty);

		float distFromCentre = (float) Math.hypot(sdistx, sdisty);

		float interpRange = Float.MAX_VALUE;

		if (distx > halfW || disty > halfH) {
			if (distx > halfW) {
				interpRange = distFromCentre / distx * halfW;
			}
			if (disty > halfH) {
				float ytest = distFromCentre / disty * halfH;
				if (interpRange > ytest) {
					interpRange = ytest;
				}
			}
		}

		if (interpRange < radius) {
			return calcInterpWVSE(distFromCentre, interpRange, radius);
		}

		return 0f;
	}

	/*
	xxxxxxxxxxxxxx
	x.oooooooooo.x
	x.ooOOOOOOoo.x
	x.ooO••••Ooo.x
	x.ooO••••Ooo.x
	x.ooOOOOOOoo.x
	x.oooooooooo.x
	
	//Linear square falloff from a fixed rectangular core where the weight is 0.
	*/
	public static float calcWeightSquareFalloff(float x, float y, float startX, float startY, float endX, float endY, float falloffStart) {
		float areaWHalf = ((endX + 1f) - startX) * 0.5f;
		float areaHHalf = ((endY + 1f) - startY) * 0.5f;

		float distFromOriginX = x - (startX + areaWHalf);
		float distFromOriginZ = y - (startY + areaHHalf);

		float distFromOrigin = (float) Math.hypot(distFromOriginX, distFromOriginZ);

		float interpRange = Float.MAX_VALUE;

		float distFromOriginAbsX = Math.abs(distFromOriginX);
		float distFromOriginAbsZ = Math.abs(distFromOriginZ);

		if (distFromOriginAbsX > 0.002f) {
			interpRange = (areaWHalf * distFromOrigin) / distFromOriginAbsX;
		}

		if (distFromOriginAbsZ > 0.002f) {
			float ztest = (areaHHalf * distFromOrigin) / distFromOriginAbsZ;
			if (interpRange > ztest) {
				interpRange = ztest;
			}
		}

		if (interpRange != Float.MAX_VALUE) {
			return calcInterpWVSE(distFromOrigin, interpRange * falloffStart, interpRange);
		}

		return 0f;
	}

	private static float calcInterpWVSE(float v, float s, float e) {
		if (e - s < 0.0001f) {
			return 0f;
		}
		if (v < s) {
			return 0f;
		}
		if (v > e) {
			return 1f;
		}
		return (v - s) / (e - s);
	}
}
