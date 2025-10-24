package com.soonwook.aiproject.controller;

import com.soonwook.aiproject.entity.ChatHistory;
import com.soonwook.aiproject.entity.User;
import com.soonwook.aiproject.service.ChatBotService;
import com.soonwook.aiproject.service.ChatHistoryService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatBotController {

  private final ChatBotService chatBotService;
  private final ChatHistoryService chatHistoryService;

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> chat(@RequestParam String prompt,
      @AuthenticationPrincipal User user) {
    if (user == null) throw new RuntimeException("인증되지 않은 사용자입니다.");

    log.info("[Chat] user={} prompt={}", user.getUsername(), prompt);
    chatHistoryService.saveUserMessage(user, prompt);

    StringBuilder responseBuilder = new StringBuilder();

    return chatBotService.streamResponse(prompt)
        .doOnNext(responseBuilder::append)
        .doOnComplete(() -> chatHistoryService.saveBotMessage(user, responseBuilder.toString()));
  }

  @GetMapping("/full")
  public Mono<ResponseEntity<Map<String, String>>> chatFull(@RequestParam String prompt,
      @AuthenticationPrincipal User user) {
    if (user == null) throw new RuntimeException("인증되지 않은 사용자입니다.");

    log.info("[Chat-Full] user={} prompt={}", user.getUsername(), prompt);
    chatHistoryService.saveUserMessage(user, prompt);

    return chatBotService.streamResponse(prompt)
        .collectList()
        .map(list -> String.join("", list))
        .doOnNext(fullResponse ->
            chatHistoryService.saveBotMessage(user, fullResponse)
        )
        .map(fullResponse -> ResponseEntity.ok(Map.of("response", fullResponse)));
  }

  @GetMapping("/history")
  public List<ChatHistory> getHistory(@AuthenticationPrincipal User user) {
    if (user == null) throw new RuntimeException("인증되지 않은 사용자입니다.");
    return chatHistoryService.getHistory(user);
  }
}