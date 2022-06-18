package ctrmap.stdlib.math.vec;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.joml.Vector2f;

public class Vec2f extends Vector2f implements AbstractVector {

	public static final Vec2f ONE() {
		return new Vec2f(1f, 1f);
	}
	
	public static final Vec2f ZERO() {
		return new Vec2f(0f, 0f);
	}

	public Vec2f(float x, float y) {
		super(x, y);
	}

	public Vec2f(Vec2f src) {
		super(src);
	}

	public Vec2f(float[] src) {
		x = src[0];
		y = src[1];
	}

	public Vec2f(float scalar) {
		super(scalar);
	}

	public Vec2f() {
		x = 0f;
		y = 0f;
	}

	public Vec2f(DataInput dis) throws IOException {
		x = dis.readFloat();
		y = dis.readFloat();
	}

	public Vec2f multiplyScalar(float mul) {
		super.mul(mul);
		return this;
	}

	public void translate(float tx, float ty) {
		x += tx;
		y += ty;
	}

	public void translate(Vec2f vec) {
		x += vec.x;
		y += vec.y;
	}

	public void scale(Vec2f scalingVector) {
		scale(scalingVector.x, scalingVector.y);
	}

	public void scale(float sx, float sy) {
		x *= sx;
		y *= sy;
	}

	public void rotate(float anglerad) {
		double cos = Math.cos(anglerad);
		double sin = Math.sin(anglerad);
		float xf = (float) (x * cos - y * sin);
		y = (float) (y * cos + x * sin);
		x = xf;
	}

	public Vec2f swapComponents() {
		float temp = x;
		x = y;
		y = temp;
		return this;
	}
	
	public float[] get(float[] arr){
		arr[0] = x;
		arr[1] = y;
		return arr;
	}

	@Override
	public Vec2f toVec2() {
		return this;
	}

	@Override
	public Vec3f toVec3() {
		return new Vec3f(x, y, 0f);
	}

	@Override
	public Vec4f toVec4() {
		return new Vec4f(x, y, 0f, 0f);
	}

	public void write(DataOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
	}

	@Override
	public String toString() {
		return "(" + x + "; " + y + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Vec2f) {
			Vec2f e = (Vec2f) o;
			return x == e.x && y == e.y;
		}
		return false;
	}
}
