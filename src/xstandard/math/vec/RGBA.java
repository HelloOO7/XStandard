package xstandard.math.vec;

import xstandard.io.serialization.BinaryDeserializer;
import xstandard.io.serialization.BinarySerializer;
import xstandard.io.serialization.ICustomSerialization;
import xstandard.io.serialization.annotations.Ignore;
import java.awt.Color;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class RGBA implements ICustomSerialization {

	@Ignore
	public short r;
	@Ignore
	public short g;
	@Ignore
	public short b;
	@Ignore
	public short a;

	public static final RGBA BLACK = new RGBA(0, 0, 0, 255);
	public static final RGBA WHITE = new RGBA(255, 255, 255, 255);
	public static final RGBA RED = new RGBA(255, 0, 0, 255);
	public static final RGBA GREEN = new RGBA(0, 255, 0, 255);

	public RGBA(int r, int g, int b, int a) {
		this.r = (short) r;
		this.g = (short) g;
		this.b = (short) b;
		this.a = (short) a;
	}

	public RGBA(int rgb, boolean a) {
		r = (short) (rgb & 0xFF);
		g = (short) ((rgb >> 8) & 0xFF);
		b = (short) ((rgb >> 16) & 0xFF);
		if (a) {
			this.a = (short) (rgb >> 24 & 0xFF);
		} else {
			this.a = 0xFF;
		}
	}

	public static RGBA fromARGB(int argb) {
		return new RGBA((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
	}

	public RGBA(float r, float g, float b, float a) {
		this.r = (short) (r * 255f);
		this.g = (short) (g * 255f);
		this.b = (short) (b * 255f);
		this.a = (short) (a * 255f);
	}

	public RGBA() {
		this(WHITE);
	}

	public RGBA(RGBA rgba) {
		set(rgba);
	}

	public RGBA(byte[] bytes) {
		this(bytes, 0);
	}
	
	public RGBA(byte[] bytes, int offset) {
		r = (short) (bytes[offset + 0] & 0xFF);
		g = (short) (bytes[offset + 1] & 0xFF);
		b = (short) (bytes[offset + 2] & 0xFF);
		a = (short) (bytes[offset + 3] & 0xFF);
	}

	public RGBA(DataInput dis) throws IOException {
		set(dis);
	}

	public RGBA(RGBA left, RGBA right, float weight) {
		set(left);
		lerp(right, weight);
	}

	public RGBA(Vec3f vec3) {
		this(vec3.x, vec3.y, vec3.z, 1.0f);
	}

	public RGBA(Vec4f vec4) {
		this(vec4.x, vec4.y, vec4.z, vec4.w);
	}

	public RGBA(float[] floats) {
		this(floats, 4);
	}

	public RGBA(float[] floats, int count) {
		if (count > 0) {
			r = (short) (floats[0] * 255f);
		}
		if (count > 1) {
			g = (short) (floats[1] * 255f);
		}
		if (count > 2) {
			b = (short) (floats[2] * 255f);
		}
		if (count > 3) {
			a = (short) (floats[3] * 255f);
		}
	}

	public RGBA(Color col) {
		set(col);
	}

	public void set(DataInput in) throws IOException {
		r = (short) in.readUnsignedByte();
		g = (short) in.readUnsignedByte();
		b = (short) in.readUnsignedByte();
		a = (short) in.readUnsignedByte();
	}

	public RGBA set(Color col) {
		r = (short) col.getRed();
		g = (short) col.getGreen();
		b = (short) col.getBlue();
		a = (short) col.getAlpha();
		return this;
	}

	public void setARGB(int argb) {
		set((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
	}

	public RGBA set(RGBA rgba) {
		r = rgba.r;
		g = rgba.g;
		b = rgba.b;
		a = rgba.a;
		return this;
	}

	public RGBA set(Vec4f vec4) {
		return set(vec4.x, vec4.y, vec4.z, vec4.w);
	}

	public RGBA set(Vec3f vec) {
		return set(vec, 1.0f);
	}

	public RGBA set(Vec3f vec, float alpha) {
		return set(vec.x, vec.y, vec.z, alpha);
	}

	public RGBA set(int r, int g, int b, int a) {
		this.r = (short) (r);
		this.g = (short) (g);
		this.b = (short) (b);
		this.a = (short) (a);
		return this;
	}

	public RGBA set(float r, float g, float b, float a) {
		this.r = (short) (r * 255f);
		this.g = (short) (g * 255f);
		this.b = (short) (b * 255f);
		this.a = (short) (a * 255f);
		return this;
	}

	public RGBA mul(RGBA other) {
		r = mulComp(r, other.r);
		g = mulComp(g, other.g);
		b = mulComp(b, other.b);
		a = mulComp(a, other.a);
		return this;
	}

	private static short mulComp(int comp1, int comp2) {
		return (short) ((comp1 * comp2 + 255) >> 8);
	}

	public RGBA lerp(RGBA right, float weight) {
		if (weight == 1f) {
			set(right);
		} else if (weight != 0f) {
			r = (short) (r + weight * (right.r - r));
			g = (short) (g + weight * (right.g - g));
			b = (short) (b + weight * (right.b - b));
			a = (short) (a + weight * (right.a - a));
		}
		return this;
	}

	public Color toColor() {
		return new Color(r, g, b, a);
	}

	public Vec4f toVector4() {
		return toVector4(new Vec4f());
	}
	
	public Vec4f toVector4(Vec4f dest) {
		dest.set(getR(), getG(), getB(), getA());
		return dest;
	}

	public byte[] toByteArray() {
		return toByteArray(new byte[4], 0);
	}

	public byte[] toByteArray(byte[] bytes, int offset) {
		bytes[offset + 0] = (byte) r;
		bytes[offset + 1] = (byte) g;
		bytes[offset + 2] = (byte) b;
		bytes[offset + 3] = (byte) a;
		return bytes;
	}

	public float[] getFloatUniform() {
		return new float[]{getR(), getG(), getB(), getA()};
	}

	private static final float _1_255 = 1 / 255f;

	public float getR() {
		return r * _1_255;
	}

	public float getG() {
		return g * _1_255;
	}

	public float getB() {
		return b * _1_255;
	}

	public float getA() {
		return a * _1_255;
	}

	public void write(DataOutput dos) throws IOException {
		dos.write(r);
		dos.write(g);
		dos.write(b);
		dos.write(a);
	}

	@Override
	public RGBA clone() {
		return new RGBA(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("R: ");
		sb.append(r);
		sb.append(", G: ");
		sb.append(g);
		sb.append(", B: ");
		sb.append(b);
		sb.append(", A: ");
		sb.append(a);
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof RGBA) {
			RGBA c = (RGBA) o;
			return c.r == r && c.g == g && c.b == b && c.a == a;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 47 * hash + this.r;
		hash = 47 * hash + this.g;
		hash = 47 * hash + this.b;
		hash = 47 * hash + this.a;
		return hash;
	}

	@Override
	public void deserialize(BinaryDeserializer deserializer) throws IOException {
		r = (short) deserializer.baseStream.readUnsignedByte();
		g = (short) deserializer.baseStream.readUnsignedByte();
		b = (short) deserializer.baseStream.readUnsignedByte();
		a = (short) deserializer.baseStream.readUnsignedByte();
	}

	@Override
	public boolean preSerialize(BinarySerializer serializer) throws IOException {
		return false;
	}

	@Override
	public void postSerialize(BinarySerializer serializer) throws IOException {
		write(serializer.baseStream);
	}
}
