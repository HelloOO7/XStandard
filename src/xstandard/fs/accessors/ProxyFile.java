package xstandard.fs.accessors;

import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;

public class ProxyFile extends FSFileAdapter {

	private final String proxyPath;
	
	public ProxyFile(FSFile toWrap, String proxyPath) {
		super(toWrap);
		this.proxyPath = proxyPath;
	}

	@Override
	public String getPath() {
		return proxyPath;
	}
	
	@Override
	public String getName() {
		return FSUtil.getFileName(proxyPath);
	}
}
