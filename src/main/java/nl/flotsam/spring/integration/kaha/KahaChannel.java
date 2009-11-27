package nl.flotsam.spring.integration.kaha;

import com.agilejava.blammo.BlammoLoggerFactory;
import org.apache.activemq.kaha.ListContainer;
import org.apache.activemq.kaha.Store;
import org.apache.activemq.kaha.StoreFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.integration.channel.AbstractPollableChannel;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.integration.selector.MessageSelector;
import org.springframework.util.Assert;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class KahaChannel<T> extends AbstractPollableChannel {

    private final ListContainer<Message<T>> container;
    private final Semaphore available;
    private static Logger logger = (Logger) BlammoLoggerFactory.create(Logger.class);

    public KahaChannel(File directory, String id, MessageCodec<T> codec) throws IOException {
        this(openStore(id, directory), id, codec);
    }

    public KahaChannel(Store store, String id, MessageCodec<T> codec) throws IOException {
        assert store != null;
        assert id != null;
        assert codec != null;
        container = store.getListContainer(id);
        container.setMarshaller(new MessageCodecAdapter(codec));
        available = new Semaphore(container.size());
    }

    private static Store openStore(String id, File directory) throws IOException {
        assert id != null;
        assert directory != null;
        logger.logConfiguration(id, directory);
        return StoreFactory.open(directory, "rw");
    }

    @Override
    protected Message<?> doReceive(long timeout) {
        return doSafeReceive(timeout);
    }

    private Message<T> doSafeReceive(long timeout) {
        try {
            if (timeout > 0) {
                if (available.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                    return container.removeFirst();
                } else {
                    return null;
                }
            } else if (timeout < 0) {
                available.acquire();
                return container.removeFirst();
            } else {
                if (available.tryAcquire()) {
                    return container.removeFirst();
                } else {
                    return null;
                }
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    protected boolean doSend(Message<?> message, long timeout) {
        container.addLast((Message<T>) message);
        available.release();
        return true;
    }

    public List<Message<?>> clear() {
        throw new UnsupportedOperationException();
    }

    public List<Message<?>> purge(MessageSelector selector) {
        throw new UnsupportedOperationException();
    }

    /**
     * @blammo.logger
     */
    public interface Logger {

        /**
         * Logs the configuration details of the channel.
         *
         * @param id Id of the channel.
         * @param directory The directory storing the files.
         * @blammo.level info
         * @blammo.message "Created Kaha Channel {id}, from directory {directory}"
         */
        void logConfiguration(String id, File directory);

    }

}
