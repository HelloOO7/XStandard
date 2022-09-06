package xstandard.io.base.impl.ext.data.interpretation;

import java.io.IOException;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.iface.WriteableStream;

public interface IDataInterpreter {

	public long readLong(ReadableStream stm) throws IOException;
	public int readInt(ReadableStream stm) throws IOException;
	public int readInt24(ReadableStream stm) throws IOException;
	public short readShort(ReadableStream stm) throws IOException;
	public byte readByte(ReadableStream stm) throws IOException;
	public int readSized(ReadableStream stm, int bytes) throws IOException;
	
	public void writeLong(WriteableStream stm, long value) throws IOException;
	public void writeInt(WriteableStream stm, int value) throws IOException;
	public void writeInt24(WriteableStream stm, int value) throws IOException;
	public void writeShort(WriteableStream stm, int value) throws IOException;
	public void writeByte(WriteableStream stm, int value) throws IOException;
	public void writeSized(WriteableStream stm, int value, int size) throws IOException;
}
