package com.example.apigateway.common.jwt;

import com.example.apigateway.common.Role;
import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.entity.User;
import com.example.apigateway.entity.UserRole;
import com.example.apigateway.repository.UserRepository;
import com.example.apigateway.repository.redis.UserRefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {
    private final UserRepository userRepository;

    private final UserRefreshTokenRepository userRefreshTokenRepository;

    private final static String IDENTIFIER_KEY = "nickname";
    private final static String ROLE = "role";

    private final static String PREFIX = "Bearer ";

    @Value("${jwt.security.key}")
    private String secretKey;

    @Value("${jwt.access-token-validity-in-second}")
    private long accessTokenExpTime;

    @Value("${jwt.refresh-token-validity-in-second}")
    private long refreshTokenExpTime;

    private final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

    public Object getPrincipal(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException(CustomResponseException.INVALID_TOKEN)).getUserRoles();
    }

    public Set<Role> getRoles(Object principal) {
        User user = (User) principal;
        return user.getUserRoles().stream().map(UserRole::getRole).collect(Collectors.toSet());
    }

    public Role getUserRole(String token) {
        if(!validateAccessToken(token)) {
            throw new CustomException(CustomResponseException.INVALID_TOKEN);
        }

        Claims claims = parseClaims(token);

        return claims.get(ROLE) == null ? null : Role.findRole(claims.get(ROLE).toString());
    }

    public String generateAccessToken(String nickname, Set<Role> roles) {
        return generateToken(nickname, roles, accessTokenExpTime);
    }

    public String generateRefreshToken(String nickname, Set<Role> roles) {
        return generateToken(nickname, roles, refreshTokenExpTime);
    }

    public String getNickname(String token) {
        String formattedToken = token.replace(PREFIX, "");
        Claims claims = parseClaims(formattedToken);

        if (!claims.containsKey(IDENTIFIER_KEY)) {
            throw new CustomException(CustomResponseException.INVALID_TOKEN);
        }

        return claims.get(IDENTIFIER_KEY).toString();
    }

    public Role getRole(String token) {
        Claims claims = parseClaims(token);
        return Role.findRole(claims.get(ROLE).toString());
    }

    public String refreshAccessToken(String refreshToken) {
        if (!validateAccessToken(refreshToken)) {
            throw new CustomException(CustomResponseException.INVALID_REFRESH_TOKEN);
        }

        String nickname = parseClaims(refreshToken).get(IDENTIFIER_KEY).toString();

        if (userRefreshTokenRepository.getValues(nickname) == null) {
            throw new CustomException(CustomResponseException.NOT_REFRESH_TOKEN);
        }

        Object principal = getPrincipal(nickname);
        Set<Role> roles = getRoles(principal);

        return generateAccessToken(nickname, roles);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        if (!claims.containsKey(IDENTIFIER_KEY) || !claims.containsKey(ROLE)) {
            throw new CustomException(CustomResponseException.INVALID_TOKEN);
        }

        String nickname = claims.get(IDENTIFIER_KEY).toString();
        UserDetails principal = (UserDetails)getPrincipal(nickname);

        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info(CustomResponseException.INVALID_TOKEN.getMessage(), e);
        } catch (ExpiredJwtException e) {
            log.info(CustomResponseException.TOKEN_HAS_EXPIRED.getMessage(), e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    public String generateToken(String nickname, Set<Role> roles, long expireTime) {
        Claims claims = Jwts.claims()
                .add(IDENTIFIER_KEY, nickname)
                .add(ROLE, roles)
                .build();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime tokenValidity = now.plusSeconds(expireTime);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(tokenValidity.toInstant()))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public String getExpireTime(String token) {
        Date expirationDate = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token.replace(PREFIX, "")).getPayload().getExpiration();

        return expirationDate.toString();
    }
}
