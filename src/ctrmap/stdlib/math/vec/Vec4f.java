package ctrmap.stdlib.math.vec;

import ctrmap.stdlib.math.MathEx;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import org.joml.Vector4f;

public class Vec4f extends Vector4f {

	public Vec4f(float x, float y, float z, float w) {
		super(x, y, z, w);
	}

	public Vec4f(float[] flt) {
		x = flt[0];
		y = flt[1];
		z = 0;
		w = 1;
		if (flt.length > 2) {
			z = flt[2];

			if (flt.length > 3) {
				w = flt[3]; //might be undefined for Vec4f
			}
		}
	}

	public Vec4f(Vec4f src) {
		super(src);
	}

	public Vec4f(DataInput dis) throws IOException {
		x = dis.readFloat();
		y = dis.readFloat();
		z = dis.readFloat();
		w = dis.readFloat();
	}

	public Vec4f() {
		super();
	}
	
	public static Vec4f parseVec4f(String src){
		src = src.trim();
		if (src.startsWith("(") && src.endsWith(")")){
			src = src.substring(1, src.length() - 1);
		}
		String[] cmds = src.split(",");
		if (cmds.length != 4){
			throw new NumberFormatException("Source is not a 4-component vector - " + Arrays.toString(cmds));
		}
		Vec4f v = new Vec4f();
		v.x = Float.parseFloat(cmds[0]);
		v.y = Float.parseFloat(cmds[1]);
		v.z = Float.parseFloat(cmds[2]);
		v.w = Float.parseFloat(cmds[3]);
		return v;
	}

	public Vec2f toVec2() {
		return new Vec2f(x, y);
	}
	
	public Vec3f toVec3() {
		return new Vec3f(x, y, z);
	}
	
	public float[] get(float[] arr){
		arr[0] = x;
		arr[1] = y;
		arr[2] = z;
		arr[3] = w;
		return arr;
	}

	public void write(DataOutput out) throws IOException{
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
		out.writeFloat(w);
	}
	
	public Vec4f toVec4() {
		return this;
	}

	public Vec4f add(Vec4f additive) {
		x += additive.x;
		y += additive.y;
		z += additive.z;
		w += additive.w;
		return this;
	}
	
	public Vec4f multiplyScalar(float mul){
		x *= mul;
		y *= mul;
		z *= mul;
		w *= mul;
		return this;
	}
	
	public boolean equalsImprecise(Object o, float precision){
		if (o != null && o instanceof Vec4f){
			Vec4f v = (Vec4f)o;
			return MathEx.impreciseFloatEquals(v.x, x, precision) 
					&& MathEx.impreciseFloatEquals(v.y, y, precision) 
					&& MathEx.impreciseFloatEquals(v.z, z, precision)
					&& MathEx.impreciseFloatEquals(v.w, w, precision);
		}
		return false;
	}

	@Override
	public float length() {
		return x * x + y * y + z * z + w * w;
	}

	public float[] toFloatUniform() {
		return new float[]{x, y, z, w};
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Vec4f) {
			Vec4f v = (Vec4f) o;
			return v.x == x && v.y == y && v.z == z && v.w == w;
		}
		return false;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ", " + w + ")";
	}
}
