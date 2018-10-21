package pers.jz.grpc.errorhandling;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Jemmy Zhang on 2018/10/16.
 */
public class ErrorHandleServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        UnknownStatusDescriptionInterceptor unknownStatusDescriptionInterceptor = new UnknownStatusDescriptionInterceptor(Arrays.asList(
                IllegalArgumentException.class
        ));
        Server server = ServerBuilder.forPort(8080)
                .addService(ServerInterceptors.intercept(new ErrorServiceImpl(), unknownStatusDescriptionInterceptor))
                .build();
        System.out.println("Starting server...");
        server.start();
        System.out.println("Server started!");
        server.awaitTermination();
    }


}
