
package ctrmap.stdlib.net;

import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.accessors.MemoryFile;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class FileDownloader {
	public static InputStream getNetworkStream(String url){
		try {
			return new URL(url).openStream();
		} catch (MalformedURLException ex) {
			Logger.getLogger(FileDownloader.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(FileDownloader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	public static MemoryFile downloadToMemory(String url){
		return new MemoryFile(url, FSUtil.readStreamToBytes(new BufferedInputStream(getNetworkStream(url))));
	}
	
	public static void downloadToFile(File f, String url){
		try {
			FileOutputStream fstrm = new FileOutputStream(f);
			InputStream urlStream = getNetworkStream(url);
			fstrm.getChannel().transferFrom(Channels.newChannel(urlStream), 0, urlStream.available());
		} catch (IOException ex) {
			Logger.getLogger(FileDownloader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
