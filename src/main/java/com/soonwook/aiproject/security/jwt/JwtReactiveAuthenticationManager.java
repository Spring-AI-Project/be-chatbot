package com.soonwook.aiproject.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    String token = authentication.getCredentials().toString();

    if (!jwtTokenProvider.validateToken(token)) {
      return Mono.empty();
    }

    return jwtTokenProvider.getAuthentication(token);
  }
}
