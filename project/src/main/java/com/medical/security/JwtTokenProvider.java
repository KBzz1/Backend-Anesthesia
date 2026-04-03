package com.medical.security;

import java.util.Date;

import javax.crypto.SecretKey;

import com.medical.pojo.auth.User;
import com.medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationDate;

    private SecretKey cachedKey;

    @Autowired(required = false)
    private UserRepository userRepository;

    @PostConstruct
    void init() {
        cachedKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateToken(String username) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(expireDate)
            .signWith(key())
            .compact();
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);
        
        // Try to get staff_id from user repository
        Long staffId = null;
        if (userRepository != null) {
            try {
                User user = userRepository.findByUsernameOrEmail(username, username).orElse(null);
                if (user != null) {
                    staffId = user.getStaffId();
                }
            } catch (Exception ignored) {
            }
        }
        
        var builder = Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(expireDate);
        
        if (staffId != null) {
            builder.claim("staff_id", staffId);
        }
        
        return builder.signWith(key()).compact();
    }

    public String getUsername(String token) {
        return Jwts.parser()
            .verifyWith(key())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(key())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean validateToken(String token) {
        Jwts.parser()
            .verifyWith(key())
            .build()
            .parse(token);
        return true;
    }

    public Long getStaffId(String token) {
        Claims claims = getClaims(token);

        Object raw = claims.get("staffId");
        if (raw == null) {
            raw = claims.get("staff_id"); // tolerate underscore naming
        }

        if (raw == null) return null;
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        try {
            return Long.parseLong(raw.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private SecretKey key() {
        return cachedKey;
    }
}
