package ctrmap.stdlib.cli;

import java.util.ArrayList;
import java.util.List;

public class ArgumentContent {
	public String key;
	public List<Object> contents = new ArrayList<>();
	
	public int valueCount() {
		return contents.size();
	}
	
	public boolean booleanValue(){
		return booleanValue(0);
	}
	
	public int intValue(){
		return intValue(0);
	}
	
	public float floatValue(){
		return floatValue(0);
	}
	
	public String stringValue(){
		return stringValue(0);
	}
	
	public boolean exists(){
		return !contents.isEmpty() && contents.get(0) != null;
	}
	
	public boolean booleanValue(int idx){
		rangeCheck(idx);
		return (Boolean)contents.get(idx);
	}
	
	public int intValue(int idx){
		rangeCheck(idx);
		return (Integer)contents.get(idx);
	}
	
	public float floatValue(int idx){
		rangeCheck(idx);
		return (Float)contents.get(idx);
	}
	
	public String stringValue(int idx){
		rangeCheck(idx);
		return String.valueOf(contents.get(idx));
	}
	
	private void rangeCheck(int idx){
		if (contents.size() <= idx){
			throw new ArrayIndexOutOfBoundsException("Requested parameter " + idx + " for " + key + " out of range.");
		}
	}
}
