package pers.jz.grpc.jwt.demo1;

import com.auth0.jwt.JWTVerifier;
import io.grpc.*;

import java.util.Map;
import java.util.Objects;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

/**
 * 拦截器，用于拦截客户端的请求，在请求到达之前解析报文头中的元数据，拿出jwt token进行校验。
 * Created by JemmyZhang on 2018/10/23
 */
public class JwtServerInterceptor implements ServerInterceptor {
    private static final ServerCall.Listener NO_OPERATION_LISTENER = new ServerCall.Listener() {
    };

    public static final Metadata.Key<String> TOKEN = Metadata.Key.of("Token", ASCII_STRING_MARSHALLER);
    public static final Context.Key<String> USER_ID_KEY = Context.key("userId");
    public static final Context.Key<String> JWT_KEY = Context.key("jwt");

    private final String secret; //密钥
    private final JWTVerifier jwtVerifier; //JWT解析器。

    public JwtServerInterceptor(String secret) {
        this.secret = secret;
        this.jwtVerifier = new JWTVerifier(secret);
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String jwt = headers.get(TOKEN);
        if (Objects.isNull(jwt)) {
            call.close(Status.UNAUTHENTICATED.withDescription("服务器没有找到合法的JWT令牌，禁止访问。"), headers);
            return NO_OPERATION_LISTENER;
        }
        Context context;
        try {
            Map<String, Object> verified = jwtVerifier.verify(jwt);
            context = Context.current()
                    .withValue(USER_ID_KEY, verified.getOrDefault("sub", "anonymous").toString())
                    .withValue(JWT_KEY, jwt);

        } catch (Exception e) {
            System.out.println("验证失败，未授权！");
            call.close(Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e), headers);
            return NO_OPERATION_LISTENER;
        }
        return Contexts.interceptCall(context, call, headers, next);
    }
}
