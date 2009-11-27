package nl.flotsam.spring.integration.kaha;

import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;
import org.springframework.integration.message.MessageBuilder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A {@link nl.flotsam.spring.integration.kaha.MessageCodec} that supports most of the basic types for the message
 * payload and its headers. <p/> <p> The types supported are: {@link String}, {@link Integer}, {@link Long}, {@link
 * Boolean}, {@link UUID}. As long as your payload and headers are of one of these types, the {@link
 * nl.flotsam.spring.integration.kaha.SimpleMessageCodec} will be able to encode and decode them. </p>
 *
 * @param <T> The type of payload.
 */
public class SimpleMessageCodec<T> implements MessageCodec<T> {

    private final static Map<Class<?>, TypeTag> TAGS_BY_TYPE = new HashMap<Class<?>, TypeTag>();
    private final static TypeTag[] TAGS_BY_INDEX = new TypeTag[TypeTag.values().length];

    static {
        for (TypeTag encoded : TypeTag.values()) {
            TAGS_BY_TYPE.put(encoded.getType(), encoded);
            TAGS_BY_INDEX[encoded.getNumeric()] = encoded;
        }
    }

    private enum TypeTag {

        String(String.class, 0),
        Integer(Integer.class, 1),
        Long(Long.class, 2),
        Boolean(Boolean.class, 3),
        UUID(UUID.class, 4);

        private final Class<?> type;
        private final int tag;

        TypeTag(Class<?> type, int tag) {
            this.type = type;
            this.tag = tag;
        }

        public int getNumeric() {
            return tag;
        }

        public Class<?> getType() {
            return type;
        }

    }

    @Override
    public void encode(Message<T> value, DataOutput out) throws IOException {
        writeTypedValue(value.getPayload(), out);
        MessageHeaders headers = value.getHeaders();
        out.writeInt(headers.size());
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            out.writeUTF(entry.getKey());
            writeTypedValue(entry.getValue(), out);
        }
    }

    @Override
    public Message<T> decode(DataInput in) throws IOException {
        Object payload = readTypedValue(in);
        int size = in.readInt();
        Map<String, Object> headers = new HashMap<String, Object>();
        for (int i = 0; i < size; i++) {
            String key = in.readUTF();
            Object value = readTypedValue(in);
            headers.put(key, value);
        }
        return (Message<T>) MessageBuilder.withPayload(payload).copyHeaders(headers).build();
    }

    private void writeTypedValue(Object value, DataOutput out) throws IOException {
        assert value != null;
        assert out != null;
        TypeTag tag = TAGS_BY_TYPE.get(value.getClass());
        assert tag != null;
        out.writeByte(tag.getNumeric());
        switch (tag) {
            case String:
                out.writeUTF((String) value);
                break;
            case Boolean:
                out.writeBoolean((Boolean) value);
                break;
            case Integer:
                out.writeInt((Integer) value);
                break;
            case Long:
                out.writeLong((Long) value);
                break;
            case UUID:
                out.writeLong(((UUID) value).getMostSignificantBits());
                out.writeLong(((UUID) value).getLeastSignificantBits());
                break;
            default:
                throw new IllegalStateException("No mapping defined for " + value.getClass());
        }
    }

    private Object readTypedValue(DataInput in) throws IOException {
        TypeTag tag = TAGS_BY_INDEX[in.readByte()];
        switch (tag) {
            case String:
                return in.readUTF();
            case Boolean:
                return in.readBoolean();
            case Integer:
                return in.readInt();
            case Long:
                return in.readLong();
            case UUID:
                return new UUID(in.readLong(), in.readLong());
            default:
                throw new IllegalStateException("No mapping defined for " + tag);
        }
    }

}
