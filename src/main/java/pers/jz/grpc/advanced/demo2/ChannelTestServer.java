package pers.jz.grpc.advanced.demo2;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import pers.jz.grpc.advanced.RouteGuideService;
import pers.jz.grpc.helloworld.GreeterGrpc;
import pers.jz.grpc.helloworld.HelloReply;
import pers.jz.grpc.helloworld.HelloRequest;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Jemmy Zhang on 2018/10/16.
 */
public class ChannelTestServer {

    private int port = 50051;
    private Server server;

    private void start() throws IOException{
        server = ServerBuilder.forPort(port).addService(new RouteGuideService()).build().start();
        System.out.println("service start...");

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("*** shutting down gRPC server since JVM is shutting down.***");
                ChannelTestServer.this.stop();
                System.out.println("*** server shutdown.");
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
        final ChannelTestServer server = new ChannelTestServer();
        server.start();
        server.blockUntilShutdown();
    }
}
