package pers.jz.grpc.basic;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pers.jz.grpc.helloworld.GreeterGrpc;
import pers.jz.grpc.helloworld.HelloReply;
import pers.jz.grpc.helloworld.HelloRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Jemmy Zhang on 2018/10/16.
 */
public class HelloWorldClient {

    private final ManagedChannel channel; //一个gRPC信道
    private final GreeterGrpc.GreeterBlockingStub blockingStub;//阻塞/同步 存根
    private final String clientName;

    //初始化信道和存根
    public HelloWorldClient(String host, int port, String clientName) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true), clientName);
    }

    private HelloWorldClient(ManagedChannelBuilder<?> channelBuilder, String clientName) {
        channel = channelBuilder.build();
        blockingStub = GreeterGrpc.newBlockingStub(channel);
        this.clientName = clientName;
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    //客户端方法
    public void greet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            System.out.println("RPC调用失败:" + e.getMessage());
            return;
        }
        System.out.println(getClientName()+" 服务器返回信息:" + response.getMessage());
    }

    public static void greet(List<HelloWorldClient> clients, String clientName) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        for (int i = 0; i < clients.size(); i++) {
            final int k = i;
            executorService.execute(() -> {
                try {
                    clients.get(k).greet(clientName);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        clients.get(k).shutdown();
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    public static void main(String[] args) throws Exception {
        List<HelloWorldClient> clients = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            HelloWorldClient client = new HelloWorldClient("127.0.0.1", 50051, "Client: " + i);
            clients.add(client);
        }
        HelloWorldClient.greet(clients, "HelloWorld");
    }

    public String getClientName() {
        return clientName;
    }
}
