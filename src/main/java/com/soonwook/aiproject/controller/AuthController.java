package com.soonwook.aiproject.controller;

import com.soonwook.aiproject.dto.LoginRequest;
import com.soonwook.aiproject.entity.User;
import com.soonwook.aiproject.entity.User.Role;
import com.soonwook.aiproject.repository.UserRepository;
import com.soonwook.aiproject.security.jwt.JwtTokenProvider;
import com.soonwook.aiproject.service.CustomUserDetailsService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService userDetailsService;

  @PostMapping("/signup")
  public String signup(@RequestBody User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setRole(Role.ROLE_USER);
    userRepository.save(user);
    return "회원가입 성공!";
  }

  @PostMapping("/login")
  public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody LoginRequest request) {
    return userDetailsService.findByUsername(request.getUsername())
        .flatMap(userDetails -> {
          if (passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            String token = jwtTokenProvider.generateToken(userDetails.getUsername());
            Map<String, Object> body = new HashMap<>();
            body.put("token", token);
            body.put("username", userDetails.getUsername());
            body.put("message", "Login successful");
            return Mono.just(ResponseEntity.ok(body));
          } else {
            Map<String, Object> error = Map.of("message", "Invalid credentials");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error));
          }
        })
        .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", "User not found"))));
  }
}

