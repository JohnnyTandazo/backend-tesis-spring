package com.courrier.backend;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:tuSecretoSeguroDeDesarrollo123!@#}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 horas
    private long jwtExpiration;

    public String generarToken(String email, Long usuarioId, String rol) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(email)
                .claim("usuarioId", usuarioId)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extraerEmail(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public String extraerRol(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Object rol = Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("rol");
        return rol != null ? rol.toString() : null;
    }

    public boolean validarToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
