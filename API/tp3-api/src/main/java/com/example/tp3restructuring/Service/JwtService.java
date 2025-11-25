package com.example.tp3restructuration.Service;

import com.example.tp3restructuration.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey SECRET_KEY = new SecretKeySpec(
            "very-secret-key-change-me-32-bytes-min!".getBytes(),
            SignatureAlgorithm.HS256.getJcaName()
    );

    public String generateToken(User u) {
        return Jwts.builder()
                .setSubject(u.getId())
                .claim("email", u.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateAndExtractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }
}
