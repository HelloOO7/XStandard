package ctrmap.stdlib.math;

import ctrmap.stdlib.math.vec.Matrix4;
import ctrmap.stdlib.math.vec.Vec3f;
import org.joml.Matrix3f;

/**
 * Linear algebra from the Blender Library, available at:
 * https://github.com/blender/blender/tree/master/source/blender/blenlib/intern
 */
public class BlenLibMath {
	
	private static Matrix3f temp_mat3 = new Matrix3f();

	public synchronized static Vec3f getRotation(Matrix4 matrix, Vec3f dest) {
		float[] eul = new float[3];
		float[] mfull = new float[9];
		float[][] m = new float[3][3];
		
		matrix.normalize3x3(temp_mat3);
		temp_mat3.get(mfull);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				m[i][j] = mfull[i + j * 3];
			}
		}
		mat3_normalized_to_eulO(eul, m);
		dest.set(eul);
		return dest;
	}

	public static void mat3_normalized_to_eulO(float[] eul, float[][] m) {
		float[] eul1 = new float[3];
		float[] eul2 = new float[3];
		float d1, d2;

		mat3_normalized_to_eulo2(m, eul1, eul2);

		d1 = Math.abs(eul1[0]) + Math.abs(eul1[1]) + Math.abs(eul1[2]);
		d2 = Math.abs(eul2[0]) + Math.abs(eul2[1]) + Math.abs(eul2[2]);

		/* return best, which is just the one with lowest values it in */
		if (d1 > d2) {
			copy_v3_v3(eul, eul2);
		} else {
			copy_v3_v3(eul, eul1);
		}
	}

	public static void copy_v3_v3(float[] dst, float[] src) {
		System.arraycopy(src, 0, dst, 0, 3);
	}

	public static void mat3_normalized_to_eulo2(float[][] mat,
		float[] eul1,
		float[] eul2) {
		short i = 2, j = 1, k = 0;
		float cy;

		cy = (float) Math.hypot(mat[i][i], mat[i][j]);

		if (cy > 16.0f * Float.MIN_VALUE) {
			eul1[i] = (float) FAtan.atan2(mat[j][k], mat[k][k]);
			eul1[j] = (float) FAtan.atan2(-mat[i][k], cy);
			eul1[k] = (float) FAtan.atan2(mat[i][j], mat[i][i]);

			eul2[i] = (float) FAtan.atan2(-mat[j][k], -mat[k][k]);
			eul2[j] = (float) FAtan.atan2(-mat[i][k], -cy);
			eul2[k] = (float) FAtan.atan2(-mat[i][j], -mat[i][i]);
		} else {
			eul1[i] = (float) FAtan.atan2(-mat[k][j], mat[j][j]);
			eul1[j] = (float) FAtan.atan2(-mat[i][k], cy);
			eul1[k] = 0;

			copy_v3_v3(eul2, eul1);
		}
	}
}
