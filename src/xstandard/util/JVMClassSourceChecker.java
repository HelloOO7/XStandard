package xstandard.util;

import java.util.Objects;

public class JVMClassSourceChecker {

	public static enum ClassSource {
		JAR,
		FILE,
		
		UNKNOWN
	}

	public static ClassSource get() {
		String protocol = JVMClassSourceChecker.class.getResource("").getProtocol();
		if (Objects.equals(protocol, "jar")) {
			return ClassSource.JAR;
		} else if (Objects.equals(protocol, "file")) {
			return ClassSource.FILE;
		}
		return ClassSource.UNKNOWN;
	}
	
	public static boolean isJAR() {
		return get() == ClassSource.JAR;
	}
}
