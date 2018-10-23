package pers.jz.grpc.jwt.demo1;

import io.grpc.*;

import java.util.concurrent.Executor;

/**
 * Created by JemmyZhang on 2018/10/23
 */
public class JwtCallCredential implements CallCredentials {

    private final String jwt;

    public JwtCallCredential(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public void applyRequestMetadata(
            MethodDescriptor<?, ?> method,
            Attributes attrs,
            Executor appExecutor,
            MetadataApplier applier) {

        try {
            Metadata header = new Metadata();
            Metadata.Key<String> jwtKey = Metadata.Key.of("Token", Metadata.ASCII_STRING_MARSHALLER);
            header.put(jwtKey, jwt);
            applier.apply(header);
        } catch (Throwable e) {
            applier.fail(Status.UNAUTHENTICATED.withCause(e));
        }


    }

}
