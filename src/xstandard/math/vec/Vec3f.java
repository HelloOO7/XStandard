package xstandard.math.vec;

import xstandard.math.MathEx;
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

	public static Vec3f hitY(Vec3f pos, Vec3f dir, float y, Vec3f dest) {
		float t = (y - pos.y) / dir.y;
		dest.set(dir);
		dest.mulAdd(t, pos);
		return dest;
	}

	public void set(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
	}

	public float[] get(float[] arr) {
		return get(arr, 0);
	}

	public float[] get(float[] arr, int off) {
		arr[off + 0] = x;
		arr[off + 1] = y;
		arr[off + 2] = z;
		return arr;
	}
	
	public boolean isZero() {
		return x == 0f && y == 0f && z == 0f;
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

	public Vec3f getDirFromEulersDegYXZ(Vec3f dest) {
		float radX = MathEx.toRadiansf(x);
		float radY = MathEx.toRadiansf(y);
		float cx = (float) Math.cos(radX);

		float dirZ = -(float) Math.cos(radY) * cx;
		float dirX = (float) Math.sin(-radY) * cx;
		float dirY = (float) Math.sin(radX);
		dest.set(dirX, dirY, dirZ);
		dest.normalize();
		return dest;
	}

	public Vec3f getDirFromEulersYXZ(Vec3f dest) {
		float z = -(float) Math.cos(-this.y) * (float) Math.cos(this.x);
		float x = (float) Math.sin(-this.y) * (float) Math.cos(this.x);
		float y = (float) Math.sin(this.x);
		dest.set(x, y, z);
		dest.normalize();
		return dest;
	}

	public Vec3f getDirFromEulersDegZYX(Vec3f dest) {
		Matrix4 m = new Matrix4();
		m.rotateZYXDeg(z, y, x);
		m.translate(0f, 0f, -1f);
		m.getTranslation(dest);
		return dest;
	}

	public Vec3f getUpVecFromEulersDegZYX(Vec3f dest) {
		Matrix4 m = new Matrix4();
		m.rotateZYXDeg(z, y, x);
		m.translate(0f, 1f, 0f);
		m.getTranslation(dest);
		return dest;
	}

	public Vec3f getCross(Vec3f other) {
		Vec3f c = new Vec3f();
		cross(other, c);
		return c;
	}

	/*
	https://stackoverflow.com/questions/29188686/finding-the-intersect-location-of-two-rays
	 */
	public static void findShortestDistance(Vec3f ray1Position, Vec3f ray1Direction, Vec3f ray2Position, Vec3f ray2Direction, Vec3f destInterPoint, Vec3f destDistance) {
		if (ray1Position.equals(ray2Position)) // same position - that is the point of intersection
		{
			destInterPoint.set(ray1Position);
			destDistance.zero();
			return;
		}

		Vec3f d3 = ray1Direction.getCross(ray2Direction);

		if (!d3.equals(Vec3f.ZERO())) // lines askew (non - parallel)
		{
			float[] matrix = new float[12];

			matrix[0] = ray1Direction.x;
			matrix[1] = -ray2Direction.x;
			matrix[2] = d3.x;
			matrix[3] = ray2Position.x - ray1Position.x;

			matrix[4] = ray1Direction.y;
			matrix[5] = -ray2Direction.y;
			matrix[6] = d3.y;
			matrix[7] = ray2Position.y - ray1Position.y;

			matrix[8] = ray1Direction.z;
			matrix[9] = -ray2Direction.z;
			matrix[10] = d3.z;
			matrix[11] = ray2Position.z - ray1Position.z;

			float[] result = solveGaussian(matrix, 3, 4);

			float a = result[3];
			float b = result[7];
			float c = result[11];

			if (a >= 0 && b >= 0) {
				destInterPoint.set(ray1Direction);
				destInterPoint.mulAdd(a, ray1Position);
				destDistance.set(d3);
				destDistance.mul(c);

				return;
			}
		}

		ray1Direction = ray1Direction.clone();
		ray2Direction = ray2Direction.clone();

		ray1Direction.normalize(); //needed for dot product - it works with unit vectors
		ray2Direction.normalize();

		Vec3f dP = ray2Position.clone().sub(ray1Position);

		float a2 = ray1Direction.dot(dP);
		float b2 = ray2Direction.dot(dP.invert());

		if (a2 < 0 && b2 < 0) {
			destInterPoint.set(ray1Position);
			destDistance.set(dP);

			return;
		}

		Vec3f p3a = ray1Direction.mulAdd(a2, ray1Position);
		Vec3f d3a = ray2Position.clone().sub(p3a);

		Vec3f p3b = ray1Position;
		Vec3f d3b = ray2Direction.mulAdd(b2, ray2Position);

		if (b2 < 0) {
			destInterPoint.set(p3a);
			destDistance.set(d3a);
			return;
		}

		if (a2 < 0) {
			destInterPoint.set(p3b);
			destDistance.set(d3b);
			return;
		}

		if (d3a.length() <= d3b.length()) {
			destInterPoint.set(p3a);
			destDistance.set(d3a);
			return;
		}

		destInterPoint.set(p3b);
		destDistance.set(d3b);
	}

	//Solves a set of linear equations using Gaussian elimination
	//https://stackoverflow.com/questions/29188686/finding-the-intersect-location-of-two-rays
	private static float[] solveGaussian(float[] matrix, int rows, int cols) {
		for (int i = 0; i < cols - 1; i++) {
			for (int j = i; j < rows; j++) {
				if (matrix[i + j * cols] != 0) {
					if (i != j) {
						for (int k = i; k < cols; k++) {
							float temp = matrix[k + j * cols];
							matrix[k + j * cols] = matrix[k + i * cols];
							matrix[k + i * cols] = temp;
						}
					}

					j = i;

					for (int v = 0; v < rows; v++) {
						if (v == j) {
							continue;
						} else {
							float factor = matrix[i + v * cols] / matrix[i + j * cols];
							matrix[i + v * cols] = 0;

							for (int u = i + 1; u < cols; u++) {
								matrix[u + v * cols] -= factor * matrix[u + j * cols];
								matrix[u + j * cols] /= matrix[i + j * cols];
							}
							matrix[i + j * cols] = 1;
						}
					}
					break;
				}
			}
		}

		return matrix;
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

	public Vec3f recip() {
		x = 1f / x;
		y = 1f / y;
		z = 1f / z;
		return this;
	}

	public Vec3f getInverse() {
		return new Vec3f(this).invert();
	}

	public Vec3f add(Vec3f v) {
		super.add(v);
		return this;
	}

	public Vec3f sub(Vec3f v) {
		super.sub(v);
		return this;
	}

	@Override
	public Vec3f mul(float mul) {
		super.mul(mul);
		return this;
	}

	@Override
	public Vec3f mulAdd(float s, Vector3fc v) {
		super.mulAdd(s, v);
		return this;
	}

	@Override
	public Vec3f rotateY(float angle) {
		super.rotateY(angle);
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
	public Vec3f clone() {
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
