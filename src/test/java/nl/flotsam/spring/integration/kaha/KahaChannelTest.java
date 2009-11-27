package nl.flotsam.spring.integration.kaha;

import org.apache.activemq.kaha.ListContainer;
import org.apache.activemq.kaha.Store;
import org.apache.activemq.kaha.StoreFactory;
import org.apache.activemq.kaha.StringMarshaller;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import java.io.IOException;

public class KahaChannelTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldActFifo() throws IOException, InterruptedException {
        KahaChannel channel = new KahaChannel(folder.getRoot(), "fifo", new StringMessageCodec());
        channel.send(createMessage(1));
        channel.send(createMessage(2));
        channel.send(createMessage(3));
        assertThat((String) channel.receive().getPayload(), is("1"));
        assertThat((String) channel.receive().getPayload(), is("2"));
        assertThat((String) channel.receive().getPayload(), is("3"));
    }

    @Test
    public void shouldSurviveReconstruction() throws IOException, InterruptedException {
        Store store = StoreFactory.open(folder.getRoot(), "rw");
        KahaChannel channel = new KahaChannel(store, "reconstruction", new StringMessageCodec());
        channel.send(createMessage(1));
        channel.send(createMessage(2));
        channel.send(createMessage(3));
        store.close();
        store = StoreFactory.open(folder.getRoot(), "rw");
        channel = new KahaChannel(store, "reconstruction", new StringMessageCodec());
        assertThat((String) channel.receive().getPayload(), is("1"));
        assertThat((String) channel.receive().getPayload(), is("2"));
        assertThat((String) channel.receive().getPayload(), is("3"));
    }

    private Message<String> createMessage(int number) {
        return MessageBuilder.withPayload(Integer.toString(number)).build();
    }
    
}
