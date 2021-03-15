
package ctrmap.stdlib.math.vec;

public class Vec2d {
	public double x;
	public double y;
	
	public Vec2d(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public Vec2d(Vec2d src){
		this.x = src.x;
		this.y = src.y;
	}
	
	public Vec2d(){
		x = 0d;
		y = 0d;
	}
	
	public void translate(Vec2d translationVector){
		translate(translationVector.x, translationVector.y);
	}
	
	public void translate(double tx, double ty){
		x += tx;
		y += ty;
	}
	
	public void scale(Vec2d scalingVector){
		scale(scalingVector.x, scalingVector.y);
	}
	
	public void scale(double sx, double sy){
		x *= sx;
		y *= sy;
	}
	
	public void rotate(double anglerad){
		double cos = Math.cos(anglerad);
		double sin = Math.sin(anglerad);
		x = x * cos - y * sin;
		y = y * cos - x * sin;
	}
}
