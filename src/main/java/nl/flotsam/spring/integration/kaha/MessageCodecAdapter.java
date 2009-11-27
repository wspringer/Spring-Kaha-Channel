package nl.flotsam.spring.integration.kaha;

import org.apache.activemq.kaha.Marshaller;
import org.springframework.integration.core.Message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A helper class, adapting the {@link MessageCodec} interface to implement the {@link
 * org.apache.activemq.kaha.Marshaller} interface.
 *
 * @param <T> The type of payload of the message to be encodec/decoded.
 */
class MessageCodecAdapter<T> implements Marshaller<Message<T>> {

    /**
     * The {@link MessageCodec} for which this object implements a {@link org.apache.activemq.kaha.Marshaller} wrapper.
     */
    private final MessageCodec<T> codec;

    public MessageCodecAdapter(MessageCodec<T> codec) {
        assert codec != null;
        this.codec = codec;
    }

    @Override
    public void writePayload(Message<T> object, DataOutput dataOut) throws IOException {
        codec.encode(object, dataOut);
    }

    @Override
    public Message<T> readPayload(DataInput dataIn) throws IOException {
        return codec.decode(dataIn);
    }
}
