
package ctrmap.stdlib.arm.elf.rpmconv.exec;

import ctrmap.stdlib.io.util.BitUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ctrmap.stdlib.arm.elf.rpmconv.ExternalSymbolDB;

/**
 *
 */
public class ETExecRelocationState {
	private ExternalSymbolDB efdb;
	
	private Map<Integer, Integer> sourceSectionOffsets = new HashMap<>();
	private Map<Integer, Integer> targetSectionOffsets = new HashMap<>();
	
	public ETExecRelocationState(List<ExecElfSection> sections, ExternalSymbolDB efdb){
		this.efdb = efdb;
		int targetOffset = 0;
		for (ExecElfSection c : sections){
			sourceSectionOffsets.put(c.id, c.getOriginalSectionOffset());
			targetSectionOffsets.put(c.id, targetOffset);
			System.out.println("section " + c.type + " size " + c.getSectionSize() + " placed at " + Integer.toHexString(targetOffset) + " orig " + c.getOriginalSectionOffset());
			targetOffset += c.getSectionSize();
			targetOffset = BitUtils.getPaddedInteger(targetOffset, 4);
		}
	}
	
	public int getTargetSectionOffsetById(int id){
		return targetSectionOffsets.getOrDefault(id, -1);
	}
	
	public int getSourceSectionOffsetById(int id){
		return sourceSectionOffsets.getOrDefault(id, -1);
	}
	
	public ExternalSymbolDB getESDB(){
		return efdb;
	}
}
