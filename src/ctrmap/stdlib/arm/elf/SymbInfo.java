
package ctrmap.stdlib.arm.elf;

/**
 *
 */
public class SymbInfo {
	public String name;
	public int absoluteAddress;
	
	public SymbInfo(String name, int addr){
		this.name = name;
		absoluteAddress = addr;
	}
}
