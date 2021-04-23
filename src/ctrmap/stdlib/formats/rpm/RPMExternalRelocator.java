package ctrmap.stdlib.formats.rpm;

public interface RPMExternalRelocator {
	public void processExternalRelocation(RPM rpm, RPMRelocation rel);
}
