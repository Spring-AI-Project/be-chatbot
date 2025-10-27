package com.soonwook.aiproject.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().toString();
    if (path.startsWith("/api/auth/")) {
      return chain.filter(exchange); // 로그인/회원가입은 인증 제외
    }

    String token = extractToken(exchange);
    if (token == null || !jwtTokenProvider.validateToken(token)) {
      return chain.filter(exchange);
    }

    // ✅ 토큰에서 username 추출
    String username = jwtTokenProvider.getUsernameFromToken(token);

    return jwtTokenProvider.getAuthentication(username)
        .flatMap(auth -> {
          var context = new SecurityContextImpl(auth);
          return chain.filter(exchange)
              .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
        })
        .switchIfEmpty(chain.filter(exchange));
  }

  private String extractToken(ServerWebExchange exchange) {
    String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      log.info("JWT header detected: {}", header);
      return header.substring(7);
    }
    return null;
  }
}

