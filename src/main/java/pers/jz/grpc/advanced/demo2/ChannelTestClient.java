package pers.jz.grpc.advanced.demo2;

import com.google.common.util.concurrent.SettableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.routeguide.Feature;
import io.grpc.examples.routeguide.Point;
import io.grpc.examples.routeguide.RouteGuideGrpc;
import io.grpc.examples.routeguide.RouteNote;
import io.grpc.stub.StreamObserver;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 用来测试多个存根是否能通共用一个通道
 *
 * @author Jemmy Zhang on 2018/10/16.
 */
public class ChannelTestClient {

    private final ManagedChannel channel; //一个gRPC信道

    //初始化信道和存根
    public ChannelTestClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    private ChannelTestClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();

    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void sendBlock() throws Exception {

        while (true) {
            Thread.sleep(800L);
            RouteGuideGrpc.RouteGuideBlockingStub blockingStub = RouteGuideGrpc.newBlockingStub(channel);
            Point point = Point.newBuilder()
                    .setLatitude(123).setLongitude(234).build();
            Feature response = blockingStub.getFeature(point);
            System.out.println("阻塞接口返回：" + response.getName() + response.getLocation());
        }

    }

    public void sendDualStream() throws Exception {
        RouteGuideGrpc.RouteGuideStub asyncStub = RouteGuideGrpc.newStub(channel);
        final SettableFuture<Void> finishFuture = SettableFuture.create();
        StreamObserver<RouteNote> responseObserver = new StreamObserver<RouteNote>() {
            @Override
            public void onNext(RouteNote routeNote) {
                System.out.println("服务器流式接口返回：" + routeNote.getMessage() + "\n" + routeNote.getLocation());
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
            int counter = 0;
            while (true) {
                Thread.sleep(1000L);
                counter++;
                Point location = Point.newBuilder().setLongitude(random.nextInt()).setLatitude(random.nextInt()).build();
                RouteNote routeNote = RouteNote.newBuilder().setLocation(location).setMessage(String.valueOf(counter)).build();
                requestObserver.onNext(routeNote);
                if (finishFuture.isDone()) {
                    break;
                }
            }
            requestObserver.onCompleted();
            finishFuture.get();
            System.out.println("结束回话！");
        } catch (Exception e) {
            requestObserver.onError(e);
            throw e;
        }
    }

    public static void main(String[] args) {
            ChannelTestClient client = new ChannelTestClient("127.0.0.1", 50051);
            new Thread(() -> {
                try {
                    client.sendBlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try {
                    client.sendDualStream();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        //client.shutdown();
    }
}
