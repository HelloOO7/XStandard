
package ctrmap.stdlib.net;

import ctrmap.stdlib.fs.FSUtil;
import ctrmap.stdlib.fs.accessors.MemoryFile;
import ctrmap.stdlib.io.base.iface.ReadableStream;
import ctrmap.stdlib.io.base.impl.InputStreamReadable;
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
 * Very rudimentary class for downloading files from the Internet.
 */
public class FileDownloader {
	
	/**
	 * Creates a ReadableStream from a HTTP URL.
	 * Bear in mind that the stream's available() method will NOT function correctly.
	 * @param url An HTTP Internet address.
	 * @return A ReadableStream bound to the URL, or null if the URL could not be resolved.
	 */
	public static ReadableStream getNetworkStream(String url){
		try {
			return new InputStreamReadable(new URL(url).openStream());
		} catch (MalformedURLException ex) {
			Logger.getLogger(FileDownloader.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(FileDownloader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	/**
	 * Downloads a file from an URL into memory.
	 * @param url Internet address of the file.
	 * @return A MemoryFile wrapped around a byte[] of the network file's data.
	 */
	public static MemoryFile downloadToMemory(String url){
		return new MemoryFile(url, FSUtil.readStreamToBytes(new BufferedInputStream(getNetworkStream(url).getInputStream())));
	}
	
	/**
	 * Transfers a file from an URL onto the disk.
	 * @param f Destination file to download to.
	 * @param url Internet address of the file.
	 */
	public static void downloadToFile(File f, String url){
		try {
			FileOutputStream fstrm = new FileOutputStream(f);
			InputStream urlStream = getNetworkStream(url).getInputStream();
			fstrm.getChannel().transferFrom(Channels.newChannel(urlStream), 0, Long.MAX_VALUE);
		} catch (IOException ex) {
			Logger.getLogger(FileDownloader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
