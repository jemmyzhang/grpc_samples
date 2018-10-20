package pers.jz.grpc.advanced;

import com.google.protobuf.ByteString;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import pers.jz.grpc.cacheService.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * @author Jemmy Zhang on 2018/10/20.
 */
public class CacheServer {
    Server server;

    final String SERVER_KEY_PATH = getClass().getClassLoader().getResource("ssl/kserver.keystore").getPath();
    final String SERVER_PASSWORD = "123456";

    {
        System.setProperty("javax.net.ssl.trustStore", SERVER_KEY_PATH);
    }

    public CacheServer(int port) throws Exception{
        sslContext();
        server = NettyServerBuilder.forPort(port).addService(new CacheServiceImpl()).build().start();
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
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(SERVER_KEY_PATH), "111111".toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore,"111111".toCharArray());
        SslContext sslContext = SslContextBuilder.forServer(keyManagerFactory).build();
        System.out.println(sslContext.isServer());
        return null;
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
