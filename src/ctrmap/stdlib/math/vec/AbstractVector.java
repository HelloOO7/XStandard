package ctrmap.stdlib.math.vec;

public interface AbstractVector {
	public Vec2f toVec2();
	public Vec3f toVec3();
	public Vec4f toVec4();
	
	public float get(int component);
}
