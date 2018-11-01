package pers.jz.grpc.pool.clientpool;

/**
 * @author Jemmy Zhang on 2018/11/1.
 */
public class ClientTest {
    public static void main(String[] args) throws Exception{
        for (int i = 0; i < 10000; i++) {
            new Thread(HelloWorldClientPool.execute(client -> {
                client.greet("Hello!");
            })).start();
        }
    }

}
