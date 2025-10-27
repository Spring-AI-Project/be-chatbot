package com.soonwook.aiproject.security.jwt;

import com.soonwook.aiproject.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtTokenProvider {

  private final UserRepository userRepository;
  private final Key key;
  private final long expiration;

  public JwtTokenProvider(
      UserRepository userRepository,
      @Value("${jwt.secret}") String secretKey,
      @Value("${jwt.expiration}") long expiration
  ) {
    this.userRepository = userRepository;
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    this.expiration = expiration;
  }

  public String generateToken(String username) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String getUsernameFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public Mono<Authentication> getAuthentication(String username) {
    return Mono.fromCallable(() -> {
      var user = userRepository.findByUsername(username)
          .orElseThrow(() -> new RuntimeException("User not found: " + username));
      return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    });
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
