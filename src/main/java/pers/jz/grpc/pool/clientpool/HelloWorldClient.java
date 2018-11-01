package pers.jz.grpc.pool.clientpool;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pers.jz.grpc.helloworld.GreeterGrpc;
import pers.jz.grpc.helloworld.HelloReply;
import pers.jz.grpc.helloworld.HelloRequest;

import java.util.concurrent.TimeUnit;

/**
 * @author Jemmy Zhang on 2018/10/16.
 */
public class HelloWorldClient {

    private final ManagedChannel channel; //一个gRPC信道

    //初始化信道和存根
    public HelloWorldClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    private HelloWorldClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    //客户端方法
    public void greet(String name) {
        GreeterGrpc.GreeterBlockingStub blockingStub=GreeterGrpc.newBlockingStub(channel);
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            System.out.println("RPC调用失败:" + e.getMessage());
            return;
        }
        System.out.println("服务器返回信息:" + response.getMessage());
    }

    public static void main(String[] args) throws Exception {
        HelloWorldClient client = new HelloWorldClient("127.0.0.1", 50051);
        client.greet("HelloWorld");
        client.shutdown();
    }
}
