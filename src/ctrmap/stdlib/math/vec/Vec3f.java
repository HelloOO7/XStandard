package ctrmap.stdlib.math.vec;

import ctrmap.stdlib.math.MathEx;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Vec3f extends Vector3f implements AbstractVector, Cloneable {

	public static final Vec3f ONE() {
		return new Vec3f(1f, 1f, 1f);
	}
	
	public static final Vec3f ZERO() {
		return new Vec3f(0f, 0f, 0f);
	}

	public Vec3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3f(Vec3f left, Vec3f right, float interpolationWeight) {
		x = left.x + (right.x - left.x) * interpolationWeight;
		y = left.y + (right.y - left.y) * interpolationWeight;
		z = left.z + (right.z - left.z) * interpolationWeight;
	}

	public Vec3f(float scalar) {
		x = scalar;
		y = scalar;
		z = scalar;
	}

	public Vec3f(Vector3f src) {
		this.x = src.x;
		this.y = src.y;
		this.z = src.z;
	}

	public Vec3f(float[] coordinates) {
		x = coordinates[0];
		y = coordinates[1];
		z = coordinates[2];
	}

	public Vec3f(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
	}

	public Vec3f() {
		x = 0f;
		y = 0f;
		z = 0f;
	}
	
	public void set(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
	}
	
	public float[] get(float[] arr){
		arr[0] = x;
		arr[1] = y;
		arr[2] = z;
		return arr;
	}

	public static Vec3f parseVec3f(String src) {
		src = src.trim();
		if (src.startsWith("(") && src.endsWith(")")) {
			src = src.substring(1, src.length() - 1);
		}
		String[] cmds = src.split(",");
		if (cmds.length != 3) {
			throw new NumberFormatException("Source is not a 3-component vector - " + Arrays.toString(cmds));
		}
		Vec3f v = new Vec3f();
		v.x = Float.parseFloat(cmds[0]);
		v.y = Float.parseFloat(cmds[1]);
		v.z = Float.parseFloat(cmds[2]);
		return v;
	}

	public float getHighestAbsComponent() {
		return Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
	}

	public float getHighestAbsComponentNonAbs() {
		float ax = Math.abs(x);
		float ay = Math.abs(y);
		float az = Math.abs(z);

		if (ax > ay) {
			if (ax > az) {
				return x;
			} else {
				return z;
			}
		} else {
			if (ay > az) {
				return y;
			} else {
				return z;
			}
		}
	}

	public float getComponentAverage() {
		return (x + y + z) / 3f;
	}

	public Vec3f invert() {
		return mul(-1);
	}

	public Vec3f getInverse() {
		return new Vec3f(this).invert();
	}

	public Vec3f add(Vec3f v) {
		super.add(v);
		return this;
	}

	public Vec3f sub(Vec3f v){
		super.sub(v);
		return this;
	}
	
	@Override
	public Vec3f mul(float mul) {
		super.mul(mul);
		return this;
	}
	
	@Override
	public Vec3f mulAdd(float s, Vector3fc v){
		super.mulAdd(s, v);
		return this;
	}

	public Vec3f mul(Vec3f mul) {
		super.mul(mul);
		return this;
	}

	public void translate(Vec3f v) {
		super.add(v);
	}

	public void setScalar(float value) {
		x = value;
		y = value;
		z = value;
	}

	@Override
	public Vec2f toVec2() {
		return new Vec2f(x, y);
	}

	@Override
	public Vec3f toVec3() {
		return this;
	}

	@Override
	public Vec4f toVec4() {
		return new Vec4f(x, y, z, 1f);
	}

	public void write(DataOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
	}

	public float dist(Vec3f second) {
		return super.distance(second);
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Vec3f) {
			Vec3f v = (Vec3f) o;
			return v.x == x && v.y == y && v.z == z;
		}
		return false;
	}
	
	@Override
	public Vec3f clone(){
		return new Vec3f(this);
	}

	public boolean equalsImprecise(Object o, float precision) {
		if (o != null && o instanceof Vec3f) {
			Vec3f v = (Vec3f) o;
			return MathEx.impreciseFloatEquals(v.x, x, precision) && MathEx.impreciseFloatEquals(v.y, y, precision) && MathEx.impreciseFloatEquals(v.z, z, precision);
		}
		return false;
	}

	public float[] toFloatUniform() {
		return new float[]{x, y, z};
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public String toStringRotationDegrees() {
		return "(" + Math.toDegrees(x) + ", " + Math.toDegrees(y) + ", " + Math.toDegrees(z) + ")";
	}
}
