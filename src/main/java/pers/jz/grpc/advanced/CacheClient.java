package pers.jz.grpc.advanced;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import pers.jz.grpc.cacheService.CacheGrpc;
import pers.jz.grpc.cacheService.StoreReq;

import java.util.concurrent.TimeUnit;

/**
 * @author Jemmy Zhang on 2018/10/20.
 */
public class CacheClient {
    private ManagedChannel channel;
    private CacheGrpc.CacheBlockingStub blockingStub;

    public CacheClient(String hostName, int port) {
        channel = NettyChannelBuilder.forAddress(hostName, port).usePlaintext(true).build();
        blockingStub = CacheGrpc.newBlockingStub(channel);
    }

    public void store(){
        StoreReq req=StoreReq.newBuilder().setKey("MyKey").setVal(ByteString.copyFromUtf8("HelloWorld")).build();
        blockingStub.store(req);
        System.out.println("End client store call.");
    }

    public void get(){

    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception{
        CacheClient client=new CacheClient("127.0.0.1",50010);
        client.store();
        client.shutdown();
    }
}
