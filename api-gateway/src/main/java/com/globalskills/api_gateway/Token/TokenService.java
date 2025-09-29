package com.globalskills.api_gateway.Token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class TokenService {

    public final String KEY_SECRET="c4a7e5bc8d9a53b249f9e7a3eb8d44f4c281cfe6b327f0b2f86f5c9a7e3408d1";
    private SecretKey getSignKey(){
        byte[] keyBytes = Decoders.BASE64.decode(KEY_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long getUserIDByToken(String token){
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String idString = claims.getSubject().trim();
        return Long.parseLong(idString);
    }
}
