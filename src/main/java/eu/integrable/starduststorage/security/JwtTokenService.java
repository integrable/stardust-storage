package eu.integrable.starduststorage.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import eu.integrable.starduststorage.property.CredentialsProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    @Autowired
    private CredentialsProperty credentialsProperty;

    public DecodedJWT getDecodedJwt(String token) throws JWTVerificationException {

        // Get secret
        String secret = credentialsProperty.getJwtSecret();

        // Build JWT verifier
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret.getBytes())).build();

        // Return decoded token
        return jwtVerifier.verify(token);
    }
}
