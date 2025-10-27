package com.soonwook.aiproject.controller;

import com.soonwook.aiproject.entity.ChatHistory;
import com.soonwook.aiproject.entity.User;
import com.soonwook.aiproject.service.ChatBotService;
import com.soonwook.aiproject.service.ChatHistoryService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
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

  @GetMapping(value = "/context", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<String>> chatWithContext(
      @RequestParam String prompt,
      @AuthenticationPrincipal User user) {

    if (user == null) throw new RuntimeException("인증되지 않은 사용자입니다.");

    log.info("[Chat-Context] user={} prompt={}", user.getUsername(), prompt);
    chatHistoryService.saveUserMessage(user, prompt);

    List<ChatHistory> historyList = chatHistoryService.getRecentConversations(user, 5);
    String context = historyList.stream()
        .map(h -> h.getRole() + ": " + h.getMessage())
        .collect(Collectors.joining("\n"));

    String promptWithContext = """
      당신은 사용자의 질문에 대해 친절하게 답변하는 AI 챗봇입니다.
      모든 답변은 한글로 작성하세요.

      [이전 대화]
      %s

      [사용자] %s
      [봇]
      """.formatted(context, prompt);

    StringBuilder responseBuf = new StringBuilder();

    return chatBotService.streamResponse(promptWithContext)
        .doOnNext(responseBuf::append)                            // ✅ 실제 응답 누적
        .map(chunk -> ServerSentEvent.builder(chunk).build())     // ✅ SSE 감싸기
        .doOnComplete(() -> chatHistoryService                    // ✅ 최종 전체 응답 저장
            .saveBotMessage(user, responseBuf.toString()));
  }

}