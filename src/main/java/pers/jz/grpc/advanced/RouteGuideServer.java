package pers.jz.grpc.advanced;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.routeguide.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Objects;

import static java.lang.Math.abs;

/**
 * @author Jemmy Zhang on 2018/10/17.
 */
public class RouteGuideServer {

    private int port = 50052;
    private Server server;

    private void start() throws IOException {
        server = ServerBuilder.forPort(port).addService(new RouteGuideServer.RouteGuideService()).build().start();
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


    private static class RouteGuideService extends RouteGuideGrpc.RouteGuideImplBase {

        /**
         * 简单RPC
         *
         * @param request
         * @param responseObserver
         */
        @Override
        public void getFeature(Point request, StreamObserver<Feature> responseObserver) {
            Feature feature = checkFeature(request);
            responseObserver.onNext(feature);
            responseObserver.onCompleted();
        }

        private Feature checkFeature(Point location) {
            System.out.println("构造了新的Feature元素。");
            return Feature.newBuilder().setName("").setLocation(location).build();
        }

        /**
         * 服务端流式RPC
         *
         * @param request
         * @param responseObserver
         */
        @Override
        public void listFeatures(Rectangle request, StreamObserver<Feature> responseObserver) {
            Point lo = Point.newBuilder(request.getLo()).build();
            Point hi = Point.newBuilder(request.getHi()).build();
            responseObserver.onNext(Feature.newBuilder().setName("lo").setLocation(lo).build());
            responseObserver.onNext(Feature.newBuilder().setName("hi").setLocation(hi).build());
            responseObserver.onCompleted();
        }

        /**
         * 客户端流式RPC
         *
         * @param responseObserver
         * @return
         */
        @Override
        public StreamObserver<Point> recordRoute(StreamObserver<RouteSummary> responseObserver) {
            return new StreamObserver<Point>() {
                int pointCount;
                int featureCount;
                int distance;
                Point previous;
                long startTime = System.nanoTime();

                @Override
                public void onNext(Point point) {
                    pointCount++;
                    if (point.getLatitude() % 2 == 0)
                        featureCount++;
                    if (Objects.nonNull(previous)) {
                        distance = abs((point.getLatitude() - previous.getLatitude()) + (point.getLongitude() - previous.getLongitude()));
                    }
                    previous = point;
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("Error occurs: =============>" + throwable.getMessage());
                }

                @Override
                public void onCompleted() {
                    int seconds = (int) ((System.nanoTime() - startTime) / 1000);
                    responseObserver
                            .onNext(RouteSummary.newBuilder()
                                    .setPointCount(pointCount)
                                    .setFeatureCount(featureCount)
                                    .setElapsedTime(seconds)
                                    .setDistance(distance).build());
                    responseObserver.onCompleted();
                }
            };
        }


        @Override
        public StreamObserver<RouteNote> routeChat(StreamObserver<RouteNote> responseObserver) {
            return new StreamObserver<RouteNote>() {
                @Override
                public void onNext(RouteNote routeNote) {
                    System.out.println("收到来自客户端的消息：" + routeNote);
                    responseObserver.onNext(routeNote);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("Error occurs: =============>" + throwable.getMessage());
                    responseObserver.onError(throwable);
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
