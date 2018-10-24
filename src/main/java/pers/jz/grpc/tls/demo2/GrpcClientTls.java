package pers.jz.grpc.tls.demo2;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import pers.jz.grpc.helloworld.GreeterGrpc;
import pers.jz.grpc.helloworld.HelloReply;
import pers.jz.grpc.helloworld.HelloRequest;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Jemmy Zhang on 2018/10/16.
 */
public class GrpcClientTls {

    private final ManagedChannel channel; //一个gRPC信道
    private final GreeterGrpc.GreeterBlockingStub blockingStub;//阻塞/同步 存根

    private static final String ca = GrpcServerTls.class.getClassLoader().getResource("ssl/ca.crt").getPath();

    //初始化信道和存根
    public GrpcClientTls(String host, int port) throws SSLException {
        this(NettyChannelBuilder.forAddress(host, port)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File(ca)).build()));
    }

    private GrpcClientTls(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = GreeterGrpc.newBlockingStub(channel);
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
        System.out.println("服务器返回信息:" + response.getMessage());
    }

    public static void main(String[] args) throws Exception {
        GrpcClientTls client = new GrpcClientTls("localhost", 8443);
        client.greet("HelloWorld");
        client.shutdown();
    }
}
