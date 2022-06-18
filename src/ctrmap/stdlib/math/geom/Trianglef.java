package ctrmap.stdlib.math.geom;

import ctrmap.stdlib.math.AABB6f;
import ctrmap.stdlib.math.vec.Vec2f;
import ctrmap.stdlib.math.vec.Vec3f;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.Collection;

public class Trianglef {

	public float[] x = new float[3];
	public float[] y = new float[3];
	public float[] z = new float[3];

	public Trianglef() {

	}

	public Trianglef(float[] x, float[] y, float[] z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setPoint(int vertex, Vec3f pos) {
		x[vertex] = pos.x;
		y[vertex] = pos.y;
		z[vertex] = pos.z;
	}

	public void setX(int vertex, float value) {
		x[vertex] = value;
	}

	public void setY(int vertex, float value) {
		y[vertex] = value;
	}

	public void setZ(int vertex, float value) {
		z[vertex] = value;
	}

	public float getX(int vertex) {
		return x[vertex];
	}

	public float getY(int vertex) {
		return y[vertex];
	}

	public float getZ(int vertex) {
		return z[vertex];
	}

	public boolean containsXZ(Vec3f point) {
		return containsXZ(point.x, point.z);
	}

	public boolean containsXZ(float sx, float sz) {
		/*
		https://stackoverflow.com/questions/2049582/how-to-determine-if-a-point-is-in-a-2d-triangle
		I changed some > to >= to allow the point being on the edge of the triangle
		 */
		float as_x = sx - x[0];
		float as_y = sz - z[0];

		boolean s_ab = (x[1] - x[0]) * as_y - (z[1] - z[0]) * as_x > 0;

		if ((x[2] - x[0]) * as_y - (z[2] - z[0]) * as_x >= 0 == s_ab) {
			return false;
		}

		if ((x[2] - x[1]) * (sz - z[1]) - (z[2] - z[1]) * (sx - x[1]) > 0 != s_ab) {
			return false;
		}

		return true;
	}
	
	/*public static void main(String[] args){
		Trianglef tri = new Trianglef(new float[]{-306, -360, -72}, new float[]{-18, -18, -18}, new float[]{144, 360, 306});
		System.out.println(tri.containsXZ(-189, 225));
	}*/

	public Vec3f vector(int index) {
		return new Vec3f(x[index], y[index], z[index]);
	}

	public float getYAtXZ(Vec3f point) {
		return Trianglef.this.getYAtXZ(point.x, point.z);
	}

	public float getYAtXZ(float sx, float sz) {
		//top1, bot1 and top2 are the two sides of some weird fraction that I found on the internet that calculates this
		float dx10 = (x[1] - x[0]);
		float dx20 = (x[2] - x[0]);
		float dy10 = (y[1] - y[0]);
		float dy20 = (y[2] - y[0]);
		float dz10 = (z[1] - z[0]);
		float dz20 = (z[2] - z[0]);

		float top1 = dx10 * dy20 - dx20 * dy10;
		float bot1 = dx10 * dz20 - dx20 * dz10;
		float top2 = dz10 * dy20 - dz20 * dy10;

		return y[0] + (top1 / bot1) * (sz - z[0]) - (top2 / bot1) * (sx - x[0]);
	}

	public Vec3f normal() {
		return normal(new Vec3f());
	}
	
	public Vec3f normal(Vec3f dest) {
		/*
		https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal
		 */
		Vec3f a = vector(1).sub(vector(0));
		Vec3f b = vector(2).sub(vector(0));
		dest.x = a.y * b.z - a.z * b.y;
		dest.y = a.z * b.x - a.x * b.z;
		dest.z = a.x * b.y - a.y * b.x;
		return dest;
	}
	
	public AABB6f getAABB(){
		AABB6f aabb = new AABB6f();
		for (int i = 0; i < 3; i++){
			Vec3f vec = vector(i);
			aabb.min.min(vec);
			aabb.max.max(vec);
		}
		return aabb;
	}

	public boolean intersects(AABB6f aabb) {
		/*
		https://github.com/juj/MathGeoLib/blob/master/src/Geometry/Triangle.cpp#L697
		*/
		Vec3f a = vector(0);
		Vec3f b = vector(1);
		Vec3f c = vector(2);

		Vec3f tMin = new Vec3f(Float.MAX_VALUE);
		Vec3f tMax = new Vec3f(-Float.MAX_VALUE);
		tMin.min(a);
		tMin.min(b);
		tMin.min(c);
		tMax.max(a);
		tMax.max(b);
		tMax.max(c);

		if (tMin.x >= aabb.max.x || tMax.x <= aabb.min.x
			|| tMin.y >= aabb.max.y || tMax.y <= aabb.min.y
			|| tMin.z >= aabb.max.z || tMax.z <= aabb.min.z) {
			return false;
		}

		Vec3f center = aabb.getCenter();
		Vec3f h = aabb.max.clone().sub(center);

		Vec3f[] t = new Vec3f[]{
			b.clone().sub(a),
			c.clone().sub(a),
			c.clone().sub(b)
		};

		Vec3f ac = a.clone().sub(center);

		Vec3f n = t[0].getCross(t[1]);
		float s = n.dot(ac);
		float r = Math.abs(h.dot(n.clone().absolute()));
		if (Math.abs(s) >= r) {
			return false;
		}

		Vec3f[] at = new Vec3f[]{t[0].clone(), t[1].clone(), t[2].clone()};
		for (Vec3f abs : at) {
			abs.absolute();
		}

		Vec3f bc = b.clone().sub(center);
		Vec3f cc = c.clone().sub(center);

		// eX <cross> t[0]
		float d1 = t[0].y * ac.z - t[0].z * ac.y;
		float d2 = t[0].y * cc.z - t[0].z * cc.y;
		float tc = (d1 + d2) * 0.5f;
		r = Math.abs(h.y * at[0].z + h.z * at[0].y);
		if (r + Math.abs(tc - d1) < Math.abs(tc)) {
			return false;
		}

		// eX <cross> t[1]
		d1 = t[1].y * ac.z - t[1].z * ac.y;
		d2 = t[1].y * bc.z - t[1].z * bc.y;
		tc = (d1 + d2) * 0.5f;
		r = Math.abs(h.y * at[1].z + h.z * at[1].y);
		if (r + Math.abs(tc - d1) < Math.abs(tc)) {
			return false;
		}

		// eX <cross> t[2]
		d1 = t[2].y * ac.z - t[2].z * ac.y;
		d2 = t[2].y * bc.z - t[2].z * bc.y;
		tc = (d1 + d2) * 0.5f;
		r = Math.abs(h.y * at[2].z + h.z * at[2].y);
		if (r + Math.abs(tc - d1) < Math.abs(tc)) {
			return false;
		}

		// eY <cross> t[0]
		d1 = t[0].z * ac.x - t[0].x * ac.z;
		d2 = t[0].z * cc.x - t[0].x * cc.z;
		tc = (d1 + d2) * 0.5f;
		r = Math.abs(h.x * at[0].z + h.z * at[0].x);
		if (r + Math.abs(tc - d1) < Math.abs(tc)) {
			return false;
		}

		// eY <cross> t[1]
		d1 = t[1].z * ac.x - t[1].x * ac.z;
		d2 = t[1].z * bc.x - t[1].x * bc.z;
		tc = (d1 + d2) * 0.5f;
		r = Math.abs(h.x * at[1].z + h.z * at[1].x);
		if (r + Math.abs(tc - d1) < Math.abs(tc)) {
			return false;
		}

		// eY <cross> t[2]
		d1 = t[2].z * ac.x - t[2].x * ac.z;
		d2 = t[2].z * bc.x - t[2].x * bc.z;
		tc = (d1 + d2) * 0.5f;
		r = Math.abs(h.x * at[2].z + h.z * at[2].x);
		if (r + Math.abs(tc - d1) < Math.abs(tc)) {
			return false;
		}

		// eZ <cross> t[0]
		d1 = t[0].x * ac.y - t[0].y * ac.x;
		d2 = t[0].x * cc.y - t[0].y * cc.x;
		tc = (d1 + d2) * 0.5f;
		r = Math.abs(h.y * at[0].x + h.x * at[0].y);
		if (r + Math.abs(tc - d1) < Math.abs(tc)) {
			return false;
		}

		// eZ <cross> t[1]
		d1 = t[1].x * ac.y - t[1].y * ac.x;
		d2 = t[1].x * bc.y - t[1].y * bc.x;
		tc = (d1 + d2) * 0.5f;
		r = Math.abs(h.y * at[1].x + h.x * at[1].y);
		if (r + Math.abs(tc - d1) < Math.abs(tc)) {
			return false;
		}

		// eZ <cross> t[2]
		d1 = t[2].x * ac.y - t[2].y * ac.x;
		d2 = t[2].x * bc.y - t[2].y * bc.x;
		tc = (d1 + d2) * 0.5f;
		r = Math.abs(h.y * at[2].x + h.x * at[2].y);
		if (r + Math.abs(tc - d1) < Math.abs(tc)) {
			return false;
		}

		// No separating axis exists, the AABB and triangle intersect.
		return true;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other != null && other instanceof Trianglef) {
			Trianglef t = (Trianglef) other;
			return Arrays.equals(t.x, x) && Arrays.equals(t.y, y) && Arrays.equals(t.z, z);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 37 * hash + Arrays.hashCode(this.x);
		hash = 37 * hash + Arrays.hashCode(this.y);
		hash = 37 * hash + Arrays.hashCode(this.z);
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			sb.append("V").append(i).append(": ").append(x[i]).append("/").append(y[i]).append("/").append(z[i]);
		}
		return sb.toString();
	}
}
