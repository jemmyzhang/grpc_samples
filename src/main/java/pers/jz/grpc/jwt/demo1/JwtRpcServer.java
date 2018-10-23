package pers.jz.grpc.jwt.demo1;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import pers.jz.grpc.helloworld.GreeterGrpc;
import pers.jz.grpc.helloworld.HelloReply;
import pers.jz.grpc.helloworld.HelloRequest;
import pers.jz.grpc.jwt.JwtTools;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Jemmy Zhang on 2018/10/16.
 */
public class JwtRpcServer {

    private int port = 50051;
    private Server server;

    private void start() throws IOException{
        ServerServiceDefinition intercept = ServerInterceptors.intercept(new GreeterImpl(), new JwtServerInterceptor(JwtTools.JWT_SECRET));
        server = ServerBuilder.forPort(port).addService(intercept).build().start();
        System.out.println("RPC Server开启...");

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("正在关闭RPC Server");
                JwtRpcServer.this.stop();
                System.out.println("RPC Server已经关闭");
            }
        });

    }

    private void stop(){
        if(Objects.nonNull(server)){
            server.shutdown();
        }
    }

    // block 一直到退出程序
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        final JwtRpcServer server = new JwtRpcServer();
        server.start();
        server.blockUntilShutdown();
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            System.out.println("收到的信息:"+request.getName());
            HelloReply reply = HelloReply.newBuilder().setMessage(("Hello: " + request.getName())).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
