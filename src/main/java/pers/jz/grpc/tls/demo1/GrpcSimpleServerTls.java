package pers.jz.grpc.tls.demo1;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import pers.jz.grpc.helloworld.GreeterGrpc;
import pers.jz.grpc.helloworld.HelloReply;
import pers.jz.grpc.helloworld.HelloRequest;

import java.util.Objects;

/**
 * @author Jemmy Zhang on 2018/10/16.
 */
public class GrpcSimpleServerTls {

    private int port = 8443;
    private Server server;

    private void start() throws Exception {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        server = ServerBuilder.forPort(port)
                .useTransportSecurity(ssc.certificate(), ssc.privateKey())
                .addService(new GreeterImpl()).build().start();
        System.out.println("service start...");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("*** shutting down gRPC server since JVM is shutting down.***");
                GrpcSimpleServerTls.this.stop();
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
        final GrpcSimpleServerTls server = new GrpcSimpleServerTls();
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
