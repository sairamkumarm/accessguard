package JWKS;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class JWKSUtil {

    private static String toBase64URL(BigInteger value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray());
    }

    public static Map<String, Object> createJWK(String kid, RSAPublicKey rsaPublicKey) {
        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kid", kid);
        jwk.put("kty", "RSA");
        jwk.put("alg", "RS256");
        jwk.put("use", "sig");
        jwk.put("n", toBase64URL(rsaPublicKey.getModulus()));
        jwk.put("e", toBase64URL(rsaPublicKey.getPublicExponent()));
        return jwk;
    }

}


