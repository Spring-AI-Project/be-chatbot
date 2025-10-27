package com.soonwook.aiproject.service;

import com.soonwook.aiproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

  private final UserRepository userRepository;

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    // JPA는 blocking이므로 fromCallable() 로 감싸야 WebFlux에서 동작함
    return Mono.fromCallable(() -> userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found: " + username)));
  }
}