package pers.jz.grpc.jwt;

import io.grpc.*;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

/**
 * Created by JemmyZhang on 2018/10/23
 */
public class JwtClientInterceptor implements ClientInterceptor {

    public static final Metadata.Key<String> JWT_METADATA_KEY = Metadata.Key.of("Token", ASCII_STRING_MARSHALLER);
    public static final Context.Key<String> JWT_CTX_KEY = Context.key("jwt");

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(JWT_METADATA_KEY, JWT_CTX_KEY.get());
                super.start(responseListener, headers);
            }
        };
    }
}
