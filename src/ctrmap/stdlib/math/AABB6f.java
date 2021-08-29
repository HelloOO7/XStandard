package ctrmap.stdlib.math;

import ctrmap.stdlib.math.vec.Vec3f;

public class AABB6f {

	public Vec3f min = new Vec3f(Float.MAX_VALUE);
	public Vec3f max = new Vec3f(-Float.MAX_VALUE);

	public boolean contains(Vec3f point) {
		return containsIgnoreY(point) && point.y >= min.y && point.y <= max.y;
	}

	public boolean containsIgnoreY(Vec3f point) {
		return point.x >= min.x && point.z >= min.z && point.x <= max.x && point.z <= max.z;
	}

	public void set(AABB6f aabb) {
		min.set(aabb.min);
		max.set(aabb.max);
	}

	public void minmax(AABB6f aabb) {
		min.min(aabb.min);
		max.min(aabb.max);
	}

	public void add(float x, float y, float z) {
		min.add(x, y, z);
		max.add(x, y, z);
	}

	public void div(float value) {
		min.div(value);
		max.div(value);
	}

	public void div(float x, float y, float z) {
		min.div(x, y, z);
		max.div(x, y, z);
	}

	public Vec3f getDimensions() {
		Vec3f diff = new Vec3f();
		max.sub(min, diff);
		return diff;
	}

	public Vec3f getCenter() {
		Vec3f diff = new Vec3f(min);
		diff.add(max);
		diff.mul(0.5f);
		return diff;
	}

	public void divUnsigned(float x, float y, float z) {
		Vec3f dim = new Vec3f();
		max.sub(min, dim);
		dim.div(x, y, z);
		min.add(dim, max);
	}

	public boolean intersects(AABB6f b) {
		return (min.x <= b.max.x && max.x >= b.min.x)
			&& (min.y <= b.max.y && max.x >= b.min.y)
			&& (min.z <= b.max.z && max.z >= b.min.z);
	}

	@Override
	public String toString() {
		return "Min: " + min + " | Max: " + max;
	}
}
