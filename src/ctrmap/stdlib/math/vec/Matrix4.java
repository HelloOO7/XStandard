
package ctrmap.stdlib.math.vec;

import ctrmap.stdlib.math.MathEx;
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
	
	public Matrix4 getRotationMtx(){
		Matrix4 mtx = new Matrix4();
		mtx.set3x3(this);
		mtx.scale(getScale().recip());
		return mtx;
	}
	
	public Matrix4 clearRotation() {
		setRotationZYX(0, 0, 0);
		return this;
	}
	
	public Matrix4 rotateZYXDeg(float z, float y, float x){
		rotateZYX(MathEx.toRadiansf(z), MathEx.toRadiansf(y), MathEx.toRadiansf(x));
		return this;
	}
	
	public void clearRotationX(){
		float siny = -m02();
		float yAngle = (float)Math.asin(-siny);
		float cosy = (float)Math.cos(yAngle);
		float cosz = m00() / cosy;
		float sinz = m01() / cosy;
		
		float cosx = 1f; //cos 0
		float sinx = 0f;
		m10(-sinz);
		m11(cosz);
		m12(0);
		m20(cosz * siny);
		m21(sinz * siny);
		m22(cosy);
	}
	
	public void clearRotationZ(){
		float siny = -m02();
		float yAngle = (float)Math.asin(-siny);
		float cosy = (float)Math.cos(yAngle);
		float cosx = m22() / cosy;
		float sinx = m12() / cosy;
		
		float cosz = 1f; //cos 0
		float sinz = 0f;
		m00(cosy);
		m01(0);
		m10(siny * sinx);
		m11(cosx);
		m20(cosx * siny);
		m21(-sinx);
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
	
	public Vec3f getRotationTo(Vec3f vec){
		return MatrixUtil.rotationFromMatrix(this, getScale(), vec);
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
