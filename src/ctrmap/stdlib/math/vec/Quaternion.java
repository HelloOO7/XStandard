package ctrmap.stdlib.math.vec;

import ctrmap.stdlib.math.MatrixUtil;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

public class Quaternion extends Quaternionf {
	public Quaternion(Quaternionf q){
		super(q);
	}
	
	public Quaternion(){
		super();
	}
	
	public Quaternion(Vec3f euler){
		this(euler.x, euler.y, euler.z);
	}
	
	public Quaternion(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
		w = in.readFloat();
	}
	
	public Quaternion(float x, float y, float z){
		super();
		rotateLocalX(x);
		rotateLocalY(y);
		rotateLocalZ(z);
	}	
	
	@Override
	public Quaternion clone(){
		return new Quaternion(this);
	}
	
	public Quaternion mul(Quaternion q){
		super.mul(q);
		return this;
	}
	
	public void write(DataOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
		out.writeFloat(w);
	}

	public Quaternion rotateXYZ(Vector3fc vec){
		super.rotateXYZ(vec.x(), vec.y(), vec.z());
		return this;
	}
	
	public Quaternion rotateZYX(Vector3fc vec){
		super.rotateZYX(vec.z(), vec.y(), vec.x());
		return this;
	}
	
	public Vec3f getEulerRotation(){
		return MatrixUtil.getQuatEuler(this);
		/*Vec3f v = new Vec3f();
		getEulerAnglesXYZ(v);
		return v;*/
	}
	
	public Matrix4 getRotationMatrix(){
		Matrix4 m = new Matrix4();
		get(m);
		return m;
	}
}
