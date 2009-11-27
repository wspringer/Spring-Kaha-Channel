package nl.flotsam.spring.integration.kaha;

import org.springframework.integration.core.Message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface MessageCodec<T> {

    void encode(Message<T> value, DataOutput out) throws IOException;

    Message<T> decode(DataInput in) throws IOException;
    
}
