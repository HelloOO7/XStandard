package ctrmap.stdlib.io;

import ctrmap.stdlib.io.base.LittleEndianDataInput;
import ctrmap.stdlib.io.iface.CloseableDataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple DataInputStream implementation with automatic reversion of LE to Java BE.
 */
public class LittleEndianDataInputStream extends LittleEndianDataInput implements CloseableDataInput {
	
	protected DataInputStream dis_handle;
	
	protected int length;

	public LittleEndianDataInputStream(InputStream in) {
		this(new DataInputStream(in));
	}
	
	public LittleEndianDataInputStream(DataInputStream dis){
		super(dis);
		dis_handle = dis;
		try {
			length = dis.available();
		} catch (IOException ex) {
			Logger.getLogger(LittleEndianDataInputStream.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public DataInputStream getRegularDis(){
		return dis_handle;
	}
	
	@Override
	public void read(byte[] b) throws IOException{
		position += dis_handle.read(b);
	}
	
	public int available() throws IOException{
		return dis_handle.available();
	}
	
	public int length(){
		return length;
	}
	
	@Override
	public void close() throws IOException{
		dis_handle.close();
	}
}
