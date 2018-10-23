package pers.jz.grpc.jwt;

import com.auth0.jwt.JWTSigner;

import java.util.HashMap;

/**
 * Created by JemmyZhang on 2018/10/23
 */
public class JwtTools {

    public static final String JWT_SECRET = "ajsdklfsadf";
    public static String createJwt(String secret, String issuer, String subject) {
        final long issuedTime = System.currentTimeMillis() / 1000l; // 发行时间
        final long expireTime = issuedTime + 60L; // 过期时间

        final JWTSigner signer = new JWTSigner(secret);
        final HashMap<String, Object> claims = new HashMap<String, Object>();
        claims.put("iss", issuer);
        claims.put("exp", expireTime);
        claims.put("iat", issuedTime);
        claims.put("sub", subject);
        return signer.sign(claims);
    }
}
