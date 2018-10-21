package pers.jz.grpc.tls;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import pers.jz.grpc.cacheService.CacheGrpc;
import pers.jz.grpc.cacheService.StoreReq;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Jemmy Zhang on 2018/10/20.
 */
public class CacheClient {
    private ManagedChannel channel;
    private CacheGrpc.CacheBlockingStub blockingStub;

    final String TRUSTED_CERT_COLLECTION_PATH = getClass().getClassLoader().getResource("ssl/openserver.pem").getPath();
    final String CLIENT_CERT_PATH = getClass().getClassLoader().getResource("ssl/oepnserver_key_pkcs8.pem").getPath();
    final String CLIENT_PK_PATH = getClass().getClassLoader().getResource("ssl/trustCertCollectionFilePath.pem").getPath();

    public SslContext buildSslContext() throws Exception {
        SslContextBuilder builder = GrpcSslContexts.forClient();
        builder.trustManager(new File(TRUSTED_CERT_COLLECTION_PATH));
        builder.keyManager(new File(CLIENT_CERT_PATH), new File(CLIENT_PK_PATH));
        return builder.build();
    }

    public CacheClient(String hostName, int port) {
        channel = NettyChannelBuilder.forAddress(hostName, port).usePlaintext(true).build();
        blockingStub = CacheGrpc.newBlockingStub(channel);
    }

    public void store() {
        StoreReq req = StoreReq.newBuilder().setKey("MyKey").setVal(ByteString.copyFromUtf8("HelloWorld")).build();
        blockingStub.store(req);
        System.out.println("End client store call.");
    }

    public void get() {

    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        CacheClient client = new CacheClient("127.0.0.1", 50010);
        client.store();
        client.shutdown();
    }
}
