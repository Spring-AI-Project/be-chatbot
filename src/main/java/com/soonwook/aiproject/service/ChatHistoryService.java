package com.soonwook.aiproject.service;

import com.soonwook.aiproject.entity.ChatHistory;
import com.soonwook.aiproject.entity.User;
import com.soonwook.aiproject.repository.ChatHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

  private final ChatHistoryRepository chatHistoryRepository;

  public void saveUserMessage(User user, String message) {
    ChatHistory history = ChatHistory.builder()
        .user(user)
        .role("USER")
        .message(message)
        .createdAt(LocalDateTime.now())
        .build();
    chatHistoryRepository.save(history);
  }

  public void saveBotMessage(User user, String message) {
    ChatHistory history = ChatHistory.builder()
        .user(user)
        .role("BOT")
        .message(message)
        .createdAt(LocalDateTime.now())
        .build();
    chatHistoryRepository.save(history);
  }

  public List<ChatHistory> getHistory(User user){
    return chatHistoryRepository.findTop10ByUserOrderByCreatedAtDesc(user);
  }
}
