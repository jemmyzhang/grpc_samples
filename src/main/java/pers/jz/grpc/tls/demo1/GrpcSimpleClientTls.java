package pers.jz.grpc.tls.demo1;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import pers.jz.grpc.helloworld.GreeterGrpc;
import pers.jz.grpc.helloworld.HelloReply;
import pers.jz.grpc.helloworld.HelloRequest;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Jemmy Zhang on 2018/10/16.
 */
public class GrpcSimpleClientTls {

    private final ManagedChannel channel; //一个gRPC信道
    private final GreeterGrpc.GreeterBlockingStub blockingStub;//阻塞/同步 存根

    //初始化信道和存根
    public GrpcSimpleClientTls(String host, int port) throws SSLException {
        this(NettyChannelBuilder.forAddress(host, port).sslContext(
                GrpcSslContexts.forClient().
                        ciphers(Http2SecurityUtil.CIPHERS,
                                SupportedCipherSuiteFilter.INSTANCE).
                        trustManager(InsecureTrustManagerFactory.INSTANCE).build()));
    }

    private GrpcSimpleClientTls(ManagedChannelBuilder<?> channelBuilder) {
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
        GrpcSimpleClientTls client = new GrpcSimpleClientTls("localhost", 8443);
        client.greet("HelloWorld");
        client.shutdown();
    }
}
