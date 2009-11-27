package nl.flotsam.spring.integration.kaha;

import org.apache.activemq.kaha.Marshaller;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

import java.io.File;

public class KahaChannelFactory<T> implements FactoryBean, BeanNameAware {

    private String name;
    private File directory;
    private MessageCodec<T> codec = new SimpleMessageCodec<T>();

    @Override
    public Object getObject() throws Exception {
        Assert.notNull(name, "Note that the bean name must be set.");
        return new KahaChannel<T>(directory,  name, codec);
    }

    @Override
    public Class getObjectType() {
        return KahaChannel.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public void setCodec(MessageCodec<T> codec) {
        this.codec = codec;
    }
}
