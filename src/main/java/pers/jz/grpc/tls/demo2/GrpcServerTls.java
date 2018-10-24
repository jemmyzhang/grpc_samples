package pers.jz.grpc.tls.demo2;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import pers.jz.grpc.helloworld.GreeterGrpc;
import pers.jz.grpc.helloworld.HelloReply;
import pers.jz.grpc.helloworld.HelloRequest;

import java.io.File;
import java.util.Objects;

/**
 * @author Jemmy Zhang on 2018/10/16.
 */
public class GrpcServerTls {

    private int port = 8443;
    private Server server;

    private static final String certChainFile = GrpcServerTls.class.getClassLoader().getResource("ssl/server.crt").getPath();
    private static final String privateKeyFile = GrpcServerTls.class.getClassLoader().getResource("ssl/server.pem").getPath();

    private void start() throws Exception {
        server = ServerBuilder.forPort(port)
                .useTransportSecurity(new File(certChainFile), new File(privateKeyFile))
                .addService(new GreeterImpl()).build().start();
        System.out.println("service start...");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("*** shutting down gRPC server since JVM is shutting down.***");
                GrpcServerTls.this.stop();
                System.out.println("*** server shutdown.");
            }
        });

    }

    private void stop() {
        if (Objects.nonNull(server)) {
            server.shutdown();
        }
    }

    // block 一直到退出程序
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


    public static void main(String[] args) throws Exception {
        final GrpcServerTls server = new GrpcServerTls();
        server.start();
        server.blockUntilShutdown();
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            System.out.println("收到的信息:" + request.getName());
            HelloReply reply = HelloReply.newBuilder().setMessage(("Hello: " + request.getName())).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
