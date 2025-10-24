package com.soonwook.aiproject.config;

import com.soonwook.aiproject.entity.User;
import com.soonwook.aiproject.entity.User.Role;
import com.soonwook.aiproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserInitializer {

  private final PasswordEncoder passwordEncoder;

  @Bean
  public CommandLineRunner initDummyUser(UserRepository userRepository) {
    return args -> {
      String username = "test";
      String password = "1234";

      if (userRepository.findByUsername(username).isEmpty()) {
        User user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .role(Role.ROLE_USER)
            .build();
        userRepository.save(user);
        log.info("더미 테스트 유저 생성 완료 username : {} password : {}",username,password);
      }
    };
  }
}
