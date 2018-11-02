package pers.jz.grpc.advanced.demo1;

import com.google.common.util.concurrent.SettableFuture;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.routeguide.*;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Jemmy Zhang on 2018/10/17.
 */
public class RouteGuideClient {
    private final ManagedChannel channel;
    private final RouteGuideGrpc.RouteGuideStub asyncStub;
    private final RouteGuideGrpc.RouteGuideBlockingStub blockingStub;
    private final RouteGuideGrpc.RouteGuideFutureStub featureStub;

    public RouteGuideClient(String host, int port) {
        this.channel = NettyChannelBuilder.forAddress(host, port)
                .negotiationType(NegotiationType.PLAINTEXT)
                .build();
        this.asyncStub = RouteGuideGrpc.newStub(channel);
        this.blockingStub = RouteGuideGrpc.newBlockingStub(channel);
        this.featureStub = RouteGuideGrpc.newFutureStub(channel);
    }

    /**
     * 关闭信道
     *
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }


    public void blockSend() {
        Point point = Point.newBuilder().setLatitude(123).setLongitude(234).build();
        Feature response;
        try {
            response = blockingStub.getFeature(point);
        } catch (StatusRuntimeException e) {
            System.out.println("RPC调用失败:" + e.getMessage());
            return;
        }
        System.out.println("服务器返回信息:\n" + response.getName() + response.getLocation());
    }

    public void serverStreamSend() {
        Rectangle request =
                Rectangle.newBuilder()
                        .setLo(Point.newBuilder().setLatitude(11).setLongitude(22).build())
                        .setHi(Point.newBuilder().setLatitude(33).setLongitude(44).build())
                        .build();
        Iterator<Feature> features = blockingStub.listFeatures(request);
        while (features.hasNext()) {
            Feature item = features.next();
            System.out.println(item.getName() + "\n" + item.getLocation());
        }
    }

    public void clientStreamSend() throws Exception {
        final SettableFuture<Void> finishFuture = SettableFuture.create();
        StreamObserver<RouteSummary> responseObserver = new StreamObserver<RouteSummary>() {
            @Override
            public void onNext(RouteSummary summary) {
                String format = String.format("Finished trip with %d points. Passed %d features. "
                                + "Travelled %d meters. It took %d seconds.", summary.getPointCount(),
                        summary.getFeatureCount(), summary.getDistance(), summary.getElapsedTime());
                System.out.println(format);
            }

            @Override
            public void onError(Throwable throwable) {
                finishFuture.setException(throwable);
            }

            @Override
            public void onCompleted() {
                finishFuture.set(null);
            }
        };

        StreamObserver<Point> requestObserver = asyncStub.recordRoute(responseObserver);

        try {
            Random random = new Random(1000L);
            Point p1 = Point.newBuilder().setLatitude(random.nextInt()).setLongitude(random.nextInt()).build();
            Point p2 = Point.newBuilder().setLatitude(random.nextInt()).setLongitude(random.nextInt()).build();
            requestObserver.onNext(p1);
            if (finishFuture.isDone()) {
                return;
            }
            Thread.sleep(2000L);
            requestObserver.onNext(p2);
            requestObserver.onCompleted();
            finishFuture.get();
        } catch (Exception e) {
            requestObserver.onError(e);
            throw e;
        }
    }

    public void dualStreamSend() throws Exception {
        final SettableFuture<Void> finishFuture = SettableFuture.create();
        StreamObserver<RouteNote> responseObserver = new StreamObserver<RouteNote>() {
            @Override
            public void onNext(RouteNote routeNote) {
                System.out.println("Receive:" + routeNote.getMessage() +"\n" + routeNote.getLocation());
            }

            @Override
            public void onError(Throwable throwable) {
                finishFuture.setException(throwable);
            }

            @Override
            public void onCompleted() {
                finishFuture.set(null);
            }
        };

        StreamObserver<RouteNote> requestObserver = asyncStub.routeChat(responseObserver);

        try {
            Random random = new Random(1000L);
            for (int i = 0; i < 10; i++) {
                Point location = Point.newBuilder().setLongitude(random.nextInt()).setLatitude(random.nextInt()).build();
                RouteNote routeNote = RouteNote.newBuilder().setLocation(location).setMessage(String.valueOf(i)).build();
                requestObserver.onNext(routeNote);
                if (finishFuture.isDone()) {
                    break;
                }
            }
            requestObserver.onCompleted();
            finishFuture.get();
            System.out.println("finish route chat!");
        } catch (Exception e) {
            requestObserver.onError(e);
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        RouteGuideClient client = new RouteGuideClient("127.0.0.1", 50052);
//        client.blockSend();
//        client.serverStreamSend();
//        client.clientStreamSend();
        client.dualStreamSend();
        client.shutdown();
    }

}
