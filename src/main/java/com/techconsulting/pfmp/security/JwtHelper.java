package com.techconsulting.pfmp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
@Slf4j
public class JwtHelper {

    @Value("${pfmp.app.jwtSecret}")
    private String jwtSecret;
    @Value("${pfmp.app.jwtIssuer}")
    private String jwtIssuer;
    @Value("${pfmp.app.jwtType}")
    private String jwtType;
    @Value("${pfmp.app.jwtAudience}")
    private String jwtAudience;

    public String generateJwtToken(UserDetails userDetails) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(60L, ChronoUnit.MINUTES)))
                .issuer(jwtIssuer)
                .audience().add(jwtAudience).and()
//                .compressWith(Jwts.ZIP.GZIP)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public Boolean validateJwtToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String getUsernameFromToken(String token) {
        log.debug("Extracting username from JWT token");
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        log.debug("Extracting expiration date from JWT token");
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        log.debug("Checking if JWT token is expired");
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        log.debug("Extracting claim from JWT token");
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .requireIssuer(jwtIssuer)
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            log.error("JWT Token expired: {}", ex.getMessage());
            throw new RuntimeException("Token expired. Please log in again.");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT Token: {}", ex.getMessage());
            throw new RuntimeException("Invalid token.");
        } catch (SignatureException ex) {
            log.error("JWT signature validation failed: {}", ex.getMessage());
            throw new RuntimeException("Invalid token signature.");
        } catch (Exception ex) {
            log.error("JWT processing error: {}", ex.getMessage());
            throw new RuntimeException("Token processing error.");
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret key is too weak. Use at least a 256-bit key.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
