package nl.flotsam.spring.integration.kaha;

import org.junit.Test;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import java.io.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SimpleMessageCodecTest {

    @Test
    public void shouldEncodeAndDecodeMessage() throws IOException {
        Message<String> message = MessageBuilder.withPayload("John Doe")
                .setHeader("age", 37)
                .setHeader("missing", true)
                .build();
        ByteArrayOutputStream sout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(sout);
        SimpleMessageCodec<String> codec = new SimpleMessageCodec<String>();
        codec.encode(message, out);
        Message<String> decoded = codec.decode(new DataInputStream(new ByteArrayInputStream(sout.toByteArray())));
        assertThat(decoded.getPayload(), is("John Doe"));
        assertThat((Integer) decoded.getHeaders().get("age"), is(37));
        assertThat((Boolean) decoded.getHeaders().get("missing"), is(true));

        // Make sure that the message ID is preserved
        assertThat(message.getHeaders().get("ID"), is(decoded.getHeaders().get("ID")));
    }
    
}
