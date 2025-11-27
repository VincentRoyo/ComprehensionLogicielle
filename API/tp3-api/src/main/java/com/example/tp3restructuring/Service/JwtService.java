package com.example.tp3restructuring.Service;

import com.example.tp3restructuring.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey SECRET_KEY = new SecretKeySpec(
            "79f9e470ccf3e4b648f013026f55c394036f14a385bf59547892f25fc23cb33a".getBytes(),
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
