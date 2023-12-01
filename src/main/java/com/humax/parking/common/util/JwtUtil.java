package com.humax.parking.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class JwtUtil {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;


    //@Value("${JWT_EXPIRATION_TIME}")
    private final Long expirationTime = 1000L * 60 * 120; // 2h

    public String createToken(Long id) {
//        Map<String, String> claims = new HashMap<>();
//        claims.put("id", String.valueOf(id));
        //claims.put("loginType", loginType.name());

        //SecretKey secretKey = createSecretKey();
        // 코드 리팩토링
        return Jwts.builder()
                .claim("id",String.valueOf(id))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey.getBytes())
                .compact();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).get("id", String.class));
    }

//    public LoginType getLoginType(String token) {
//        return LoginType.valueOf(getClaims(token).get("loginType", String.class));
//    }

    //Token에 담긴 claim을 반환하는 Method이다.
    private Claims getClaims(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecretKey.getBytes())
                .parseClaimsJws(token).getBody();
        return claims;
    }

//    private SecretKey createSecretKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
}
