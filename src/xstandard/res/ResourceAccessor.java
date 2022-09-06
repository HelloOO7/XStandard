package xstandard.res;

import java.io.InputStream;

public class ResourceAccessor {
	
	private final String rootClasspath;
	
	public ResourceAccessor(String rootClasspath) {
		this.rootClasspath = rootClasspath;
	}
	
	public InputStream getStream(String name) {
		return ResourceAccess.getStream(rootClasspath + "/" + name);
	}
	
	public byte[] getByteArray(String name) {
		return ResourceAccess.getByteArray(rootClasspath + "/" + name);
	}
}
