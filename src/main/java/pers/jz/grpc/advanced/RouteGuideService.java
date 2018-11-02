package pers.jz.grpc.advanced;

import io.grpc.examples.routeguide.*;
import io.grpc.stub.StreamObserver;

import java.util.Objects;

import static java.lang.Math.abs;

/**
 * @author Jemmy Zhang on 2018/11/2.
 */
public class RouteGuideService extends RouteGuideGrpc.RouteGuideImplBase {

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
