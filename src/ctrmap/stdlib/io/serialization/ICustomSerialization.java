package ctrmap.stdlib.io.serialization;

import java.io.IOException;

/**
 * Interface for carrying out user-defined operations during de/serialization.
 */
public interface ICustomSerialization {

	/**
	 * Action to perform after deserialization.
	 *
	 * @param deserializer
	 * @throws IOException
	 */
	public void deserialize(BinaryDeserializer deserializer) throws IOException;

	/**
	 * Action to perform before the object is written.
	 *
	 * @param serializer
	 * @return True if the serializer should skip writing the object fields.
	 * @throws IOException
	 */
	public boolean preSerialize(BinarySerializer serializer) throws IOException;

	/**
	 * Action to perform after the object is written.
	 *
	 * @param serializer
	 * @throws IOException
	 */
	public void postSerialize(BinarySerializer serializer) throws IOException;
}
