
package ctrmap.stdlib.math.vec;

import ctrmap.stdlib.math.MatrixUtil;
import java.io.DataInput;
import java.io.IOException;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Vector3fc;

public class Matrix4 extends Matrix4f{
	
	public Matrix4(float[] flt){
		this();
		set(flt);
	}
	
	public Matrix4(){
		super();
	}
	
	public Matrix4(Matrix4f mtx){
		super(mtx);
	}
	
	public Matrix4(Matrix4x3f mtx){
		super(mtx);
	}
	
	public Matrix4(Matrix3f mtx){
		super(mtx);
	}
	
	public Matrix4(DataInput in) throws IOException{
		float[] buf = new float[16];
		for (int i = 0; i < 16; i++){
			buf[i] = in.readFloat();
		}
		set(buf);
	}
	
	public static Matrix4 createTranslation(Vec3f t){
		return new Matrix4().translation(t);
	}
	
	public static Matrix4 createTranslation(float x, float y, float z){
		return new Matrix4().translation(x, y, z);
	}
	
	@Override
	public Matrix4 clone(){
		return new Matrix4(this);
	}
	
	@Override
	public Matrix4 invert(){
		super.invert();
		return this;
	}
	
	@Override
	public Matrix4 translation(float x, float y, float z){
		super.translation(x, y, z);
		return this;
	}
	
	@Override
	public Matrix4 translation(Vector3fc vec){
		super.translation(vec);
		return this;
	}
	
	public Matrix4 getInverseMatrix(){
		return clone().invert();
	}
	
	public float[] getMatrix(){
		return super.get(new float[16]);
	}
	
	public Matrix4 clearTranslation() {
		setTranslation(0, 0, 0);
		return this;
	}
	
	public Matrix4 rotate(Vec3f vec){
		rotateZYX(vec);
		return this;
	}
	
	public Vec3f getTranslation(){
		return new Vec3f(super.getTranslation(new Vec3f()));
	}
	
	public Vec3f getScale(){
		return new Vec3f(super.getScale(new Vec3f()));
	}
	
	public Vec3f getRotation(){
		return MatrixUtil.rotationFromMatrix(this, getScale());
	}
	
	public Vec3f getRotation(Vec3f scale){
		return MatrixUtil.rotationFromMatrix(this, scale);
	}
	
	public Matrix4 multiplyRight(Matrix4 multiplier){
		super.mulLocal(multiplier);
		return this;
	}
	
	public Vec3f multiplyVector(Vec3f base){
		return MatrixUtil.transformVector(base, this);
	}
}
