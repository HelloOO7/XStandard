package ctrmap.stdlib.math.vec;

import ctrmap.stdlib.math.FAtan;
import ctrmap.stdlib.math.MathEx;
import ctrmap.stdlib.math.MatrixUtil;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

public class Quaternion extends Quaternionf {

	public Quaternion(Quaternionf q) {
		super(q);
	}

	public Quaternion() {
		super();
	}

	public Quaternion(Vec3f euler) {
		this(euler.x, euler.y, euler.z);
	}

	public Quaternion(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
		w = in.readFloat();
	}

	public Quaternion(float x, float y, float z) {
		super();
		rotateLocalX(x);
		rotateLocalY(y);
		rotateLocalZ(z);
	}

	public Quaternion(float x, float y, float z, float w) {
		super(x, y, z, w);
	}

	@Override
	public Quaternion clone() {
		return new Quaternion(this);
	}

	public Quaternion mul(Quaternion q) {
		super.mul(q);
		return this;
	}

	public void write(DataOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
		out.writeFloat(w);
	}

	public Quaternion rotateXYZ(Vector3fc vec) {
		super.rotateXYZ(vec.x(), vec.y(), vec.z());
		return this;
	}

	public Quaternion rotateZYX(Vector3fc vec) {
		super.rotateZYX(vec.z(), vec.y(), vec.x());
		return this;
	}
	
	public Quaternion rotateZYXDeg(float z, float y, float x) {
		super.rotateZYX(MathEx.toRadiansf(z), MathEx.toRadiansf(y), MathEx.toRadiansf(x));
		return this;
	}

	public Vec3f toEulerAnglesAllocFree(Vec3f dest) {
		// roll (x-axis rotation)
		float sinr_cosp = 2f * (w * x + y * z);
		float cosr_cosp = 1f - 2f * (x * x + y * y);
		dest.x = FAtan.atan2(sinr_cosp, cosr_cosp);

		// pitch (y-axis rotation)
		double sinp = 2.0 * (w * y - z * x);
		if (Math.abs(sinp) >= 1) {
			dest.y = (float) Math.copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
		} else {
			dest.y = (float) Math.asin(sinp);
		}

		// yaw (z-axis rotation)
		float siny_cosp = 2f * (w * z + x * y);
		float cosy_cosp = 1f - 2f * (y * y + z * z);
		dest.z = FAtan.atan2(siny_cosp, cosy_cosp);

		return dest;
	}

	public Vec3f getEulerRotation(Vec3f dest) {
		return MatrixUtil.getQuatEuler(this, dest);
	}

	public Vec3f getEulerRotation() {
		return MatrixUtil.getQuatEuler(this);
		/*Vec3f v = new Vec3f();
		getEulerAnglesXYZ(v);
		return v;*/
	}

	public Matrix4 getRotationMatrix() {
		Matrix4 m = new Matrix4();
		get(m);
		return m;
	}
}
