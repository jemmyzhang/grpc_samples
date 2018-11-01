package pers.jz.grpc.pool.clientpool;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author Jemmy Zhang on 2018/11/1.
 */
public class HelloWorldClientPool {
    private static GenericObjectPool<HelloWorldClient> objectPool = null;

    static {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(8);
        config.setMinIdle(0);
        config.setMaxIdle(8);
        config.setMaxWaitMillis(-1);
        config.setLifo(true);
        config.setMinEvictableIdleTimeMillis(1000L * 60L * 30);
        config.setBlockWhenExhausted(true);
        objectPool = new GenericObjectPool<HelloWorldClient>(new HelloWorldClientFactory(), config);
    }

    public static void close(){
        objectPool.close();
    }

    private static HelloWorldClient borrowObject() throws Exception {
        try {
            HelloWorldClient client = objectPool.borrowObject();
            System.out.println("总创建线程数" + objectPool.getCreatedCount());
            return client;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static Runnable execute(WorkCallBack<HelloWorldClient> work) {
        return () -> {
            HelloWorldClient client = null;
            try {
                client = borrowObject();
                work.callback(client);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (client != null) {
                    try {
                        objectPool.returnObject(client);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}
