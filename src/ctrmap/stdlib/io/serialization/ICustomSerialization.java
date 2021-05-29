package ctrmap.stdlib.io.serialization;

import java.io.IOException;

public interface ICustomSerialization {
    public void deserialize(BinaryDeserializer deserializer) throws IOException;
    public boolean preSerialize(BinarySerializer serializer) throws IOException;
    public void postSerialize(BinarySerializer serializer) throws IOException;
}
