package com.example.board_service.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    private final Key key;
    private final long accessTokenValidityMs;

    public String getEmail(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject(); // email = subject
    }

    public String getRole(String token) {
        Claims claims = parseClaims(token);
        return claims.get("roles", String.class);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (!StringUtils.hasText(bearer) || !bearer.startsWith("Bearer ")) {
            return null;
        }
        return bearer.substring(7);
    }


    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms:3600000}") long accessTokenValidityMs
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityMs = accessTokenValidityMs;
    }

    public String generateAccessToken(Long userId, String email, String roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMs);

        return Jwts.builder()
                .setSubject(email) // 주체 = 이메일
                .claim("userId", userId)
                .claim("roles", roles) // "ROLE_USER,ROLE_ADMIN"
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰 위조 / 만료 / 형식 오류 등
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * JWT에서 Authentication 객체 만들어서 SecurityContext에 넣을 때 사용
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String email = claims.getSubject();
        String roles = claims.get("roles", String.class);
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // password는 필요 없어서 빈 문자열
        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User(email, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return claims.get("userId", Long.class);
    }
}
