package pers.jz.grpc.advanced.demo1;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pers.jz.grpc.advanced.RouteGuideService;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Jemmy Zhang on 2018/10/17.
 */
public class RouteGuideServer {

    private int port = 50052;
    private Server server;

    private void start() throws IOException {
        server = ServerBuilder.forPort(port).addService(new RouteGuideService()).build().start();
        System.out.println("service start...");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("*** shutting down gRPC server since JVM is shutting down.***");
                RouteGuideServer.this.stop();
                System.out.println("*** server shutdown.");
            }
        });

    }

    private void stop() {
        if (Objects.nonNull(server)) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final RouteGuideServer server = new RouteGuideServer();
        server.start();
        server.blockUntilShutdown();
    }

}
