package com.soonwook.aiproject.config;

import com.soonwook.aiproject.security.jwt.JwtAuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(cors -> cors.configurationSource(exchange -> {
          var c = new CorsConfiguration();
          c.setAllowedOrigins(List.of("http://localhost:5173"));
          c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
          c.setAllowedHeaders(List.of("*"));
          c.setAllowCredentials(true);
          return c;
        }))
        .authorizeExchange(ex -> ex
            .pathMatchers("/api/auth/**", "/h2-console/**").permitAll()
            .anyExchange().authenticated()
        )
        .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}



