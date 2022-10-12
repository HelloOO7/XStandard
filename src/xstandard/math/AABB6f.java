package xstandard.math;

import xstandard.math.vec.Vec3f;

public class AABB6f {

	public Vec3f min = new Vec3f(Float.MAX_VALUE);
	public Vec3f max = new Vec3f(-Float.MAX_VALUE);
	
	public AABB6f() {
		
	}
	
	public AABB6f(AABB6f src) {
		set(src);
	}
	
	public void zero() {
		min.zero();
		max.zero();
	}

	public void reset() {
		min.set(Float.MAX_VALUE);
		max.set(-Float.MAX_VALUE);
	}

	public void update(Vec3f vec) {
		min.min(vec);
		max.max(vec);
	}

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
		max.max(aabb.max);
	}

	public void grow(float inc) {
		grow(inc, inc, inc);
	}

	public void grow(float incX, float incY, float incZ) {
		min.sub(incX, incY, incZ);
		max.add(incX, incY, incZ);
	}

	public void add(float x, float y, float z) {
		min.add(x, y, z);
		max.add(x, y, z);
	}
	
	public void add(Vec3f vec) {
		min.add(vec);
		max.add(vec);
	}
	
	public void mul(float value) {
		min.mul(value);
		max.mul(value);
	}

	public void mul(float x, float y, float z) {
		min.mul(x, y, z);
		max.mul(x, y, z);
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
	
	public Vec3f[] corners() {
		return new Vec3f[] {
			cornerXnYnZn(),
			cornerXnYnZp(),
			cornerXnYpZn(),
			cornerXnYpZp(),
			cornerXpYnZn(),
			cornerXpYnZp(),
			cornerXpYpZn(),
			cornerXpYpZp()
		};
	}

	public Vec3f cornerXnYnZn() {
		return min.clone();
	}

	public Vec3f cornerXnYnZp() {
		return new Vec3f(min.x, min.y, max.z);
	}

	public Vec3f cornerXnYpZp() {
		return new Vec3f(min.x, max.y, max.z);
	}
	
	public Vec3f cornerXnYpZn() {
		return new Vec3f(min.x, max.y, min.z);
	}

	public Vec3f cornerXpYpZp() {
		return max.clone();
	}

	public Vec3f cornerXpYnZp() {
		return new Vec3f(max.x, min.y, max.z);
	}

	public Vec3f cornerXpYpZn() {
		return new Vec3f(max.x, max.y, min.z);
	}

	public Vec3f cornerXpYnZn() {
		return new Vec3f(max.x, min.y, min.z);
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
