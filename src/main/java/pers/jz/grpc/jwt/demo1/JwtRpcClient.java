package pers.jz.grpc.jwt.demo1;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pers.jz.grpc.helloworld.GreeterGrpc;
import pers.jz.grpc.helloworld.HelloReply;
import pers.jz.grpc.helloworld.HelloRequest;
import pers.jz.grpc.jwt.JwtTools;

import java.util.concurrent.TimeUnit;

/**
 * @author Jemmy Zhang on 2018/10/16.
 */
public class JwtRpcClient {

    private final ManagedChannel channel; //一个gRPC信道
    private final GreeterGrpc.GreeterBlockingStub blockingStub;//阻塞/同步 存根

    public JwtRpcClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    private JwtRpcClient(ManagedChannelBuilder<?> channelBuilder) {
        JwtCallCredential jwtCallCredential = buildJwtCallCredential();
        channel = channelBuilder.build();
        blockingStub = GreeterGrpc.newBlockingStub(channel).withCallCredentials(jwtCallCredential);
    }

    /**
     * 建立一个
     * @return
     */
    private JwtCallCredential buildJwtCallCredential() {
        String jwt = JwtTools.createJwt(JwtTools.JWT_SECRET, "admin", "password");
        return new JwtCallCredential(jwt);
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
        JwtRpcClient client = new JwtRpcClient("127.0.0.1", 50051);
        client.greet("HelloWorld");
        client.shutdown();
    }
}
