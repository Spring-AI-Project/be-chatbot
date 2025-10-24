package com.soonwook.aiproject.controller;

import com.soonwook.aiproject.entity.User;
import com.soonwook.aiproject.entity.User.Role;
import com.soonwook.aiproject.repository.UserRepository;
import com.soonwook.aiproject.security.jwt.JwtTokenProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/signup")
  public String signup(@RequestBody User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setRole(Role.ROLE_USER);
    userRepository.save(user);
    return "회원가입 성공!";
  }

  @PostMapping("/login")
  public Map<String, String> login(@RequestBody Map<String, String> req) {
    String username = req.get("username");
    String password = req.get("password");

    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, password)
    );

    String token = jwtTokenProvider.generateToken(username);
    return Map.of("token", token);
  }
}

