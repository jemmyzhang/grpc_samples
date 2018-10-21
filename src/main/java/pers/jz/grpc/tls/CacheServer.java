package pers.jz.grpc.tls;

import com.google.protobuf.ByteString;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import pers.jz.grpc.cacheService.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

/**
 * @author Jemmy Zhang on 2018/10/20.
 */
public class CacheServer {
    Server server;

    final String PRIVATE_KEY_FILE_PATH = getClass().getClassLoader().getResource("ssl/oepnserver_key_pkcs8.pem").getPath();
    final String CERT_CHAIN__FILE_PATH = getClass().getClassLoader().getResource("ssl/openserver.pem").getPath();
    final String TRUST_CERT_COLLECTION_FILE_PATH = getClass().getClassLoader().getResource("ssl/trustCertCollectionFilePath.pem").getPath();

    {
        System.setProperty("javax.net.ssl.trustStore", PRIVATE_KEY_FILE_PATH);
    }

    public CacheServer(int port) throws Exception {
        sslContext();
        server = NettyServerBuilder.forPort(port).sslContext(sslContext()).addService(new CacheServiceImpl()).build().start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("*** shutting down gRPC server since JVM is shutting down.***");
                CacheServer.this.stop();
                System.out.println("*** server shutdown.");
            }
        });
    }

    private SslContext sslContext() throws Exception {
        SslContextBuilder sslContextBuilder = SslContextBuilder
                .forServer(new FileInputStream(CERT_CHAIN__FILE_PATH), new FileInputStream(PRIVATE_KEY_FILE_PATH));
        File file = new File(TRUST_CERT_COLLECTION_FILE_PATH);
        if (file.exists()) {
            sslContextBuilder.trustManager(file);
            sslContextBuilder.clientAuth(ClientAuth.NONE);
        }
        return sslContextBuilder.build();
    }


    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void stop() {
        if (Objects.nonNull(server)) {
            server.shutdown();
        }
    }

    private class CacheServiceImpl extends CacheGrpc.CacheImplBase {

        @Override
        public void get(GetReq request, StreamObserver<GetResp> responseObserver) {
            super.get(request, responseObserver);
        }

        @Override
        public void store(StoreReq request, StreamObserver<StoreResp> responseObserver) {
            String key = request.getKey();
            ByteString val = request.getVal();
            String log = String.format("Receive from client: key {%s}, value {%s}", key, val.toStringUtf8());
            System.out.println(log);
            responseObserver.onNext(StoreResp.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws Exception {
        CacheServer server = new CacheServer(50010);
        //server.blockUntilShutdown();

    }
}
