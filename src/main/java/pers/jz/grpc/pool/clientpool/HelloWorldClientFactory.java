package pers.jz.grpc.pool.clientpool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author Jemmy Zhang on 2018/11/1.
 */
public class HelloWorldClientFactory extends BasePooledObjectFactory<HelloWorldClient> {

    private String host = "localhost";
    private int port = 50051;

    @Override
    public HelloWorldClient create() throws Exception {
        return new HelloWorldClient(host, port);
    }

    @Override
    public PooledObject<HelloWorldClient> wrap(HelloWorldClient obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public void destroyObject(PooledObject<HelloWorldClient> p) throws Exception {
        p.getObject().shutdown();
        super.destroyObject(p);
    }
}
