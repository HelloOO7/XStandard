package xstandard.math.vec;

import xstandard.math.BlenLibMath;
import xstandard.math.MathEx;
import xstandard.math.MatrixUtil;
import java.io.DataInput;
import java.io.IOException;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Vector3fc;

public class Matrix4 extends Matrix4f {

	public Matrix4(float[] flt) {
		this();
		set(flt);
	}
	
	public static Matrix4 createRowMajor(float[] mtx) {
		return new Matrix4(mtx).setRowMajor(mtx);
	}

	public Matrix4() {
		super();
	}

	public Matrix4(Matrix4f mtx) {
		super(mtx);
	}

	public Matrix4(Matrix4x3f mtx) {
		super(mtx);
	}

	public Matrix4(Matrix3f mtx) {
		super(mtx);
	}

	public Matrix4(DataInput in) throws IOException {
		float[] buf = new float[16];
		for (int i = 0; i < 16; i++) {
			buf[i] = in.readFloat();
		}
		set(buf);
	}

	public static Matrix4 createRotation(Vec3f vec) {
		return createRotation(vec.x, vec.y, vec.z);
	}

	public static Matrix4 createRotation(float x, float y, float z) {
		Matrix4 mrx = new Matrix4();
		mrx.rotationZYX(z, y, x);
		return mrx;
	}

	public static Matrix4 createTranslation(Vec3f t) {
		return new Matrix4().translation(t);
	}

	public static Matrix4 createTranslation(float x, float y, float z) {
		return new Matrix4().translation(x, y, z);
	}

	public static Matrix4 createScale(float x, float y, float z) {
		Matrix4 mtx = new Matrix4();
		mtx.scaling(x, y, z);
		return mtx;
	}

	public Vec4f[] frustumPlanes() {
		/*
		https://stackoverflow.com/questions/12836967/extracting-view-frustum-planes-gribb-hartmann-method
		 */
		Vec4f[] p_planes = new Vec4f[6];
		//Left clipping plane
		p_planes[0].x = m30() + m00();
		p_planes[0].y = m31() + m01();
		p_planes[0].z = m32() + m02();
		p_planes[0].w = m33() + m03();
		// Right clipping plane
		p_planes[1].x = m30() - m00();
		p_planes[1].y = m31() - m01();
		p_planes[1].z = m32() - m02();
		p_planes[1].w = m33() - m03();
		// Top clipping plane
		p_planes[2].x = m30() - m10();
		p_planes[2].y = m31() - m11();
		p_planes[2].z = m32() - m12();
		p_planes[2].w = m33() - m13();
		// Bottom clipping plane
		p_planes[3].x = m30() + m10();
		p_planes[3].y = m31() + m11();
		p_planes[3].z = m32() + m12();
		p_planes[3].w = m33() + m13();
		// Near clipping plane
		p_planes[4].x = m30() + m20();
		p_planes[4].y = m31() + m21();
		p_planes[4].z = m32() + m22();
		p_planes[4].w = m33() + m23();
		// Far clipping plane
		p_planes[5].x = m30() - m20();
		p_planes[5].y = m31() - m21();
		p_planes[5].z = m32() - m22();
		p_planes[5].w = m33() - m23();
		return p_planes;
	}

	@Override
	public Matrix4 clone() {
		return new Matrix4(this);
	}

	@Override
	public Matrix4 invert() {
		super.invert();
		return this;
	}

	@Override
	public Matrix4 translation(float x, float y, float z) {
		super.translation(x, y, z);
		return this;
	}

	@Override
	public Matrix4 translation(Vector3fc vec) {
		super.translation(vec);
		return this;
	}

	public float[] getRowMajor(float[] dest, int offset) {
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				dest[offset + y * 4 + x] = this.getRowColumn(y, x);
			}
		}
		return dest;
	}

	public void invScale(Vec3f vec) {
		scale(1f / vec.x, 1f / vec.y, 1f / vec.z);
	}

	public Matrix4 getInverseMatrix() {
		return clone().invert();
	}

	public float[] getMatrix() {
		return super.get(new float[16]);
	}

	public Matrix4 getRotationMtx() {
		Matrix4 mtx = new Matrix4();
		mtx.set3x3(this);
		mtx.scale(getScale().recip());
		return mtx;
	}

	public Matrix4 clearRotation() {
		setRotationZYX(0, 0, 0);
		return this;
	}

	public Matrix4 rotateZYXDeg(float z, float y, float x) {
		rotateZYX(MathEx.toRadiansf(z), MathEx.toRadiansf(y), MathEx.toRadiansf(x));
		return this;
	}

	public void clearRotationX() {
		float siny = -m02();
		float yAngle = (float) Math.asin(-siny);
		float cosy = (float) Math.cos(yAngle);
		float cosz = m00() / cosy;
		float sinz = m01() / cosy;

		float cosx = 1f; //cos 0
		float sinx = 0f;
		m10(-sinz);
		m11(cosz);
		m12(0);
		m20(cosz * siny);
		m21(sinz * siny);
		m22(cosy);
	}

	public void clearRotationZ() {
		float siny = -m02();
		float yAngle = (float) Math.asin(-siny);
		float cosy = (float) Math.cos(yAngle);
		float cosx = m22() / cosy;
		float sinx = m12() / cosy;

		float cosz = 1f; //cos 0
		float sinz = 0f;
		m00(cosy);
		m01(0);
		m10(siny * sinx);
		m11(cosx);
		m20(cosx * siny);
		m21(-sinx);
	}

	public Matrix4 clearTranslation() {
		setTranslation(0, 0, 0);
		return this;
	}

	public Matrix4 setRowMajor(float[] array) {
		float[] newMtx = new float[16];
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				newMtx[y * 4 + x] = array[x * 4 + y];
			}
		}
		set(newMtx);
		return this;
	}
	
	public Matrix4 rotate(Vec3f vec) {
		rotateZYX(vec);
		return this;
	}

	public Vec3f getTranslation() {
		return new Vec3f(super.getTranslation(new Vec3f()));
	}

	public Vec3f getScale() {
		return new Vec3f(super.getScale(new Vec3f()));
	}

	public Vec3f getRotation() {
		return getRotationTo(new Vec3f());
	}

	public Vec3f getRotationTo(Vec3f vec) {
		return getRotationToBLI(vec);
	}

	public Vec3f getRotationToBLI(Vec3f vec) {
		return BlenLibMath.getRotation(this, vec);
	}

	public Vec3f getRotationToAlt(Vec3f vec) {
		return MatrixUtil.rotationFromMatrix(this, getScale(), vec);
	}

	public Vec3f getRotationToYXZ(Vec3f vec) {
		return MatrixUtil.rotationFromMatrixYXZ(this, getScale(), vec);
	}

	public Vec3f getRotation(Vec3f scale) {
		return getRotation();
	}

	public Matrix4 multiplyRight(Matrix4 multiplier) {
		super.mulLocal(multiplier);
		return this;
	}
}
