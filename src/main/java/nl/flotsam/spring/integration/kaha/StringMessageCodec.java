package nl.flotsam.spring.integration.kaha;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StringMessageCodec implements MessageCodec<String> {

    @Override
    public void encode(Message<String> value, DataOutput out) throws IOException {
        out.writeUTF(value.getPayload());
    }

    @Override
    public Message<String> decode(DataInput in) throws IOException {
        return MessageBuilder.withPayload(in.readUTF()).build();
    }
}
