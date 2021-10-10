package ctrmap.stdlib.math;

import ctrmap.stdlib.math.vec.Matrix4;
import ctrmap.stdlib.math.vec.Quaternion;
import ctrmap.stdlib.math.vec.Vec3f;

public class MatrixUtil {

	public static Vec3f transformVector(Vec3f v, Matrix4 m) {
		v = new Vec3f(v);
		v.mulPosition(m);
		return v;
	}

	public static Matrix4 createRotation(Vec3f vec) {
		return createRotation(vec.x, vec.y, vec.z);
	}

	public static Matrix4 createRotation(float x, float y, float z) {
		Matrix4 mrx = new Matrix4();
		mrx.rotateZ(z);
		mrx.rotateY(y);
		mrx.rotateX(x);

		//return multiplyRight(multiplyRight(multiplyRight(new Matrix4(), mrx), mry), mrz);
		return mrx;
	}

	public static Matrix4 createTranslation(Vec3f vec) {
		return createTranslation(vec.x, vec.y, vec.z);
	}

	public static Matrix4 createTranslation(float x, float y, float z) {
		Matrix4 mtx = new Matrix4();
		mtx.translate(x, y, z);
		return mtx;
	}

	public static Matrix4 copyTranslation(Matrix4 src, Matrix4 tgt) {
		tgt.setTranslation(src.getTranslation());
		return tgt;
	}

	public static Matrix4 createScale(float x, float y, float z) {
		Matrix4 mtx = new Matrix4();
		mtx.scale(x, y, z);
		return mtx;
	}

	public static void decompose3x3(float[] src, float[][] dst) {
		for (int i = 0; i < 9; i++) {
			dst[i / 3][i % 3] = src[i];
		}
	}

	public static Matrix4 copyMatrix(Matrix4 input) {
		if (input == null) {
			return null;
		}
		Matrix4 ret = new Matrix4();
		copyMatrix(input, ret);
		return ret;
	}

	public static void copyMatrix(Matrix4 input, Matrix4 output) {
		output.set(input.getMatrix());
	}

	public static Matrix4 multiplyRight(Matrix4 base, Matrix4 multiplier) {
		return base.multiplyRight(multiplier);
	}
	
	public static Vec3f getQuatEuler(Quaternion q, Vec3f dest) {
		return q.getRotationMatrix().getRotationTo(dest);
		//unfortunately, q.toEuler is often broken, as are most JOGL functions anyway :( So we use the matrix method.
	}

	public static Vec3f getQuatEuler(Quaternion q) {
		return getQuatEuler(q, new Vec3f());
	}

	public static Vec3f rotationFromMatrix(Matrix4 mtx, Vec3f s) {
		return rotationFromMatrix(mtx, s, new Vec3f());
	}

	private static float[] mtx4x4 = new float[16];
	private static float[] mtx3x3flip = new float[9];
	
	private static synchronized void fill3x3(Vec3f scale) {
		float invX = 1f / scale.x;
		float invY = 1f / scale.y;
		float invZ = 1f / scale.z;
		mtx3x3flip[0] = mtx4x4[0] * invX;
		mtx3x3flip[1] = mtx4x4[4] * invY;
		mtx3x3flip[2] = mtx4x4[8] * invZ;
		mtx3x3flip[3] = mtx4x4[1] * invX;
		mtx3x3flip[4] = mtx4x4[5] * invY;
		mtx3x3flip[5] = mtx4x4[9] * invZ;
		mtx3x3flip[6] = mtx4x4[2] * invX;
		mtx3x3flip[7] = mtx4x4[6] * invY;
		mtx3x3flip[8] = mtx4x4[10] * invZ;
	}
	
	public static synchronized Vec3f rotationFromMatrix(Matrix4 mtx, Vec3f s, Vec3f r) {
		mtx.get(mtx4x4);
		fill3x3(s);
		return rotationFromMatrixImplZYX(mtx3x3flip, r);
	}
	
	public static synchronized Vec3f rotationFromMatrixYXZ(Matrix4 mtx, Vec3f s, Vec3f r) {
		mtx.get(mtx4x4);
		fill3x3(s);
		return rotationFromMatrixImplYXZ(mtx3x3flip, r);
	}
	
	/*
	https://www.geometrictools.com/Documentation/EulerAngles.pdf
	*/

	private static Vec3f rotationFromMatrixImplYXZ(float[] mtx, Vec3f r) {
		//M00 M01 M02	mtx[0] mtx[1] mtx[2]
		//M10 M11 M12	mtx[3] mtx[4] mtx[5]
		//M20 M22 M23	mtx[6] mtx[7] mtx[8]
		
		double x;
		double y;
		double z;
		
		if (mtx[5] < 1f) {
			if (mtx[5] > -1f) {
				x = Math.asin(-mtx[5]);
				y = Math.atan2(mtx[2], mtx[7]);
				z = Math.atan2(mtx[3], mtx[4]);
			}
			else {
				x = MathEx.HALF_PI;
				y = -Math.atan2(mtx[1], mtx[0]);
				z = 0.0;
			}
		}
		else {
			x = MathEx.HALF_PI_NEG;
			y = Math.atan2(-mtx[1], mtx[0]);
			z = 0.0;
		}
		
		r.x = (float)x;
		r.y = (float)y;
		r.z = (float)z;
		
		return r;
	}

	private static Vec3f rotationFromMatrixImplZYX(float[] mtx, Vec3f r) {
		//M11 M12 M13	mtx[0] mtx[1] mtx[2]
		//M21 M22 M23	mtx[3] mtx[4] mtx[5]
		//M31 M32 M33	mtx[6] mtx[7] mtx[8]
		double x;
		double y;
		double z;

		if (!(mtx[6] == 1 || mtx[6] == -1)) {
			double y1 = -Math.asin(mtx[6]);
			double y2 = Math.PI - y1;
			double x1 = Math.atan2(mtx[7] / Math.cos(y1), mtx[8] / Math.cos(y1));
			//double x2 = Math.atan2(mtx[7] / Math.cos(y2), mtx[8] / Math.cos(y2));
			double z1 = Math.atan2(mtx[3] / Math.cos(y1), mtx[0] / Math.cos(y1));
			//double z2 = Math.atan2(mtx[3] / Math.cos(y2), mtx[0] / Math.cos(y2));
			r.x = (float) x1;
			r.y = (float) y1;
			r.z = (float) z1;
		} else {
			z = 0;
			if (mtx[6] == -1) {
				y = Math.PI / 2.0;
				x = Math.atan2(mtx[1], mtx[2]);
			} else {
				y = -Math.PI / 2.0;
				x = Math.atan2(-mtx[1], -mtx[2]);
			}
			r.x = (float) x;
			r.y = (float) y;
			r.z = (float) z;
		}
		return r;
	}

	public static Matrix4 getMatrix4(float[] mtx) {
		return new Matrix4(mtx);
	}

	public static Matrix4 getMatrix4FromRowMajor(float[] mtx) {
		float[] newMtx = new float[16];
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				newMtx[y * 4 + x] = mtx[x * 4 + y];
			}
		}
		return getMatrix4(newMtx);
	}
}
