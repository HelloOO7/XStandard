/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ctrmap.stdlib.math;

/**
 *
 */
public class SLERP {
	public static float angleAngle(float angle1, float angle2, float weight){
		float angleBetween = angle2 - angle1;
		
		float sin1 = (float)Math.sin(angle1 * (1 - weight));
		float sin2 = (float)Math.sin(angle2 * weight);
		float sinAB = (float)Math.sin(angleBetween);
		
		return (sin1 / sinAB) * angle1 + (sin2 / sinAB) * angle2;
	}
}
