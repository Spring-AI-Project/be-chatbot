package com.soonwook.aiproject.controller;

import com.soonwook.aiproject.entity.User;
import com.soonwook.aiproject.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatBotController {

  private final ChatBotService chatBotService;

  @GetMapping("/api/chat")
  public Flux<String> chat(@RequestParam String prompt, @AuthenticationPrincipal User user) {
    log.info("user = {}", user.getUsername());
    return chatBotService.streamResponse(prompt);
  }
}