package nl.flotsam.spring.integration.kaha;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.annotation.DirtiesContext;

@RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:benchmark.xml")
public class BenchmarkTest {

    @Autowired
    @Qualifier("kaha-channel")
    private MessageChannel kahaChannel;

    @Autowired
    @Qualifier("regular-channel")
    private MessageChannel regularChannel;

    @Autowired
    private Logger logger;

    @Test
    @DirtiesContext
    public void runWithKaha() throws InterruptedException {
        run("kaha", kahaChannel, logger);
    }

    @Test
    @DirtiesContext
    public void runWithRegular() throws InterruptedException {
        run("regular", regularChannel, logger);
    }

    public void run(String id, MessageChannel channel, Logger logger) throws InterruptedException {
        for (int i = 0; i < 10000; i++) {
            Message<String> message = MessageBuilder.withPayload(Integer.toString(i)).build();
            kahaChannel.send(message);
        }
        System.out.println("Duration for " + id + ": " + logger.waitForCompletion());
    }

}
